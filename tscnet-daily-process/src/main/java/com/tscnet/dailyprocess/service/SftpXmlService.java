package com.tscnet.dailyprocess.service;

import lombok.RequiredArgsConstructor;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SftpXmlService {

    private final SftpRemoteFileTemplate sftpRemoteFileTemplate;

    private static final String REMOTE_DIRECTORY = "filesToProcess";

    public List<String> readXmlFiles() {

        List<String> xmlFiles = new ArrayList<>();

        sftpRemoteFileTemplate.execute(session -> {

            SftpClient.DirEntry[] files = session.list(REMOTE_DIRECTORY);

            for (SftpClient.DirEntry file : files) {
                String fileName = file.getFilename();

                // Only XML files
                if (!fileName.toLowerCase().endsWith(".xml")) {
                    continue;
                }

                String remoteFile =
                        REMOTE_DIRECTORY + "/" + fileName;

                System.out.println(
                        "Reading SFTP file: " + remoteFile
                );

                try (InputStream inputStream =
                             session.readRaw(remoteFile)) {

                    String xml = new String(
                            inputStream.readAllBytes(),
                            StandardCharsets.UTF_8
                    );

                    xmlFiles.add(xml);

                    System.out.println(
                            "XML File: " + fileName
                    );

                    System.out.println(xml);
                }
            }

            return null;
        });

        return xmlFiles;
    }

    public void readXmlFile() {

        sftpRemoteFileTemplate.execute(session -> {

            System.out.println("Connected to SFTP");

            try {
                SftpClient.DirEntry[] files = session.list(".");

                System.out.println("Files in current directory:");

                for (SftpClient.DirEntry file : files) {
                    System.out.println("FILE = " + file.getFilename());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        });
    }
}