package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * OdataAdapterService - generated JavaDoc.
 */
public interface OdataAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}