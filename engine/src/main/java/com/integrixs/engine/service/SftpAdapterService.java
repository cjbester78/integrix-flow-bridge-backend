package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * SftpAdapterService - generated JavaDoc.
 */
public interface SftpAdapterService {
    String download(CommunicationAdapter adapter);
    void upload(CommunicationAdapter adapter, String payload);
}