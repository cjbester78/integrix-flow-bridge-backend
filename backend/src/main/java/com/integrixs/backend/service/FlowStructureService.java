package com.integrixs.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.FlowStructureMessage;
import com.integrixs.data.model.MessageStructure;
import com.integrixs.data.model.User;
import com.integrixs.data.model.FlowStructure.ProcessingMode;
import com.integrixs.data.model.FlowStructureMessage.MessageType;
import com.integrixs.data.repository.BusinessComponentRepository;
import com.integrixs.data.repository.FlowStructureMessageRepository;
import com.integrixs.data.repository.FlowStructureRepository;
import com.integrixs.data.repository.MessageStructureRepository;
import com.integrixs.shared.dto.structure.*;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.user.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlowStructureService {
    
    private final FlowStructureRepository flowStructureRepository;
    private final MessageStructureRepository messageStructureRepository;
    private final FlowStructureMessageRepository flowStructureMessageRepository;
    private final BusinessComponentRepository businessComponentRepository;
    private final EnvironmentPermissionService environmentPermissionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Transactional
    public FlowStructureDTO create(FlowStructureCreateRequestDTO request, User currentUser) {
        log.info("Creating flow structure: {}", request.getName());
        log.info("Current environment: {}, canCreateFlows: {}", 
            environmentPermissionService.getEnvironmentInfo().get("type"),
            environmentPermissionService.isActionAllowed("flow.create"));
        
        // Check environment permissions
        try {
            environmentPermissionService.checkPermission("flow.create");
        } catch (Exception e) {
            log.error("Environment permission check failed: ", e);
            throw e;
        }
        
        try {
            // Check if name already exists for business component
        if (flowStructureRepository.existsByNameAndBusinessComponentIdAndIsActiveTrue(
                request.getName(), request.getBusinessComponentId())) {
            throw new RuntimeException("Flow structure with name '" + request.getName() + 
                    "' already exists for this business component");
        }
        
        FlowStructure flowStructure = FlowStructure.builder()
                .name(request.getName())
                .description(request.getDescription())
                .processingMode(FlowStructure.ProcessingMode.valueOf(request.getProcessingMode().name()))
                .direction(FlowStructure.Direction.valueOf(request.getDirection().name()))
                .namespace(request.getNamespace() != null ? serializeToJson(request.getNamespace()) : null)
                .metadata(request.getMetadata() != null ? serializeToJson(request.getMetadata()) : null)
                .tags(request.getTags() != null ? serializeToJson(request.getTags()) : null)
                .businessComponent(businessComponentRepository.findById(request.getBusinessComponentId())
                        .orElseThrow(() -> new RuntimeException("Business component not found")))
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();
        
        flowStructure = flowStructureRepository.save(flowStructure);
        
        // Create flow structure messages
        if (request.getMessageStructureIds() != null) {
            createFlowStructureMessages(flowStructure, request.getMessageStructureIds());
        }
        
        // Set WSDL content if provided (imported WSDL), otherwise generate
        if (request.getWsdlContent() != null && !request.getWsdlContent().trim().isEmpty()) {
            flowStructure.setWsdlContent(request.getWsdlContent());
            flowStructure.setSourceType("EXTERNAL");
        } else {
            generateWsdl(flowStructure);
            flowStructure.setSourceType("INTERNAL");
        }
        
        return toDTO(flowStructure);
        } catch (Exception e) {
            log.error("Error creating flow structure: ", e);
            throw e;
        }
    }
    
    @Transactional
    public FlowStructureDTO update(String id, FlowStructureCreateRequestDTO request, User currentUser) {
        log.info("Updating flow structure: {}", id);
        
        FlowStructure flowStructure = flowStructureRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Flow structure not found"));
        
        // Check if name is being changed and already exists
        if (!flowStructure.getName().equals(request.getName()) &&
                flowStructureRepository.existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(
                        request.getName(), request.getBusinessComponentId(), id)) {
            throw new RuntimeException("Flow structure with name '" + request.getName() + 
                    "' already exists for this business component");
        }
        
        flowStructure.setName(request.getName());
        flowStructure.setDescription(request.getDescription());
        flowStructure.setProcessingMode(FlowStructure.ProcessingMode.valueOf(request.getProcessingMode().name()));
        flowStructure.setDirection(FlowStructure.Direction.valueOf(request.getDirection().name()));
        flowStructure.setNamespace(request.getNamespace() != null ? serializeToJson(request.getNamespace()) : null);
        flowStructure.setMetadata(request.getMetadata() != null ? serializeToJson(request.getMetadata()) : null);
        flowStructure.setTags(request.getTags() != null ? serializeToJson(request.getTags()) : null);
        flowStructure.setBusinessComponent(businessComponentRepository.findById(request.getBusinessComponentId())
                .orElseThrow(() -> new RuntimeException("Business component not found")));
        flowStructure.setUpdatedBy(currentUser);
        flowStructure.setVersion(flowStructure.getVersion() + 1);
        
        // Update flow structure messages
        flowStructureMessageRepository.deleteByFlowStructureId(id);
        if (request.getMessageStructureIds() != null) {
            createFlowStructureMessages(flowStructure, request.getMessageStructureIds());
        }
        
        // Update WSDL content if provided, otherwise regenerate
        if (request.getWsdlContent() != null && !request.getWsdlContent().trim().isEmpty()) {
            flowStructure.setWsdlContent(request.getWsdlContent());
            flowStructure.setSourceType("EXTERNAL");
        } else {
            generateWsdl(flowStructure);
            // Keep existing source type if updating, otherwise set to INTERNAL
            if (flowStructure.getSourceType() == null) {
                flowStructure.setSourceType("INTERNAL");
            }
        }
        
        flowStructure = flowStructureRepository.save(flowStructure);
        return toDTO(flowStructure);
    }
    
    @Transactional(readOnly = true)
    public FlowStructureDTO findById(String id) {
        FlowStructure flowStructure = flowStructureRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Flow structure not found"));
        return toDTO(flowStructure);
    }
    
    @Transactional(readOnly = true)
    public Page<FlowStructureDTO> findAll(String businessComponentId, 
                                         FlowStructureDTO.ProcessingMode processingMode,
                                         FlowStructureDTO.Direction direction,
                                         String search, 
                                         Pageable pageable) {
        FlowStructure.ProcessingMode mode = processingMode != null ? 
                FlowStructure.ProcessingMode.valueOf(processingMode.name()) : null;
        FlowStructure.Direction dir = direction != null ? 
                FlowStructure.Direction.valueOf(direction.name()) : null;
                
        Page<FlowStructure> page = flowStructureRepository.findAllWithFilters(
                businessComponentId, mode, dir, search, pageable);
        return page.map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public List<FlowStructureDTO> findByBusinessComponent(String businessComponentId) {
        return flowStructureRepository.findByBusinessComponentId(businessComponentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<FlowStructureDTO> findByMessageStructure(String messageStructureId) {
        log.info("Finding flow structures using message structure: {}", messageStructureId);
        List<FlowStructure> flowStructures = flowStructureMessageRepository.findFlowStructuresByMessageStructureId(messageStructureId);
        
        return flowStructures.stream()
                .filter(FlowStructure::getIsActive)
                .map(flowStructure -> {
                    FlowStructureDTO dto = toDTO(flowStructure);
                    
                    // Add information about which message types use this message structure
                    List<String> messageTypes = flowStructureMessageRepository.findByFlowStructureId(flowStructure.getId())
                            .stream()
                            .filter(fsm -> fsm.getMessageStructure().getId().equals(messageStructureId))
                            .map(fsm -> fsm.getMessageType().toString())
                            .collect(Collectors.toList());
                    
                    // Store message types in metadata for the frontend
                    Map<String, Object> metadata = dto.getMetadata() != null ? 
                            new HashMap<>(dto.getMetadata()) : new HashMap<>();
                    metadata.put("messageTypes", messageTypes);
                    dto.setMetadata(metadata);
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void delete(String id) {
        log.info("Deleting flow structure: {}", id);
        FlowStructure flowStructure = flowStructureRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Flow structure not found"));
        
        flowStructure.setIsActive(false);
        flowStructureRepository.save(flowStructure);
    }
    
    private void createFlowStructureMessages(FlowStructure flowStructure, 
                                           Map<FlowStructureMessageDTO.MessageType, String> messageStructureIds) {
        for (Map.Entry<FlowStructureMessageDTO.MessageType, String> entry : messageStructureIds.entrySet()) {
            MessageStructure messageStructure = messageStructureRepository.findById(entry.getValue())
                    .orElseThrow(() -> new RuntimeException("Message structure not found: " + entry.getValue()));
            
            FlowStructureMessage flowMessage = FlowStructureMessage.builder()
                    .flowStructure(flowStructure)
                    .messageType(FlowStructureMessage.MessageType.valueOf(entry.getKey().name()))
                    .messageStructure(messageStructure)
                    .build();
            
            flowStructureMessageRepository.save(flowMessage);
        }
    }
    
    private void generateWsdl(FlowStructure flowStructure) {
        log.info("=== GENERATING WSDL for flow structure: {} ===", flowStructure.getName());
        String serviceName = flowStructure.getName().replaceAll("[^a-zA-Z0-9]", "");
        String namespace = "http://integrixflowbridge.com/" + serviceName;
        
        StringBuilder wsdl = new StringBuilder();
        wsdl.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        wsdl.append("<definitions name=\"").append(serviceName).append("Service\"\n");
        wsdl.append("             targetNamespace=\"").append(namespace).append("\"\n");
        wsdl.append("             xmlns=\"http://schemas.xmlsoap.org/wsdl/\"\n");
        wsdl.append("             xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\n");
        wsdl.append("             xmlns:tns=\"").append(namespace).append("\"\n");
        wsdl.append("             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n");
        wsdl.append("\n");
        
        // Types section - define message structures based on linked message structures
        wsdl.append("  <types>\n");
        wsdl.append("    <xsd:schema targetNamespace=\"").append(namespace).append("\">\n");
        
        // Generate element definitions based on message structures
        Set<FlowStructureMessage> messages = flowStructure.getFlowStructureMessages();
        log.info("Flow structure has {} message structure associations", messages != null ? messages.size() : 0);
        
        if (messages != null && !messages.isEmpty()) {
            boolean hasInlineXsd = false;
            
            // First, check if all message structures have XSD content for inline inclusion
            for (FlowStructureMessage msg : messages) {
                MessageStructure msgStructure = msg.getMessageStructure();
                log.info("Processing message structure: {} (type: {})", 
                    msgStructure != null ? msgStructure.getName() : "null", 
                    msg.getMessageType());
                if (msgStructure != null) {
                    String xsdContent = msgStructure.getXsdContent();
                    log.info("Message structure {} - XSD content present: {}, length: {}", 
                        msgStructure.getName(), 
                        xsdContent != null && !xsdContent.trim().isEmpty(),
                        xsdContent != null ? xsdContent.length() : 0);
                    
                    if (xsdContent != null && !xsdContent.trim().isEmpty()) {
                        hasInlineXsd = true;
                        // Extract and inline the XSD content
                        try {
                            log.info("Processing XSD content for message structure: {} (type: {})", msgStructure.getName(), msg.getMessageType());
                            log.info("XSD content preview: {}", xsdContent.substring(0, Math.min(200, xsdContent.length())));
                        
                        // Parse the XSD to extract element definitions
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document xsdDoc = builder.parse(new ByteArrayInputStream(xsdContent.getBytes(StandardCharsets.UTF_8)));
                        
                        // Get all element definitions from the XSD
                        NodeList elements = xsdDoc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "element");
                        
                        for (int i = 0; i < elements.getLength(); i++) {
                            Element elem = (Element) elements.item(i);
                            String elementName = elem.getAttribute("name");
                            
                            // Only include top-level elements (those without parent elements)
                            if (elementName != null && !elementName.isEmpty() && elem.getParentNode().getNodeName().endsWith("schema")) {
                                wsdl.append("      <!-- ").append(msg.getMessageType()).append(" message from ").append(msgStructure.getName()).append(" -->\n");
                                
                                // Serialize the element with all its content
                                wsdl.append(serializeElement(elem, "      "));
                                wsdl.append("\n");
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error processing XSD content for message structure: " + msgStructure.getName(), e);
                        // Fall back to anyType if parsing fails
                        String elementName = msgStructure.getName();
                        wsdl.append("      <!-- ").append(msg.getMessageType()).append(" message from ").append(msgStructure.getName()).append(" (parse error) -->\n");
                        wsdl.append("      <xsd:element name=\"").append(elementName).append("\" type=\"xsd:anyType\"/>\n");
                    }
                    } else {
                        // No XSD content, use anyType
                        String elementName = msgStructure.getName();
                        wsdl.append("      <!-- ").append(msg.getMessageType()).append(" message from ").append(msgStructure.getName()).append(" -->\n");
                        wsdl.append("      <xsd:element name=\"").append(elementName).append("\" type=\"xsd:anyType\"/>\n");
                    }
                } else {
                    // Fallback to generic name if no structure defined
                    String elementName = getElementNameForMessageType(msg.getMessageType());
                    wsdl.append("      <xsd:element name=\"").append(elementName).append("\" type=\"xsd:anyType\"/>\n");
                }
            }
        } else {
            // Default elements if no message structures defined
            if (flowStructure.getProcessingMode() == ProcessingMode.SYNC) {
                wsdl.append("      <xsd:element name=\"Request\" type=\"xsd:anyType\"/>\n");
                wsdl.append("      <xsd:element name=\"Response\" type=\"xsd:anyType\"/>\n");
                wsdl.append("      <xsd:element name=\"Fault\" type=\"xsd:anyType\"/>\n");
            } else {
                wsdl.append("      <xsd:element name=\"Message\" type=\"xsd:anyType\"/>\n");
            }
        }
        
        wsdl.append("    </xsd:schema>\n");
        wsdl.append("  </types>\n");
        wsdl.append("\n");
        
        // Messages section
        if (messages != null && !messages.isEmpty()) {
            for (FlowStructureMessage msg : messages) {
                String messageName = getMessageNameForType(msg.getMessageType());
                MessageStructure msgStructure = msg.getMessageStructure();
                String elementName = msgStructure != null ? msgStructure.getName() : getElementNameForMessageType(msg.getMessageType());
                wsdl.append("  <message name=\"").append(messageName).append("\">\n");
                wsdl.append("    <part name=\"parameters\" element=\"tns:").append(elementName).append("\"/>\n");
                wsdl.append("  </message>\n");
                wsdl.append("\n");
            }
        } else {
            // Default messages
            if (flowStructure.getProcessingMode() == ProcessingMode.SYNC) {
                wsdl.append("  <message name=\"RequestMessage\">\n");
                wsdl.append("    <part name=\"parameters\" element=\"tns:Request\"/>\n");
                wsdl.append("  </message>\n");
                wsdl.append("\n");
                wsdl.append("  <message name=\"ResponseMessage\">\n");
                wsdl.append("    <part name=\"parameters\" element=\"tns:Response\"/>\n");
                wsdl.append("  </message>\n");
                wsdl.append("\n");
                wsdl.append("  <message name=\"FaultMessage\">\n");
                wsdl.append("    <part name=\"parameters\" element=\"tns:Fault\"/>\n");
                wsdl.append("  </message>\n");
                wsdl.append("\n");
            } else {
                wsdl.append("  <message name=\"Message\">\n");
                wsdl.append("    <part name=\"parameters\" element=\"tns:Message\"/>\n");
                wsdl.append("  </message>\n");
                wsdl.append("\n");
            }
        }
        
        // PortType section
        wsdl.append("  <portType name=\"").append(serviceName).append("PortType\">\n");
        wsdl.append("    <operation name=\"process\">\n");
        
        if (flowStructure.getProcessingMode() == ProcessingMode.SYNC) {
            wsdl.append("      <input message=\"tns:RequestMessage\"/>\n");
            wsdl.append("      <output message=\"tns:ResponseMessage\"/>\n");
            wsdl.append("      <fault name=\"fault\" message=\"tns:FaultMessage\"/>\n");
        } else {
            wsdl.append("      <input message=\"tns:Message\"/>\n");
        }
        
        wsdl.append("    </operation>\n");
        wsdl.append("  </portType>\n");
        wsdl.append("\n");
        
        // Binding section
        wsdl.append("  <binding name=\"").append(serviceName).append("Binding\" type=\"tns:").append(serviceName).append("PortType\">\n");
        wsdl.append("    <soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>\n");
        wsdl.append("    <operation name=\"process\">\n");
        wsdl.append("      <soap:operation soapAction=\"process\"/>\n");
        wsdl.append("      <input>\n");
        wsdl.append("        <soap:body use=\"literal\"/>\n");
        wsdl.append("      </input>\n");
        
        if (flowStructure.getProcessingMode() == ProcessingMode.SYNC) {
            wsdl.append("      <output>\n");
            wsdl.append("        <soap:body use=\"literal\"/>\n");
            wsdl.append("      </output>\n");
            wsdl.append("      <fault name=\"fault\">\n");
            wsdl.append("        <soap:fault name=\"fault\" use=\"literal\"/>\n");
            wsdl.append("      </fault>\n");
        }
        
        wsdl.append("    </operation>\n");
        wsdl.append("  </binding>\n");
        wsdl.append("\n");
        
        // Service section
        wsdl.append("  <service name=\"").append(serviceName).append("Service\">\n");
        wsdl.append("    <port name=\"").append(serviceName).append("Port\" binding=\"tns:").append(serviceName).append("Binding\">\n");
        wsdl.append("      <soap:address location=\"http://localhost:8080/api/flow/").append(serviceName).append("\"/>\n");
        wsdl.append("    </port>\n");
        wsdl.append("  </service>\n");
        wsdl.append("</definitions>");
        
        String generatedWsdl = wsdl.toString();
        log.info("=== GENERATED WSDL PREVIEW (first 500 chars) ===");
        log.info(generatedWsdl.substring(0, Math.min(500, generatedWsdl.length())));
        log.info("=== END WSDL GENERATION ===");
        
        flowStructure.setWsdlContent(generatedWsdl);
        
        // Store operation info in metadata
        try {
            Map<String, Object> metadata = flowStructure.getMetadata() != null ?
                    objectMapper.readValue(flowStructure.getMetadata(), Map.class) : new HashMap<>();
            
            Map<String, Object> operationInfo = new HashMap<>();
            operationInfo.put("hasInput", true);
            operationInfo.put("hasOutput", flowStructure.getProcessingMode() == ProcessingMode.SYNC);
            operationInfo.put("hasFault", flowStructure.getProcessingMode() == ProcessingMode.SYNC);
            operationInfo.put("isSynchronous", flowStructure.getProcessingMode() == ProcessingMode.SYNC);
            
            List<String> messageTypes = new ArrayList<>();
            messageTypes.add("input");
            if (flowStructure.getProcessingMode() == ProcessingMode.SYNC) {
                messageTypes.add("output");
                messageTypes.add("fault");
            }
            operationInfo.put("messageTypes", messageTypes);
            
            metadata.put("operationInfo", operationInfo);
            flowStructure.setMetadata(objectMapper.writeValueAsString(metadata));
        } catch (Exception e) {
            log.error("Error updating metadata with operation info", e);
        }
    }
    
    private String getElementNameForMessageType(MessageType type) {
        switch (type) {
            case INPUT:
                return "Request";
            case OUTPUT:
                return "Response";
            case FAULT:
                return "Fault";
            default:
                return "Message";
        }
    }
    
    private String getMessageNameForType(MessageType type) {
        switch (type) {
            case INPUT:
                return "RequestMessage";
            case OUTPUT:
                return "ResponseMessage";
            case FAULT:
                return "FaultMessage";
            default:
                return "Message";
        }
    }
    
    @Transactional
    public void regenerateWsdlForAll() {
        log.info("Regenerating WSDL for all flow structures");
        List<FlowStructure> flowStructures = flowStructureRepository.findAll();
        for (FlowStructure flowStructure : flowStructures) {
            if (flowStructure.getWsdlContent() == null || 
                flowStructure.getWsdlContent().contains("WSDL generation pending")) {
                generateWsdl(flowStructure);
                flowStructureRepository.save(flowStructure);
                log.info("Regenerated WSDL for flow structure: {}", flowStructure.getName());
            }
        }
    }
    
    private FlowStructureDTO toDTO(FlowStructure entity) {
        try {
            Set<FlowStructureMessageDTO> messages = entity.getFlowStructureMessages() != null ?
                    entity.getFlowStructureMessages().stream()
                            .map(this::toFlowStructureMessageDTO)
                            .collect(Collectors.toSet()) : new HashSet<>();
            
            return FlowStructureDTO.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .processingMode(FlowStructureDTO.ProcessingMode.valueOf(entity.getProcessingMode().name()))
                    .direction(FlowStructureDTO.Direction.valueOf(entity.getDirection().name()))
                    .wsdlContent(entity.getWsdlContent())
                    .sourceType(entity.getSourceType())
                    .namespace(entity.getNamespace() != null ? 
                            objectMapper.readValue(entity.getNamespace(), new TypeReference<Map<String, Object>>() {}) : null)
                    .metadata(entity.getMetadata() != null ? 
                            objectMapper.readValue(entity.getMetadata(), new TypeReference<Map<String, Object>>() {}) : null)
                    .tags(entity.getTags() != null ? 
                            objectMapper.readValue(entity.getTags(), new TypeReference<Set<String>>() {}) : null)
                    .version(entity.getVersion())
                    .isActive(entity.getIsActive())
                    .businessComponent(toBusinessComponentDTO(entity.getBusinessComponent()))
                    .flowStructureMessages(messages)
                    .createdBy(toUserDTO(entity.getCreatedBy()))
                    .updatedBy(toUserDTO(entity.getUpdatedBy()))
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting FlowStructure to DTO", e);
            throw new RuntimeException("Error converting FlowStructure to DTO", e);
        }
    }
    
    private FlowStructureMessageDTO toFlowStructureMessageDTO(FlowStructureMessage entity) {
        return FlowStructureMessageDTO.builder()
                .flowStructureId(entity.getFlowStructure().getId())
                .messageType(FlowStructureMessageDTO.MessageType.valueOf(entity.getMessageType().name()))
                .messageStructure(toMessageStructureDTO(entity.getMessageStructure()))
                .build();
    }
    
    private MessageStructureDTO toMessageStructureDTO(MessageStructure entity) {
        try {
            return MessageStructureDTO.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .xsdContent(entity.getXsdContent())
                    .namespace(entity.getNamespace() != null ? 
                            objectMapper.readValue(entity.getNamespace(), new TypeReference<Map<String, Object>>() {}) : null)
                    .metadata(entity.getMetadata() != null ? 
                            objectMapper.readValue(entity.getMetadata(), new TypeReference<Map<String, Object>>() {}) : null)
                    .tags(entity.getTags() != null ? 
                            objectMapper.readValue(entity.getTags(), new TypeReference<Set<String>>() {}) : null)
                    .version(entity.getVersion())
                    .isActive(entity.getIsActive())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting MessageStructure to DTO", e);
            throw new RuntimeException("Error converting MessageStructure to DTO", e);
        }
    }
    
    private BusinessComponentDTO toBusinessComponentDTO(com.integrixs.data.model.BusinessComponent entity) {
        if (entity == null) return null;
        return BusinessComponentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
    
    private UserDTO toUserDTO(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
    
    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error serializing to JSON", e);
            throw new RuntimeException("Error serializing to JSON", e);
        }
    }
    
    private String serializeElement(Element element, String indent) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            
            // Clone the element to avoid modifying the original
            Element clonedElement = (Element) element.cloneNode(true);
            
            // Fix namespace prefixes to use xsd: instead of xs:
            fixNamespacePrefixes(clonedElement);
            
            DOMSource source = new DOMSource(clonedElement);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            
            // Add proper indentation
            String serialized = writer.toString();
            String[] lines = serialized.split("\n");
            StringBuilder indented = new StringBuilder();
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    indented.append(indent).append(line.trim()).append("\n");
                }
            }
            
            return indented.toString().trim();
        } catch (Exception e) {
            log.error("Error serializing element", e);
            // Fallback to basic element
            return indent + "<xsd:element name=\"" + element.getAttribute("name") + "\" type=\"xsd:anyType\"/>";
        }
    }
    
    private void fixNamespacePrefixes(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element elem = (Element) node;
            
            // Fix element name
            if (elem.getPrefix() != null && elem.getPrefix().equals("xs")) {
                elem.setPrefix("xsd");
            }
            
            // Fix attributes
            NamedNodeMap attributes = elem.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                String value = attr.getNodeValue();
                if (value != null && value.contains("xs:")) {
                    attr.setNodeValue(value.replace("xs:", "xsd:"));
                }
            }
            
            // Recursively fix child nodes
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                fixNamespacePrefixes(children.item(i));
            }
        }
    }
}