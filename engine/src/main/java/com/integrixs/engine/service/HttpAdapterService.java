package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * HttpAdapterService - generated JavaDoc.
 */
public interface HttpAdapterService {
    String get(CommunicationAdapter adapter);
    void post(CommunicationAdapter adapter, String payload);
}