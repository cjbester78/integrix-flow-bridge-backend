package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * FtpAdapterService - generated JavaDoc.
 */
public interface FtpAdapterService {
    String download(CommunicationAdapter adapter);
    void upload(CommunicationAdapter adapter, String payload);
}