package com.integrationlab.engine.service.impl;

import com.integrationlab.engine.service.IdocAdapterService;
import com.integrationlab.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

@Service
/**
 * IdocAdapterServiceImpl - generated JavaDoc.
 */
public class IdocAdapterServiceImpl implements IdocAdapterService {
    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received IDoc for adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        System.out.println("Sent IDoc for adapter: " + adapter.getName());
    }
}
