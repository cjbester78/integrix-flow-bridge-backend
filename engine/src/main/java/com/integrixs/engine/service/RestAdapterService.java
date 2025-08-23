package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * RestAdapterService - generated JavaDoc.
 */
public interface RestAdapterService {
    String get(CommunicationAdapter adapter);
    void post(CommunicationAdapter adapter, String payload);
}