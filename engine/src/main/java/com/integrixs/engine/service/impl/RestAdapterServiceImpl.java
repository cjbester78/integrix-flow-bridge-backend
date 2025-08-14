package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.RestAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

@Service
/**
 * RestAdapterServiceImpl - generated JavaDoc.
 */
public class RestAdapterServiceImpl implements RestAdapterService {
    @Override
    public String get(CommunicationAdapter adapter) {
        return "GET from REST adapter: " + adapter.getName();
    }

    @Override
    public void post(CommunicationAdapter adapter, String payload) {
        System.out.println("POST to REST adapter: " + adapter.getName());
    }
}
