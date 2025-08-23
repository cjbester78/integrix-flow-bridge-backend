package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.MailAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
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
