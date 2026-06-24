package com.demo.cms.admin.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/cms/media")
@CrossOrigin(origins = "*")
@Slf4j
public class MediaController {

    private final Path uploadDir = Paths.get("/app/uploads");

    public MediaController() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            log.error("Could not create upload directory!", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Please select a file to upload."));
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = uploadDir.resolve(uniqueFileName);
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + uniqueFileName;
            
            log.info("Successfully uploaded file {} to {}", originalFileName, targetLocation);

            return ResponseEntity.ok(Map.of("url", fileUrl));

        } catch (IOException ex) {
            log.error("Could not store file.", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not store file. Please try again."));
        }
    }
}
