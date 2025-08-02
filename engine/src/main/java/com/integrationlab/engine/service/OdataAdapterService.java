package com.integrationlab.engine.service;

import com.integrationlab.data.model.CommunicationAdapter;

/**
 * OdataAdapterService - generated JavaDoc.
 */
public interface OdataAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}