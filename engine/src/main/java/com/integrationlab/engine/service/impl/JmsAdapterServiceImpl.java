package com.integrationlab.engine.service.impl;

import com.integrationlab.engine.service.JmsAdapterService;
import com.integrationlab.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

@Service
/**
 * JmsAdapterServiceImpl - generated JavaDoc.
 */
public class JmsAdapterServiceImpl implements JmsAdapterService {
    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received JMS message for adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        System.out.println("Sent JMS message for adapter: " + adapter.getName());
    }
}
