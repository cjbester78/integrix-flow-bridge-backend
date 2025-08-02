package com.integrationlab.engine.service;

import com.integrationlab.data.model.CommunicationAdapter;

/**
 * RestAdapterService - generated JavaDoc.
 */
public interface RestAdapterService {
    String get(CommunicationAdapter adapter);
    void post(CommunicationAdapter adapter, String payload);
}