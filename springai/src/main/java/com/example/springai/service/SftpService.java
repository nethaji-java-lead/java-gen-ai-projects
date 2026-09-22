package com.example.springai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class SftpService {

    private final SessionFactory<?> sessionFactory;

    @Value("${sftp.remote-directory}")
    private String remoteDirectory;

    public SftpService(SessionFactory<?> sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String remotePath = remoteDirectory + "/" + fileName;

        try (InputStream inputStream = file.getInputStream(); Session<?> session = sessionFactory.getSession()) {

            session.write(inputStream, remotePath);

            return remotePath;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to SFTP: " + fileName, e);
        }
    }

    public InputStream download(String fileName) {

        String remotePath = remoteDirectory + "/" + fileName;

        try {
            Session<?> session = sessionFactory.getSession();

            InputStream inputStream = session.readRaw(remotePath);

            return new FilterInputStream(inputStream) {
                @Override
                public void close() throws IOException {
                    super.close();
                    session.close();
                }
            };

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to download file: " + fileName, e);
        }
    }
}
