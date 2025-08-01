package com.integrationlab.service;

import com.integrationlab.backend.test.BaseUnitTest;
import com.integrationlab.model.Certificate;
import com.integrationlab.repository.CertificateRepository;
import com.integrationlab.shared.dto.certificate.CertificateDTO;
import com.integrationlab.shared.dto.certificate.CertificateUploadRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CertificateService.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
class CertificateServiceTest extends BaseUnitTest {
    
    @Mock
    private CertificateRepository certificateRepository;
    
    @InjectMocks
    private CertificateService certificateService;
    
    @TempDir
    Path tempDir;
    
    private Certificate certificate;
    private CertificateUploadRequestDTO uploadRequest;
    private MultipartFile mockFile;
    
    @BeforeEach
    void setUp() {
        super.setUp();
        
        // Set the certificate storage path to temp directory
        ReflectionTestUtils.setField(certificateService, "certStoragePath", tempDir.toString());
        
        // Initialize test data
        certificate = new Certificate();
        certificate.setId("test-cert-id");
        certificate.setName("Test Certificate");
        certificate.setFormat("PEM");
        certificate.setType("SSL");
        certificate.setUploadedBy("testuser");
        certificate.setUploadedAt(LocalDateTime.now());
        certificate.setFileName("test.crt");
        certificate.setContent("test content".getBytes());
        
        uploadRequest = new CertificateUploadRequestDTO();
        uploadRequest.setName("Test Certificate");
        uploadRequest.setFormat("PEM");
        uploadRequest.setType("SSL");
        uploadRequest.setPassword("secret");
        uploadRequest.setUploadedBy("testuser");
        
        mockFile = mock(MultipartFile.class);
    }
    
    @Test
    void testSaveCertificate_Success() throws Exception {
        // Given
        when(mockFile.getOriginalFilename()).thenReturn("test.crt");
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);
        
        // When
        CertificateDTO result = certificateService.saveCertificate(uploadRequest, mockFile);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-cert-id");
        assertThat(result.getName()).isEqualTo("Test Certificate");
        assertThat(result.getFormat()).isEqualTo("PEM");
        assertThat(result.getType()).isEqualTo("SSL");
        
        // Verify certificate was saved
        ArgumentCaptor<Certificate> certCaptor = ArgumentCaptor.forClass(Certificate.class);
        verify(certificateRepository).save(certCaptor.capture());
        Certificate savedCert = certCaptor.getValue();
        assertThat(savedCert.getName()).isEqualTo("Test Certificate");
        assertThat(savedCert.getPassword()).isEqualTo("secret");
        
        // Verify file was written to disk
        File savedFile = new File(tempDir.toFile(), "test-cert-id.crt");
        assertThat(savedFile).exists();
        assertThat(Files.readString(savedFile.toPath())).isEqualTo("test content");
    }
    
    @Test
    void testSaveCertificate_FileIOException() throws Exception {
        // Given
        when(mockFile.getOriginalFilename()).thenReturn("test.crt");
        when(mockFile.getBytes()).thenThrow(new RuntimeException("IO Error"));
        
        // When/Then
        assertThatThrownBy(() -> certificateService.saveCertificate(uploadRequest, mockFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("IO Error");
        
        // Verify no certificate was saved
        verify(certificateRepository, never()).save(any());
    }
    
    @Test
    void testGetAllCertificates() {
        // Given
        Certificate cert1 = certificate;
        Certificate cert2 = new Certificate();
        cert2.setId("cert-2");
        cert2.setName("Certificate 2");
        cert2.setFormat("DER");
        cert2.setType("CA");
        cert2.setUploadedBy("admin");
        cert2.setUploadedAt(LocalDateTime.now());
        
        when(certificateRepository.findAll()).thenReturn(Arrays.asList(cert1, cert2));
        
        // When
        List<CertificateDTO> result = certificateService.getAllCertificates();
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("test-cert-id");
        assertThat(result.get(0).getName()).isEqualTo("Test Certificate");
        assertThat(result.get(1).getId()).isEqualTo("cert-2");
        assertThat(result.get(1).getName()).isEqualTo("Certificate 2");
    }
    
    @Test
    void testGetCertificateById_Found() {
        // Given
        when(certificateRepository.findById("test-cert-id")).thenReturn(Optional.of(certificate));
        
        // When
        Optional<Certificate> result = certificateService.getCertificateById("test-cert-id");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("test-cert-id");
        assertThat(result.get().getName()).isEqualTo("Test Certificate");
    }
    
    @Test
    void testGetCertificateById_NotFound() {
        // Given
        when(certificateRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        // When
        Optional<Certificate> result = certificateService.getCertificateById("non-existent");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void testDeleteCertificate_WithExistingFile() throws Exception {
        // Given
        String certId = "test-cert-id";
        File certFile = new File(tempDir.toFile(), certId + ".crt");
        Files.write(certFile.toPath(), "test content".getBytes());
        assertThat(certFile).exists();
        
        // When
        certificateService.deleteCertificate(certId);
        
        // Then
        verify(certificateRepository).deleteById(certId);
        assertThat(certFile).doesNotExist();
    }
    
    @Test
    void testDeleteCertificate_FileNotExists() throws Exception {
        // Given
        String certId = "test-cert-id";
        
        // When
        certificateService.deleteCertificate(certId);
        
        // Then
        verify(certificateRepository).deleteById(certId);
        // Should not throw exception even if file doesn't exist
    }
    
    @Test
    void testDeleteCertificate_FileDeleteFailure() throws Exception {
        // Given
        String certId = "test-cert-id";
        File certFile = new File(tempDir.toFile(), certId + ".crt");
        Files.write(certFile.toPath(), "test content".getBytes());
        certFile.setReadOnly(); // This might cause deletion to fail on some systems
        
        // When/Then
        // The method should handle file deletion failures gracefully
        assertThatCode(() -> certificateService.deleteCertificate(certId))
                .doesNotThrowAnyException();
        
        verify(certificateRepository).deleteById(certId);
    }
    
    @Test
    void testWriteFileToDisk_CreatesDirectory() throws Exception {
        // Given
        Path newDir = tempDir.resolve("newdir");
        ReflectionTestUtils.setField(certificateService, "certStoragePath", newDir.toString());
        
        when(mockFile.getOriginalFilename()).thenReturn("test.crt");
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);
        
        assertThat(newDir.toFile()).doesNotExist();
        
        // When
        certificateService.saveCertificate(uploadRequest, mockFile);
        
        // Then
        assertThat(newDir.toFile()).exists();
        assertThat(newDir.toFile()).isDirectory();
        File savedFile = new File(newDir.toFile(), "test-cert-id.crt");
        assertThat(savedFile).exists();
    }
}