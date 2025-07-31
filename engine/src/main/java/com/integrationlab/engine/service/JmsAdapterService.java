package com.integrationlab.engine.service;

import com.integrationlab.model.CommunicationAdapter;

/**
 * JmsAdapterService - generated JavaDoc.
 */
public interface JmsAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}