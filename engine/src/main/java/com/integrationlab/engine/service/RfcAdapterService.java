package com.integrationlab.engine.service;

import com.integrationlab.model.CommunicationAdapter;

/**
 * RfcAdapterService - generated JavaDoc.
 */
public interface RfcAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}