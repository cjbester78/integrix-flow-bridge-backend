package com.integrationlab.engine.service;

import com.integrationlab.data.model.CommunicationAdapter;

/**
 * IdocAdapterService - generated JavaDoc.
 */
public interface IdocAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}