package com.integrationlab.engine.service.impl;

import com.integrationlab.engine.service.MailAdapterService;
import com.integrationlab.data.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

@Service
/**
 * MailAdapterServiceImpl - generated JavaDoc.
 */
public class MailAdapterServiceImpl implements MailAdapterService {
    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received email from adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        System.out.println("Sent email from adapter: " + adapter.getName());
    }
}
