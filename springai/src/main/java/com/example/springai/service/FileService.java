package com.example.springai.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class FileService {

    private final SftpService sftpService;


    public FileService(SftpService sftpService) {
        this.sftpService = sftpService;
    }

    public String uploadFile(MultipartFile file) {

        // Business validations can go here

        validate(file);

        return sftpService.upload(file);
    }

    private void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

       /* if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "File size cannot exceed 10 MB"
            );
        }*/
    }

    public InputStream downloadFile(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }

        return sftpService.download(fileName);
    }


}
