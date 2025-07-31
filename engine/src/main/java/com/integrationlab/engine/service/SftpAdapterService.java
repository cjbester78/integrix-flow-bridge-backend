package com.integrationlab.engine.service;

import com.integrationlab.model.CommunicationAdapter;

/**
 * SftpAdapterService - generated JavaDoc.
 */
public interface SftpAdapterService {
    String download(CommunicationAdapter adapter);
    void upload(CommunicationAdapter adapter, String payload);
}