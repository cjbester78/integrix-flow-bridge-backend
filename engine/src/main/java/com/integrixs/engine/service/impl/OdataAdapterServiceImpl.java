package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.OdataAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

@Service
/**
 * OdataAdapterServiceImpl - generated JavaDoc.
 */
public class OdataAdapterServiceImpl implements OdataAdapterService {
    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received OData for adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        System.out.println("Sent OData for adapter: " + adapter.getName());
    }
}
