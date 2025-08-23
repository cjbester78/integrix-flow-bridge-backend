package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * JdbcAdapterService - generated JavaDoc.
 */
public interface JdbcAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}