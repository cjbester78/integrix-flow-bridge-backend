package com.integrationlab.controller;

import com.integrationlab.backend.test.BaseIntegrationTest;
import com.integrationlab.backend.test.JwtTestUtils;
import com.integrationlab.model.Certificate;
import com.integrationlab.repository.CertificateRepository;
import com.integrationlab.shared.dto.certificate.CertificateUploadRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CertificateController.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
class CertificateControllerIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private CertificateRepository certificateRepository;
    
    @Value("${certificates.storage.path:/opt/integrixlab/certs}")
    private String certStoragePath;
    
    private Certificate testCertificate;
    private String authToken;
    
    @BeforeEach
    protected void setUp() {
        super.setUp();
        
        // Clean up database
        certificateRepository.deleteAll();
        
        // Create test certificate
        testCertificate = new Certificate();
        testCertificate.setName("Test Certificate");
        testCertificate.setFormat("PEM");
        testCertificate.setType("SSL");
        testCertificate.setUploadedBy("testuser");
        testCertificate.setFileName("test.crt");
        testCertificate.setContent("-----BEGIN CERTIFICATE-----\nMIIBkTCB+wIJAKHHIG...".getBytes());
        testCertificate.setUploadedAt(LocalDateTime.now());
        testCertificate = certificateRepository.save(testCertificate);
        
        // Generate auth token for secured endpoints
        authToken = JwtTestUtils.createAdminToken();
        
        // Ensure certificate storage directory exists
        File storageDir = new File(certStoragePath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }
    
    @Test
    void testUploadCertificate_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "new-cert.crt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "-----BEGIN CERTIFICATE-----\nNEW CERT CONTENT...".getBytes()
        );
        
        CertificateUploadRequestDTO metadata = new CertificateUploadRequestDTO();
        metadata.setName("New Certificate");
        metadata.setFormat("PEM");
        metadata.setType("SSL");
        metadata.setUploadedBy("admin");
        metadata.setPassword("certpassword");
        
        MockMultipartFile metadataFile = new MockMultipartFile(
                "metadata",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                toJson(metadata).getBytes()
        );
        
        // When & Then
        MvcResult result = mockMvc.perform(multipart("/api/certificates")
                .file(file)
                .file(metadataFile)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Certificate"))
                .andExpect(jsonPath("$.format").value("PEM"))
                .andExpect(jsonPath("$.type").value("SSL"))
                .andExpect(jsonPath("$.uploadedBy").value("admin"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        
        // Verify certificate was saved
        assertThat(certificateRepository.count()).isEqualTo(2);
        
        // Extract certificate ID from response
        String responseBody = result.getResponse().getContentAsString();
        String certId = objectMapper.readTree(responseBody).get("id").asText();
        
        // Verify file was written to disk
        Path certFile = Paths.get(certStoragePath, certId + ".crt");
        assertThat(Files.exists(certFile)).isTrue();
        assertThat(Files.readString(certFile)).isEqualTo("-----BEGIN CERTIFICATE-----\nNEW CERT CONTENT...");
        
        // Clean up
        Files.deleteIfExists(certFile);
    }
    
    @Test
    void testGetAllCertificates() throws Exception {
        // Given - Add another certificate
        Certificate cert2 = new Certificate();
        cert2.setName("Second Certificate");
        cert2.setFormat("DER");
        cert2.setType("CA");
        cert2.setUploadedBy("admin");
        cert2.setFileName("second.crt");
        cert2.setContent("DER content".getBytes());
        cert2.setUploadedAt(LocalDateTime.now());
        certificateRepository.save(cert2);
        
        // When & Then
        mockMvc.perform(get("/api/certificates")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }
    
    @Test
    void testDownloadCertificate_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/certificates/{id}/download", testCertificate.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.crt\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(testCertificate.getContent()));
    }
    
    @Test
    void testDownloadCertificate_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/certificates/{id}/download", "non-existent-id")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testDeleteCertificate_Success() throws Exception {
        // Given - Write file to disk
        Path certFile = Paths.get(certStoragePath, testCertificate.getId() + ".crt");
        Files.write(certFile, "test content".getBytes());
        assertThat(Files.exists(certFile)).isTrue();
        
        // When & Then
        mockMvc.perform(delete("/api/certificates/{id}", testCertificate.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // Verify certificate was deleted
        assertThat(certificateRepository.findById(testCertificate.getId())).isEmpty();
        assertThat(Files.exists(certFile)).isFalse();
    }
    
    @Test
    void testDeleteCertificate_FileNotExists() throws Exception {
        // Given - No file on disk
        Path certFile = Paths.get(certStoragePath, testCertificate.getId() + ".crt");
        assertThat(Files.exists(certFile)).isFalse();
        
        // When & Then - Should still succeed
        mockMvc.perform(delete("/api/certificates/{id}", testCertificate.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // Verify certificate was deleted
        assertThat(certificateRepository.findById(testCertificate.getId())).isEmpty();
    }
    
    @Test
    void testUploadCertificate_MissingFile() throws Exception {
        // Given
        CertificateUploadRequestDTO metadata = new CertificateUploadRequestDTO();
        metadata.setName("New Certificate");
        metadata.setFormat("PEM");
        metadata.setType("SSL");
        metadata.setUploadedBy("admin");
        
        MockMultipartFile metadataFile = new MockMultipartFile(
                "metadata",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                toJson(metadata).getBytes()
        );
        
        // When & Then
        mockMvc.perform(multipart("/api/certificates")
                .file(metadataFile)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testUploadCertificate_InvalidMetadata() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "new-cert.crt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "cert content".getBytes()
        );
        
        MockMultipartFile metadataFile = new MockMultipartFile(
                "metadata",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                "invalid json".getBytes()
        );
        
        // When & Then
        mockMvc.perform(multipart("/api/certificates")
                .file(file)
                .file(metadataFile)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetAllCertificates_EmptyList() throws Exception {
        // Given - Clear all certificates
        certificateRepository.deleteAll();
        
        // When & Then
        mockMvc.perform(get("/api/certificates")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}