package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * RfcAdapterService - generated JavaDoc.
 */
public interface RfcAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}