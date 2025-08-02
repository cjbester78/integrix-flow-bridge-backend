package com.integrationlab.engine.service;

import com.integrationlab.data.model.CommunicationAdapter;

/**
 * JdbcAdapterService - generated JavaDoc.
 */
public interface JdbcAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}