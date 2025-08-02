package com.integrationlab.engine.service;

import com.integrationlab.data.model.CommunicationAdapter;

/**
 * HttpAdapterService - generated JavaDoc.
 */
public interface HttpAdapterService {
    String get(CommunicationAdapter adapter);
    void post(CommunicationAdapter adapter, String payload);
}