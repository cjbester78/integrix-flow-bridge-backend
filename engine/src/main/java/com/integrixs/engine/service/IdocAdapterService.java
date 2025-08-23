package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * IdocAdapterService - generated JavaDoc.
 */
public interface IdocAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}