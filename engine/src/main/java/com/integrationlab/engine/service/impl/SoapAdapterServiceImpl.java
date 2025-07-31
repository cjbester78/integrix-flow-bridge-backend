package com.integrationlab.engine.service.impl;

import com.integrationlab.engine.service.SoapAdapterService;
import com.integrationlab.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

@Service
/**
 * SoapAdapterServiceImpl - generated JavaDoc.
 */
public class SoapAdapterServiceImpl implements SoapAdapterService {
    @Override
    public String invoke(CommunicationAdapter adapter) {
        return "Invoked SOAP adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        System.out.println("Sent to SOAP adapter: " + adapter.getName());
    }
}
