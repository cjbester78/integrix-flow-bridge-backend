package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * MailAdapterService - generated JavaDoc.
 */
public interface MailAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}