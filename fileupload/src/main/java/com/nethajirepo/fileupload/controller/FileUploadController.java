package com.techie.springai.rag.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final Path uploadDirectory = Paths.get("uploads");

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("File is empty");
            }

            Files.createDirectories(uploadDirectory);

            String fileName = Paths.get(Objects.requireNonNull(file.getOriginalFilename()))
                    .getFileName()
                    .toString();

            Path targetPath = uploadDirectory.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return ResponseEntity.ok(
                    "File uploaded successfully: " + targetPath
            );

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("File upload failed: " + e.getMessage());
        }
    }
}
