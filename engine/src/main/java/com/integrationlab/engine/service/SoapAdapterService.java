package com.integrationlab.engine.service;

import com.integrationlab.model.CommunicationAdapter;

/**
 * SoapAdapterService - generated JavaDoc.
 */
public interface SoapAdapterService {
    String invoke(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}