package com.integrationlab.engine.service;

import com.integrationlab.data.model.CommunicationAdapter;

/**
 * FtpAdapterService - generated JavaDoc.
 */
public interface FtpAdapterService {
    String download(CommunicationAdapter adapter);
    void upload(CommunicationAdapter adapter, String payload);
}