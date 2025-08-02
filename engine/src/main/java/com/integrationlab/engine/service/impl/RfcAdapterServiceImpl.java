package com.integrationlab.engine.service.impl;

import com.integrationlab.engine.service.RfcAdapterService;
import com.integrationlab.data.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

@Service
/**
 * RfcAdapterServiceImpl - generated JavaDoc.
 */
public class RfcAdapterServiceImpl implements RfcAdapterService {
    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received RFC for adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        System.out.println("Sent RFC for adapter: " + adapter.getName());
    }
}