package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.JdbcAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

@Service
/**
 * JdbcAdapterServiceImpl - generated JavaDoc.
 */
public class JdbcAdapterServiceImpl implements JdbcAdapterService {
    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received JDBC data for adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        System.out.println("Sent JDBC data for adapter: " + adapter.getName());
    }
}
