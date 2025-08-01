package com.integrationlab.adapters.factory;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of AdapterFactory that creates adapter instances
 * based on configuration objects.
 */
public class DefaultAdapterFactory implements AdapterFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultAdapterFactory.class);
    
    @Override
    public SenderAdapter createSender(AdapterType adapterType, Object configuration) throws AdapterException {
        logger.debug("Creating sender adapter for type: {}", adapterType);
        
        validateConfiguration(adapterType, configuration);
        
        try {
            SenderAdapter adapter = switch (adapterType) {
                case HTTP -> createHttpSender(configuration);
                case JDBC -> createJdbcSender(configuration);
                case REST -> createRestSender(configuration);
                case SOAP -> createSoapSender(configuration);
                case FILE -> createFileSender(configuration);
                case MAIL -> createMailSender(configuration);
                case FTP -> createFtpSender(configuration);
                case SFTP -> createSftpSender(configuration);
                case RFC -> createRfcSender(configuration);
                case IDOC -> createIdocSender(configuration);
                case JMS -> createJmsSender(configuration);
                case ODATA -> createOdataSender(configuration);
                default -> throw new AdapterException.ConfigurationException(adapterType, 
                        "Unsupported sender adapter type: " + adapterType);
            };
            
            logger.debug("Successfully created sender adapter for type: {}", adapterType);
            return adapter;
            
        } catch (ClassCastException e) {
            throw new AdapterException.ConfigurationException(adapterType, 
                    "Invalid configuration type for " + adapterType + " sender", e);
        } catch (Exception e) {
            logger.error("Failed to create sender adapter for type: {}", adapterType, e);
            throw new AdapterException(adapterType, AdapterMode.SENDER, 
                    "Failed to create sender adapter", e);
        }
    }
    
    @Override
    public ReceiverAdapter createReceiver(AdapterType adapterType, Object configuration) throws AdapterException {
        logger.debug("Creating receiver adapter for type: {}", adapterType);
        
        validateConfiguration(adapterType, configuration);
        
        try {
            ReceiverAdapter adapter = switch (adapterType) {
                case HTTP -> createHttpReceiver(configuration);
                case JDBC -> createJdbcReceiver(configuration);
                case REST -> createRestReceiver(configuration);
                case SOAP -> createSoapReceiver(configuration);
                case FILE -> createFileReceiver(configuration);
                case MAIL -> createMailReceiver(configuration);
                case FTP -> createFtpReceiver(configuration);
                case SFTP -> createSftpReceiver(configuration);
                case RFC -> createRfcReceiver(configuration);
                case IDOC -> createIdocReceiver(configuration);
                case JMS -> createJmsReceiver(configuration);
                case ODATA -> createOdataReceiver(configuration);
                default -> throw new AdapterException.ConfigurationException(adapterType, 
                        "Unsupported receiver adapter type: " + adapterType);
            };
            
            logger.debug("Successfully created receiver adapter for type: {}", adapterType);
            return adapter;
            
        } catch (ClassCastException e) {
            throw new AdapterException.ConfigurationException(adapterType, 
                    "Invalid configuration type for " + adapterType + " receiver", e);
        } catch (Exception e) {
            logger.error("Failed to create receiver adapter for type: {}", adapterType, e);
            throw new AdapterException(adapterType, AdapterMode.RECEIVER, 
                    "Failed to create receiver adapter", e);
        }
    }
    
    @Override
    public boolean supports(AdapterType adapterType, AdapterMode adapterMode) {
        // This factory supports all adapter types and modes
        return adapterType != null && adapterMode != null;
    }
    
    @Override
    public String getFactoryName() {
        return "DefaultAdapterFactory";
    }
    
    private void validateConfiguration(AdapterType adapterType, Object configuration) throws AdapterException {
        if (adapterType == null) {
            throw new AdapterException.ConfigurationException(null, "Adapter type cannot be null");
        }
        if (configuration == null) {
            throw new AdapterException.ConfigurationException(adapterType, "Configuration cannot be null");
        }
    }
    
    // Factory methods for sender adapters - these will be implemented as we build each adapter
    
    private SenderAdapter createHttpSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.HttpSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                    "HTTP sender requires HttpSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.HttpSenderAdapter(
                (com.integrationlab.adapters.config.HttpSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createJdbcSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.JdbcSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, 
                    "JDBC sender requires JdbcSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.JdbcSenderAdapter(
                (com.integrationlab.adapters.config.JdbcSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createRestSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.RestSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.REST, 
                    "REST sender requires RestSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.RestSenderAdapter(
                (com.integrationlab.adapters.config.RestSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createSoapSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.SoapSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.SOAP, 
                    "SOAP sender requires SoapSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.SoapSenderAdapter(
                (com.integrationlab.adapters.config.SoapSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createFileSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.FileSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                    "File sender requires FileSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.FileSenderAdapter(
                (com.integrationlab.adapters.config.FileSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createMailSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.MailSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.MAIL, 
                    "Mail sender requires MailSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.MailSenderAdapter(
                (com.integrationlab.adapters.config.MailSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createFtpSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.FtpSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.FTP, 
                    "FTP sender requires FtpSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.FtpSenderAdapter(
                (com.integrationlab.adapters.config.FtpSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createSftpSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.SftpSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.SFTP, 
                    "SFTP sender requires SftpSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.SftpSenderAdapter(
                (com.integrationlab.adapters.config.SftpSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createRfcSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.RfcSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.RFC, 
                    "RFC sender requires RfcSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.RfcSenderAdapter(
                (com.integrationlab.adapters.config.RfcSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createIdocSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.IdocSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, 
                    "IDOC sender requires IdocSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.IdocSenderAdapter(
                (com.integrationlab.adapters.config.IdocSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createJmsSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.JmsSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.JMS, 
                    "JMS sender requires JmsSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.JmsSenderAdapter(
                (com.integrationlab.adapters.config.JmsSenderAdapterConfig) configuration);
    }
    
    private SenderAdapter createOdataSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.OdataSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.ODATA, 
                    "ODATA sender requires OdataSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.OdataSenderAdapter(
                (com.integrationlab.adapters.config.OdataSenderAdapterConfig) configuration);
    }
    
    // Factory methods for receiver adapters - these will be implemented as we build each adapter
    
    private ReceiverAdapter createHttpReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.HttpReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                    "HTTP receiver requires HttpReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.HttpReceiverAdapter(
                (com.integrationlab.adapters.config.HttpReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createJdbcReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.JdbcReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.JDBC, 
                    "JDBC receiver requires JdbcReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.JdbcReceiverAdapter(
                (com.integrationlab.adapters.config.JdbcReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createRestReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.RestReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.REST, 
                    "REST receiver requires RestReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.RestReceiverAdapter(
                (com.integrationlab.adapters.config.RestReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createSoapReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.SoapReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.SOAP, 
                    "SOAP receiver requires SoapReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.SoapReceiverAdapter(
                (com.integrationlab.adapters.config.SoapReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createFileReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.FileReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.FILE, 
                    "File receiver requires FileReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.FileReceiverAdapter(
                (com.integrationlab.adapters.config.FileReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createMailReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.MailReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.MAIL, 
                    "Mail receiver requires MailReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.MailReceiverAdapter(
                (com.integrationlab.adapters.config.MailReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createFtpReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.FtpReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.FTP, 
                    "FTP receiver requires FtpReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.FtpReceiverAdapter(
                (com.integrationlab.adapters.config.FtpReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createSftpReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.SftpReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.SFTP, 
                    "SFTP receiver requires SftpReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.SftpReceiverAdapter(
                (com.integrationlab.adapters.config.SftpReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createRfcReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.RfcReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.RFC, 
                    "RFC receiver requires RfcReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.RfcReceiverAdapter(
                (com.integrationlab.adapters.config.RfcReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createIdocReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.IdocReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, 
                    "IDOC receiver requires IdocReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.IdocReceiverAdapter(
                (com.integrationlab.adapters.config.IdocReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createJmsReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.JmsReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.JMS, 
                    "JMS receiver requires JmsReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.JmsReceiverAdapter(
                (com.integrationlab.adapters.config.JmsReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapter createOdataReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrationlab.adapters.config.OdataReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterType.ODATA, 
                    "ODATA receiver requires OdataReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrationlab.adapters.impl.OdataReceiverAdapter(
                (com.integrationlab.adapters.config.OdataReceiverAdapterConfig) configuration);
    }
}