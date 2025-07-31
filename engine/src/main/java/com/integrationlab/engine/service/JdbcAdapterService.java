package com.integrationlab.engine.service;

import com.integrationlab.model.CommunicationAdapter;

/**
 * JdbcAdapterService - generated JavaDoc.
 */
public interface JdbcAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}