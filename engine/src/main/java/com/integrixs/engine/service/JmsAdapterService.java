package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * JmsAdapterService - generated JavaDoc.
 */
public interface JmsAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}