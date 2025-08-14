package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.SftpAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

@Service
/**
 * SftpAdapterServiceImpl - generated JavaDoc.
 */
public class SftpAdapterServiceImpl implements SftpAdapterService {
    @Override
    public String download(CommunicationAdapter adapter) {
        return "Downloaded SFTP data for adapter: " + adapter.getName();
    }

    @Override
    public void upload(CommunicationAdapter adapter, String payload) {
        System.out.println("Uploaded to SFTP adapter: " + adapter.getName());
    }
}
