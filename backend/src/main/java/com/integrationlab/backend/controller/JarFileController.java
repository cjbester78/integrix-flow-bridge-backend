package com.integrationlab.backend.controller;

import com.integrationlab.backend.service.JarFileService;
import com.integrationlab.shared.dto.JarFileDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jar-files")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JarFileController {
    
    private static final Logger logger = LoggerFactory.getLogger(JarFileController.class);
    
    private final JarFileService jarFileService;
    
    /**
     * Get all JAR files
     */
    @GetMapping
    public ResponseEntity<List<JarFileDTO>> getAllJarFiles() {
        logger.debug("Getting all JAR files");
        List<JarFileDTO> jarFiles = jarFileService.getAllJarFiles();
        return ResponseEntity.ok(jarFiles);
    }
    
    /**
     * Get JAR file by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<JarFileDTO> getJarFileById(@PathVariable String id) {
        logger.debug("Getting JAR file by id: {}", id);
        JarFileDTO jarFile = jarFileService.getJarFileById(id);
        return ResponseEntity.ok(jarFile);
    }
}