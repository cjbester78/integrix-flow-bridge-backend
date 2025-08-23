package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * SoapAdapterService - generated JavaDoc.
 */
public interface SoapAdapterService {
    String invoke(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}