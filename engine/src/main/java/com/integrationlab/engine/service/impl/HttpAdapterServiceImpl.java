package com.integrationlab.engine.service.impl;

import com.integrationlab.engine.service.HttpAdapterService;
import com.integrationlab.data.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

@Service
/**
 * HttpAdapterServiceImpl - generated JavaDoc.
 */
public class HttpAdapterServiceImpl implements HttpAdapterService {
    @Override
    public String get(CommunicationAdapter adapter) {
        return "GET from HTTP adapter: " + adapter.getName();
    }

    @Override
    public void post(CommunicationAdapter adapter, String payload) {
        System.out.println("POST to HTTP adapter: " + adapter.getName());
    }
}
