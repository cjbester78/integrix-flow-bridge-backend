package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.MailSenderAdapterConfig;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mail Sender Adapter implementation for email retrieval and processing (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Supports IMAP/POP3 protocols, email filtering, attachment handling, and S/MIME security.
 */
public class MailSenderAdapter extends AbstractSenderAdapter {
    
    private final MailSenderAdapterConfig config;
    private final Map<String, String> processedMessages = new ConcurrentHashMap<>();
    private Store mailStore;
    private Folder mailFolder;
    
    public MailSenderAdapter(MailSenderAdapterConfig config) {
        super(AdapterType.MAIL);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing Mail sender adapter (inbound) with server: {}:{} using {}", 
                config.getMailServerHost(), config.getMailServerPort(), config.getMailProtocol());
        
        validateConfiguration();
        
        // For per-poll mode, we don't maintain persistent connection
        if ("permanently".equals(config.getConnectionMode())) {
            connectToMailServer();
        }
        
        logger.info("Mail sender adapter initialized successfully");
    }
    
    @Override
    protected void doSenderDestroy() throws Exception {
        logger.info("Destroying Mail sender adapter");
        
        disconnectFromMailServer();
        processedMessages.clear();
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Basic mail server connectivity
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.MAIL, () -> {
            Store testStore = null;
            try {
                testStore = createMailStore();
                testStore.connect();
                
                if (testStore.isConnected()) {
                    return ConnectionTestUtil.createTestSuccess(AdapterType.MAIL, 
                            "Mail Connection", "Successfully connected to mail server");
                } else {
                    return ConnectionTestUtil.createTestFailure(AdapterType.MAIL, 
                            "Mail Connection", "Mail server connection failed", null);
                }
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.MAIL, 
                        "Mail Connection", "Failed to connect to mail server: " + e.getMessage(), e);
            } finally {
                if (testStore != null && testStore.isConnected()) {
                    try { testStore.close(); } catch (Exception ignored) {}
                }
            }
        }));
        
        // Test 2: Folder access
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.MAIL, () -> {
            Store testStore = null;
            Folder testFolder = null;
            try {
                testStore = createMailStore();
                testStore.connect();
                
                testFolder = testStore.getFolder(config.getFolderName());
                testFolder.open(Folder.READ_ONLY);
                
                int messageCount = testFolder.getMessageCount();
                int unreadCount = testFolder.getUnreadMessageCount();
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.MAIL, 
                        "Folder Access", String.format("Folder accessible: %d total, %d unread messages", 
                                messageCount, unreadCount));
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.MAIL, 
                        "Folder Access", "Failed to access mail folder: " + e.getMessage(), e);
            } finally {
                if (testFolder != null && testFolder.isOpen()) {
                    try { testFolder.close(false); } catch (Exception ignored) {}
                }
                if (testStore != null && testStore.isConnected()) {
                    try { testStore.close(); } catch (Exception ignored) {}
                }
            }
        }));
        
        // Test 3: Message filtering test
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.MAIL, () -> {
            Store testStore = null;
            Folder testFolder = null;
            try {
                testStore = createMailStore();
                testStore.connect();
                testFolder = testStore.getFolder(config.getFolderName());
                testFolder.open(Folder.READ_ONLY);
                
                SearchTerm searchTerm = buildSearchCriteria();
                Message[] messages = searchTerm != null ? 
                        testFolder.search(searchTerm) : testFolder.getMessages();
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.MAIL, 
                        "Message Filtering", "Found " + messages.length + " messages matching criteria");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.MAIL, 
                        "Message Filtering", "Failed to test message filtering: " + e.getMessage(), e);
            } finally {
                if (testFolder != null && testFolder.isOpen()) {
                    try { testFolder.close(false); } catch (Exception ignored) {}
                }
                if (testStore != null && testStore.isConnected()) {
                    try { testStore.close(); } catch (Exception ignored) {}
                }
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.MAIL, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // For Mail Sender (inbound), "send" means polling/retrieving emails FROM mail server
        return pollForEmails();
    }
    
    private AdapterResult pollForEmails() throws Exception {
        List<Map<String, Object>> processedEmails = new ArrayList<>();
        Store store = null;
        Folder folder = null;
        
        try {
            // Get or create connection
            if ("permanently".equals(config.getConnectionMode())) {
                store = mailStore;
                folder = mailFolder;
                if (store == null || !store.isConnected() || folder == null || !folder.isOpen()) {
                    connectToMailServer();
                    store = mailStore;
                    folder = mailFolder;
                }
            } else {
                store = createMailStore();
                store.connect();
                folder = store.getFolder(config.getFolderName());
                folder.open(Folder.READ_WRITE); // Need write access for marking as read/moving
            }
            
            // Build search criteria
            SearchTerm searchTerm = buildSearchCriteria();
            
            // Get messages
            Message[] messages = searchTerm != null ? 
                    folder.search(searchTerm) : folder.getMessages();
            
            // Apply max messages limit
            int maxMessages = config.getMaxMessages() != null ? 
                    Integer.parseInt(config.getMaxMessages()) : messages.length;
            int messagesToProcess = Math.min(messages.length, maxMessages);
            
            // Process messages
            for (int i = 0; i < messagesToProcess; i++) {
                Message message = messages[i];
                
                try {
                    if (shouldProcessMessage(message)) {
                        Map<String, Object> emailData = processMessage(message);
                        if (emailData != null) {
                            processedEmails.add(emailData);
                            handlePostProcessing(folder, message);
                            
                            // Mark as processed
                            processedMessages.put(getMessageId(message), String.valueOf(System.currentTimeMillis()));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing email message: {}", getMessageId(message), e);
                    
                    if (!config.isContinueOnError()) {
                        throw new AdapterException.ProcessingException(AdapterType.MAIL, 
                                "Email processing failed for message " + getMessageId(message) + ": " + e.getMessage(), e);
                    }
                }
            }
            
        } finally {
            if ("per-poll".equals(config.getConnectionMode())) {
                if (folder != null && folder.isOpen()) {
                    folder.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            }
        }
        
        logger.info("Mail sender adapter polled {} emails from server", processedEmails.size());
        
        return AdapterResult.success(processedEmails, 
                String.format("Retrieved %d emails from mail server", processedEmails.size()));
    }
    
    private Map<String, Object> processMessage(Message message) throws Exception {
        Map<String, Object> emailData = new HashMap<>();
        
        // Basic message properties
        emailData.put("messageId", getMessageId(message));
        emailData.put("subject", message.getSubject());
        emailData.put("from", message.getFrom() != null && message.getFrom().length > 0 ? 
                message.getFrom()[0].toString() : "");
        emailData.put("to", Arrays.toString(message.getAllRecipients()));
        emailData.put("sentDate", message.getSentDate());
        emailData.put("receivedDate", message.getReceivedDate());
        emailData.put("size", message.getSize());
        
        // Handle message content
        Object content = message.getContent();
        Map<String, String> contentData = extractContent(content);
        emailData.putAll(contentData);
        
        // Handle attachments
        if (config.isIncludeAttachments()) {
            List<Map<String, Object>> attachments = extractAttachments(message);
            emailData.put("attachments", attachments);
        }
        
        // Additional headers if configured
        if (config.isIncludeHeaders()) {
            Map<String, String> headers = new HashMap<>();
            Enumeration<Header> headerEnum = message.getAllHeaders();
            while (headerEnum.hasMoreElements()) {
                Header header = headerEnum.nextElement();
                headers.put(header.getName(), header.getValue());
            }
            emailData.put("headers", headers);
        }
        
        return emailData;
    }
    
    private Map<String, String> extractContent(Object content) throws Exception {
        Map<String, String> contentData = new HashMap<>();
        
        if (content instanceof String) {
            // Plain text content
            contentData.put("textContent", (String) content);
            contentData.put("contentType", "text/plain");
        } else if (content instanceof MimeMultipart) {
            // Multipart message
            MimeMultipart multipart = (MimeMultipart) content;
            StringBuilder textContent = new StringBuilder();
            StringBuilder htmlContent = new StringBuilder();
            
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                
                if (bodyPart.isMimeType("text/plain")) {
                    textContent.append(bodyPart.getContent().toString());
                } else if (bodyPart.isMimeType("text/html")) {
                    htmlContent.append(bodyPart.getContent().toString());
                }
            }
            
            // Include content based on configuration
            switch (config.getContentHandling().toLowerCase()) {
                case "text":
                    contentData.put("textContent", textContent.toString());
                    contentData.put("contentType", "text/plain");
                    break;
                case "html":
                    contentData.put("htmlContent", htmlContent.toString());
                    contentData.put("contentType", "text/html");
                    break;
                case "both":
                default:
                    contentData.put("textContent", textContent.toString());
                    contentData.put("htmlContent", htmlContent.toString());
                    contentData.put("contentType", "multipart");
                    break;
            }
        }
        
        return contentData;
    }
    
    private List<Map<String, Object>> extractAttachments(Message message) throws Exception {
        List<Map<String, Object>> attachments = new ArrayList<>();
        
        Object content = message.getContent();
        if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) ||
                    (bodyPart.getFileName() != null && !bodyPart.getFileName().isEmpty())) {
                    
                    Map<String, Object> attachment = new HashMap<>();
                    attachment.put("fileName", bodyPart.getFileName());
                    attachment.put("contentType", bodyPart.getContentType());
                    attachment.put("size", bodyPart.getSize());
                    
                    // Save attachment to directory if configured
                    if (config.getAttachmentDirectory() != null) {
                        String savedPath = saveAttachment(bodyPart);
                        attachment.put("savedPath", savedPath);
                    } else {
                        // Include content directly
                        try (InputStream is = bodyPart.getInputStream();
                             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                            
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                baos.write(buffer, 0, bytesRead);
                            }
                            attachment.put("content", baos.toByteArray());
                        }
                    }
                    
                    attachments.add(attachment);
                }
            }
        }
        
        return attachments;
    }
    
    private String saveAttachment(BodyPart bodyPart) throws Exception {
        String fileName = bodyPart.getFileName();
        if (fileName == null) {
            fileName = "attachment_" + System.currentTimeMillis();
        }
        
        File attachmentDir = new File(config.getAttachmentDirectory());
        if (!attachmentDir.exists()) {
            attachmentDir.mkdirs();
        }
        
        File attachmentFile = new File(attachmentDir, fileName);
        
        try (InputStream is = bodyPart.getInputStream();
             FileOutputStream fos = new FileOutputStream(attachmentFile)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        return attachmentFile.getAbsolutePath();
    }
    
    private void handlePostProcessing(Folder folder, Message message) throws Exception {
        // Mark as read if configured
        if (config.isMarkAsRead()) {
            message.setFlag(Flags.Flag.SEEN, true);
        }
        
        // Move to processed folder if configured
        if (config.getProcessedFolder() != null) {
            Folder processedFolder = folder.getStore().getFolder(config.getProcessedFolder());
            if (!processedFolder.exists()) {
                processedFolder.create(Folder.HOLDS_MESSAGES);
            }
            
            folder.copyMessages(new Message[]{message}, processedFolder);
        }
        
        // Delete after processing if configured
        if (config.isDeleteAfterFetch()) {
            message.setFlag(Flags.Flag.DELETED, true);
        }
    }
    
    private boolean shouldProcessMessage(Message message) throws Exception {
        String messageId = getMessageId(message);
        
        // Check if already processed
        if (processedMessages.containsKey(messageId)) {
            return false;
        }
        
        // Additional filtering can be added here
        return true;
    }
    
    private String getMessageId(Message message) throws Exception {
        String[] messageIds = message.getHeader("Message-ID");
        if (messageIds != null && messageIds.length > 0) {
            return messageIds[0];
        }
        
        // Fallback to subject + sent date
        return message.getSubject() + "_" + 
               (message.getSentDate() != null ? message.getSentDate().getTime() : System.currentTimeMillis());
    }
    
    private SearchTerm buildSearchCriteria() throws Exception {
        List<SearchTerm> terms = new ArrayList<>();
        
        // Unread messages only
        if (config.isFetchUnreadOnly()) {
            terms.add(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        }
        
        // Date filters
        if (config.getDateFromFilter() != null) {
            Date fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(config.getDateFromFilter());
            terms.add(new ReceivedDateTerm(ComparisonTerm.GE, fromDate));
        }
        
        if (config.getDateToFilter() != null) {
            Date toDate = new SimpleDateFormat("yyyy-MM-dd").parse(config.getDateToFilter());
            terms.add(new ReceivedDateTerm(ComparisonTerm.LE, toDate));
        }
        
        // Subject filter
        if (config.getSubjectFilter() != null && !config.getSubjectFilter().trim().isEmpty()) {
            terms.add(new SubjectTerm(config.getSubjectFilter()));
        }
        
        // From address filter
        if (config.getFromAddressFilter() != null && !config.getFromAddressFilter().trim().isEmpty()) {
            terms.add(new FromStringTerm(config.getFromAddressFilter()));
        }
        
        // Today's messages only
        if (config.isFetchFromToday()) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            terms.add(new ReceivedDateTerm(ComparisonTerm.GE, today.getTime()));
        }
        
        // Combine all terms with AND
        if (terms.isEmpty()) {
            return null;
        } else if (terms.size() == 1) {
            return terms.get(0);
        } else {
            SearchTerm result = terms.get(0);
            for (int i = 1; i < terms.size(); i++) {
                result = new AndTerm(result, terms.get(i));
            }
            return result;
        }
    }
    
    private void connectToMailServer() throws Exception {
        if (mailStore != null) {
            disconnectFromMailServer();
        }
        
        mailStore = createMailStore();
        mailStore.connect();
        
        mailFolder = mailStore.getFolder(config.getFolderName());
        mailFolder.open(Folder.READ_WRITE);
    }
    
    private Store createMailStore() throws Exception {
        Properties props = new Properties();
        
        String protocol = config.getMailProtocol().toLowerCase();
        
        // Configure protocol-specific properties
        if ("imap".equals(protocol)) {
            props.setProperty("mail.store.protocol", "imap");
            props.setProperty("mail.imap.host", config.getMailServerHost());
            props.setProperty("mail.imap.port", config.getMailServerPort());
            
            if (config.isUseSSLTLS()) {
                props.setProperty("mail.imap.ssl.enable", "true");
                props.setProperty("mail.imap.ssl.trust", "*");
            }
            
            props.setProperty("mail.imap.connectiontimeout", config.getConnectionTimeout());
            props.setProperty("mail.imap.timeout", config.getReadTimeout());
            
        } else if ("pop3".equals(protocol)) {
            props.setProperty("mail.store.protocol", "pop3");
            props.setProperty("mail.pop3.host", config.getMailServerHost());
            props.setProperty("mail.pop3.port", config.getMailServerPort());
            
            if (config.isUseSSLTLS()) {
                props.setProperty("mail.pop3.ssl.enable", "true");
                props.setProperty("mail.pop3.ssl.trust", "*");
            }
            
            props.setProperty("mail.pop3.connectiontimeout", config.getConnectionTimeout());
            props.setProperty("mail.pop3.timeout", config.getReadTimeout());
        }
        
        Session session = Session.getInstance(props);
        Store store = session.getStore(protocol);
        
        return store;
    }
    
    private void disconnectFromMailServer() {
        if (mailFolder != null && mailFolder.isOpen()) {
            try {
                mailFolder.close(false);
            } catch (Exception e) {
                logger.warn("Error closing mail folder", e);
            }
            mailFolder = null;
        }
        
        if (mailStore != null && mailStore.isConnected()) {
            try {
                mailStore.close();
            } catch (Exception e) {
                logger.warn("Error closing mail store", e);
            }
            mailStore = null;
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getMailServerHost() == null || config.getMailServerHost().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.MAIL, "Mail server host is required");
        }
        if (config.getMailUsername() == null || config.getMailUsername().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.MAIL, "Mail username is required");
        }
        if (config.getMailPassword() == null) {
            throw new AdapterException.ConfigurationException(AdapterType.MAIL, "Mail password is required");
        }
        if (config.getFolderName() == null || config.getFolderName().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.MAIL, "Mail folder name is required");
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("Mail Sender (Inbound): %s:%s, User: %s, Protocol: %s, Folder: %s, Polling: %sms", 
                config.getMailServerHost(),
                config.getMailServerPort(),
                config.getMailUsername(),
                config.getMailProtocol(),
                config.getFolderName(),
                config.getPollingInterval());
    }
}