package com.example.springai.controller;


import com.example.springai.service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file)
    {
        String remotePath = fileService.uploadFile(file);
        return ResponseEntity.ok(
                "File uploaded successfully: " + remotePath
        );
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable String fileName) {

        InputStream inputStream = fileService.downloadFile(fileName);

        InputStreamResource resource =
                new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
