package com.integrixs.backend.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.integrixs.data.model.Certificate;
import com.integrixs.data.repository.CertificateRepository;
import com.integrixs.shared.dto.certificate.CertificateDTO;
import com.integrixs.shared.dto.certificate.CertificateUploadRequestDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;

    @Value("${certificates.storage.path:/opt/integrixlab/certs}")
    private String certStoragePath;

    public CertificateService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    public CertificateDTO saveCertificate(CertificateUploadRequestDTO dto, MultipartFile file) throws Exception {
        Certificate cert = new Certificate();
        cert.setName(dto.getName());
        cert.setFormat(dto.getFormat());
        cert.setType(dto.getType());
        cert.setPassword(dto.getPassword());
        cert.setUploadedBy(dto.getUploadedBy());
        cert.setFileName(file.getOriginalFilename());
        cert.setContent(file.getBytes());

        Certificate saved = certificateRepository.save(cert);

        writeFileToDisk(saved.getId(), file);

        return toDTO(saved);
    }

    public List<CertificateDTO> getAllCertificates() {
        return certificateRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<Certificate> getCertificateById(String id) {
        return certificateRepository.findById(id);
    }

    public void deleteCertificate(String id) throws Exception {
        certificateRepository.deleteById(id);
        File f = new File(certStoragePath + "/" + id + ".crt");
        if (f.exists()) {
            Files.delete(f.toPath());
        }
    }

    private void writeFileToDisk(String id, MultipartFile file) throws Exception {
        File dir = new File(certStoragePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File out = new File(dir, id + ".crt");
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(file.getBytes());
        }
    }

    private CertificateDTO toDTO(Certificate cert) {
        CertificateDTO dto = new CertificateDTO();
        dto.setId(cert.getId());
        dto.setName(cert.getName());
        dto.setFormat(cert.getFormat());
        dto.setType(cert.getType());
        dto.setUploadedBy(cert.getUploadedBy());
        dto.setUploadedAt(cert.getUploadedAt());
        return dto;
    }
}
