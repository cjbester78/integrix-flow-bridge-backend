package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.MailReceiverAdapterConfig;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mail Receiver Adapter implementation for email sending and delivery (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Supports SMTP protocol, email composition, attachments, and authentication.
 */
public class MailReceiverAdapter extends AbstractReceiverAdapter {
    
    private final MailReceiverAdapterConfig config;
    private Session mailSession;
    private final AtomicInteger batchCounter = new AtomicInteger(0);
    private final List<Object> batchBuffer = new ArrayList<>();
    private long lastBatchFlush = System.currentTimeMillis();
    
    public MailReceiverAdapter(MailReceiverAdapterConfig config) {
        super(AdapterType.MAIL);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing Mail receiver adapter (outbound) with server: {}:{}", 
                config.getSmtpServerHost(), config.getSmtpServerPort());
        
        validateConfiguration();
        initializeMailSession();
        
        logger.info("Mail receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying Mail receiver adapter");
        
        // Flush any remaining batch data
        if (config.isEnableBatching() && !batchBuffer.isEmpty()) {
            try {
                flushBatch();
            } catch (Exception e) {
                logger.warn("Error flushing batch during Mail adapter shutdown", e);
            }
        }
        
        batchBuffer.clear();
        mailSession = null;
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Basic SMTP connectivity
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.MAIL, () -> {
            Transport testTransport = null;
            try {
                testTransport = mailSession.getTransport("smtp");
                testTransport.connect(config.getSmtpUsername(), config.getSmtpPassword());
                
                if (testTransport.isConnected()) {
                    return ConnectionTestUtil.createTestSuccess(AdapterType.MAIL, 
                            "SMTP Connection", "Successfully connected to SMTP server");
                } else {
                    return ConnectionTestUtil.createTestFailure(AdapterType.MAIL, 
                            "SMTP Connection", "SMTP server connection failed", null);
                }
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.MAIL, 
                        "SMTP Connection", "Failed to connect to SMTP server: " + e.getMessage(), e);
            } finally {
                if (testTransport != null && testTransport.isConnected()) {
                    try { testTransport.close(); } catch (Exception ignored) {}
                }
            }
        }));
        
        // Test 2: Email composition test
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.MAIL, () -> {
            try {
                // Create a test email without sending
                MimeMessage testMessage = createTestMessage();
                
                if (testMessage != null) {
                    return ConnectionTestUtil.createTestSuccess(AdapterType.MAIL, 
                            "Email Composition", "Successfully created test email message");
                } else {
                    return ConnectionTestUtil.createTestFailure(AdapterType.MAIL, 
                            "Email Composition", "Failed to create test email message", null);
                }
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.MAIL, 
                        "Email Composition", "Failed to compose test email: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: Template processing test
        if (config.getEmailTemplate() != null || config.getSubjectTemplate() != null) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.MAIL, () -> {
                try {
                    Map<String, Object> testData = new HashMap<>();
                    testData.put("testKey", "testValue");
                    testData.put("timestamp", System.currentTimeMillis());
                    
                    String processedSubject = processTemplate(
                            config.getSubjectTemplate() != null ? config.getSubjectTemplate() : "Test Subject", 
                            testData);
                    String processedBody = processTemplate(
                            config.getEmailTemplate() != null ? config.getEmailTemplate() : "Test Body", 
                            testData);
                    
                    return ConnectionTestUtil.createTestSuccess(AdapterType.MAIL, 
                            "Template Processing", "Successfully processed email templates");
                            
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.MAIL, 
                            "Template Processing", "Failed to process email templates: " + e.getMessage(), e);
                }
            }));
        }
        
        return ConnectionTestUtil.combineTestResults(AdapterType.MAIL, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doReceive(Object payload) throws Exception {
        // For Mail Receiver (outbound), this method sends emails TO recipients
        if (config.isEnableBatching()) {
            return addToBatch(payload);
        } else {
            return sendEmail(payload);
        }
    }
    
    protected AdapterResult doReceive() throws Exception {
        // Default receive without criteria
        throw new AdapterException.OperationException(AdapterType.MAIL, 
                "Mail Receiver requires data payload for email operations");
    }
    
    private AdapterResult addToBatch(Object payload) throws Exception {
        synchronized (batchBuffer) {
            batchBuffer.add(payload);
            
            boolean shouldFlush = false;
            
            // Check size-based flushing
            if ("SIZE_BASED".equals(config.getBatchStrategy()) || "MIXED".equals(config.getBatchStrategy())) {
                if (config.getBatchSize() != null && batchBuffer.size() >= config.getBatchSize()) {
                    shouldFlush = true;
                }
            }
            
            // Check time-based flushing
            if ("TIME_BASED".equals(config.getBatchStrategy()) || "MIXED".equals(config.getBatchStrategy())) {
                long timeSinceLastFlush = System.currentTimeMillis() - lastBatchFlush;
                if (timeSinceLastFlush >= config.getBatchTimeoutMs()) {
                    shouldFlush = true;
                }
            }
            
            if (shouldFlush) {
                return flushBatch();
            } else {
                return AdapterResult.success(null, 
                        String.format("Added to batch (%d/%d items)", 
                                batchBuffer.size(), 
                                config.getBatchSize() != null ? config.getBatchSize() : "unlimited"));
            }
        }
    }
    
    private AdapterResult flushBatch() throws Exception {
        synchronized (batchBuffer) {
            if (batchBuffer.isEmpty()) {
                return AdapterResult.success(null, "No items in batch to flush");
            }
            
            List<Object> itemsToSend = new ArrayList<>(batchBuffer);
            batchBuffer.clear();
            lastBatchFlush = System.currentTimeMillis();
            
            return sendBatchEmail(itemsToSend);
        }
    }
    
    private AdapterResult sendBatchEmail(List<Object> items) throws Exception {
        // Create single email with all batch items
        StringBuilder batchContent = new StringBuilder();
        batchContent.append("Batch Email - ").append(items.size()).append(" items\n\n");
        
        for (int i = 0; i < items.size(); i++) {
            batchContent.append("Item ").append(i + 1).append(":\n");
            batchContent.append(convertToString(items.get(i)));
            batchContent.append("\n\n");
        }
        
        Map<String, Object> batchData = new HashMap<>();
        batchData.put("subject", "Batch Email - " + items.size() + " items");
        batchData.put("body", batchContent.toString());
        batchData.put("itemCount", items.size());
        batchData.put("batchId", batchCounter.incrementAndGet());
        
        return sendEmailMessage(batchData, true, items.size());
    }
    
    private AdapterResult sendEmail(Object payload) throws Exception {
        return sendEmailMessage(payload, false, 1);
    }
    
    private AdapterResult sendEmailMessage(Object payload, boolean isBatch, int itemCount) throws Exception {
        Transport transport = null;
        
        try {
            // Create email message
            MimeMessage message = createEmailMessage(payload);
            
            // Connect to SMTP server
            transport = mailSession.getTransport("smtp");
            transport.connect(config.getSmtpUsername(), config.getSmtpPassword());
            
            // Send message
            transport.sendMessage(message, message.getAllRecipients());
            
            String messageId = message.getMessageID();
            Address[] recipients = message.getAllRecipients();
            int recipientCount = recipients != null ? recipients.length : 0;
            
            logger.info("Mail receiver adapter sent email to {} recipients", recipientCount);
            
            String resultMessage = isBatch ? 
                    String.format("Successfully sent batch email with %d items to %d recipients", itemCount, recipientCount) :
                    String.format("Successfully sent email to %d recipients", recipientCount);
            
            AdapterResult result = AdapterResult.success(messageId, resultMessage);
            result.addMetadata("messageId", messageId);
            result.addMetadata("recipientCount", recipientCount);
            result.addMetadata("itemCount", itemCount);
            result.addMetadata("subject", message.getSubject());
            
            return result;
            
        } finally {
            if (transport != null && transport.isConnected()) {
                transport.close();
            }
        }
    }
    
    private MimeMessage createEmailMessage(Object payload) throws Exception {
        MimeMessage message = new MimeMessage(mailSession);
        
        // Extract email data from payload
        Map<String, Object> emailData = extractEmailData(payload);
        
        // Set sender
        String fromAddress = (String) emailData.getOrDefault("from", config.getFromAddress());
        if (fromAddress != null) {
            message.setFrom(new InternetAddress(fromAddress));
        }
        
        // Set recipients
        setRecipients(message, emailData);
        
        // Set subject
        String subject = (String) emailData.getOrDefault("subject", "Message from Integration Flow");
        if (config.getSubjectTemplate() != null) {
            subject = processTemplate(config.getSubjectTemplate(), emailData);
        }
        message.setSubject(subject, config.getMailEncoding());
        
        // Set content
        setMessageContent(message, emailData);
        
        // Set additional headers
        message.setSentDate(new Date());
        message.setReplyTo(InternetAddress.parse(fromAddress != null ? fromAddress : config.getFromAddress()));
        
        // Add custom headers if configured
        if (config.getCustomHeaders() != null && !config.getCustomHeaders().isEmpty()) {
            String[] headers = config.getCustomHeaders().split(",");
            for (String header : headers) {
                String[] keyValue = header.split(":");
                if (keyValue.length == 2) {
                    message.setHeader(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        
        return message;
    }
    
    private void setRecipients(MimeMessage message, Map<String, Object> emailData) throws Exception {
        // To recipients
        String toAddresses = (String) emailData.getOrDefault("to", config.getToAddress());
        if (toAddresses != null && !toAddresses.trim().isEmpty()) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddresses));
        }
        
        // CC recipients
        String ccAddresses = (String) emailData.getOrDefault("cc", config.getCcAddress());
        if (ccAddresses != null && !ccAddresses.trim().isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddresses));
        }
        
        // BCC recipients
        String bccAddresses = (String) emailData.getOrDefault("bcc", config.getBccAddress());
        if (bccAddresses != null && !bccAddresses.trim().isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bccAddresses));
        }
    }
    
    private void setMessageContent(MimeMessage message, Map<String, Object> emailData) throws Exception {
        String body = (String) emailData.getOrDefault("body", "");
        if (config.getEmailTemplate() != null) {
            body = processTemplate(config.getEmailTemplate(), emailData);
        }
        
        // Check for attachments
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) emailData.get("attachments");
        
        if (attachments != null && !attachments.isEmpty()) {
            // Create multipart message with attachments
            Multipart multipart = new MimeMultipart();
            
            // Add body part
            MimeBodyPart bodyPart = new MimeBodyPart();
            if (config.getContentType().toLowerCase().contains("html")) {
                bodyPart.setContent(body, "text/html; charset=" + config.getMailEncoding());
            } else {
                bodyPart.setText(body, config.getMailEncoding());
            }
            multipart.addBodyPart(bodyPart);
            
            // Add attachment parts
            for (Map<String, Object> attachment : attachments) {
                MimeBodyPart attachmentPart = createAttachmentPart(attachment);
                if (attachmentPart != null) {
                    multipart.addBodyPart(attachmentPart);
                }
            }
            
            message.setContent(multipart);
        } else {
            // Simple text/html message
            if (config.getContentType().toLowerCase().contains("html")) {
                message.setContent(body, "text/html; charset=" + config.getMailEncoding());
            } else {
                message.setText(body, config.getMailEncoding());
            }
        }
    }
    
    private MimeBodyPart createAttachmentPart(Map<String, Object> attachment) throws Exception {
        MimeBodyPart attachmentPart = new MimeBodyPart();
        
        String fileName = (String) attachment.get("fileName");
        String filePath = (String) attachment.get("filePath");
        byte[] content = (byte[]) attachment.get("content");
        
        if (filePath != null) {
            // File-based attachment
            DataSource source = new FileDataSource(filePath);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(fileName != null ? fileName : new File(filePath).getName());
        } else if (content != null) {
            // Content-based attachment
            DataSource source = new ByteArrayDataSource(content, "application/octet-stream");
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(fileName != null ? fileName : "attachment");
        } else {
            logger.warn("Attachment has neither file path nor content, skipping");
            return null;
        }
        
        return attachmentPart;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractEmailData(Object payload) throws Exception {
        if (payload instanceof Map) {
            return (Map<String, Object>) payload;
        }
        
        if (payload instanceof String) {
            Map<String, Object> data = new HashMap<>();
            data.put("body", payload);
            return data;
        }
        
        // For other types, convert to string
        Map<String, Object> data = new HashMap<>();
        data.put("body", convertToString(payload));
        return data;
    }
    
    private String convertToString(Object payload) throws Exception {
        if (payload == null) {
            return "";
        }
        
        if (payload instanceof String) {
            return (String) payload;
        }
        
        if (payload instanceof byte[]) {
            return new String((byte[]) payload, config.getMailEncoding());
        }
        
        return payload.toString();
    }
    
    private String processTemplate(String template, Map<String, Object> data) {
        if (template == null) {
            return "";
        }
        
        String result = template;
        
        // Replace common placeholders
        result = result.replace("${timestamp}", String.valueOf(System.currentTimeMillis()));
        result = result.replace("${datetime}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        result = result.replace("${date}", DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now()));
        result = result.replace("${time}", DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));
        
        // Replace data-specific placeholders
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (result.contains(placeholder)) {
                    result = result.replace(placeholder, String.valueOf(entry.getValue()));
                }
            }
        }
        
        return result;
    }
    
    private MimeMessage createTestMessage() throws Exception {
        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(config.getFromAddress()));
        message.setSubject("Test Message");
        message.setText("This is a test message");
        return message;
    }
    
    private void initializeMailSession() throws Exception {
        Properties props = new Properties();
        
        // SMTP configuration
        props.put("mail.smtp.host", config.getSmtpServerHost());
        props.put("mail.smtp.port", config.getSmtpServerPort());
        props.put("mail.smtp.auth", "true");
        
        // SSL/TLS configuration
        if (config.isUseSSLTLS()) {
            if ("465".equals(config.getSmtpServerPort())) {
                // SSL
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.ssl.trust", "*");
            } else {
                // STARTTLS
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
            }
        }
        
        // Timeouts
        props.put("mail.smtp.connectiontimeout", config.getConnectionTimeout());
        props.put("mail.smtp.timeout", config.getReadTimeout());
        
        // Create session
        mailSession = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpPassword());
            }
        });
        
        // Enable debug if configured
        if (config.isEnableDebug()) {
            mailSession.setDebug(true);
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getSmtpServerHost() == null || config.getSmtpServerHost().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.MAIL, "SMTP server host is required");
        }
        if (config.getSmtpUsername() == null || config.getSmtpUsername().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.MAIL, "SMTP username is required");
        }
        if (config.getSmtpPassword() == null) {
            throw new AdapterException.ConfigurationException(AdapterType.MAIL, "SMTP password is required");
        }
        if (config.getFromAddress() == null || config.getFromAddress().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.MAIL, "From address is required");
        }
    }
    
    @Override
    protected long getPollingIntervalMs() {
        // Mail receivers typically don't poll, they send emails
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("Mail Receiver (Outbound): %s:%s, User: %s, From: %s, Batching: %s", 
                config.getSmtpServerHost(),
                config.getSmtpServerPort(),
                config.getSmtpUsername(),
                config.getFromAddress(),
                config.isEnableBatching() ? "Enabled" : "Disabled");
    }
    
    // Helper class for byte array data source
    private static class ByteArrayDataSource implements DataSource {
        private final byte[] data;
        private final String contentType;
        
        public ByteArrayDataSource(byte[] data, String contentType) {
            this.data = data;
            this.contentType = contentType;
        }
        
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(data);
        }
        
        @Override
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException("Read-only data source");
        }
        
        @Override
        public String getContentType() {
            return contentType;
        }
        
        @Override
        public String getName() {
            return "ByteArrayDataSource";
        }
    }
}