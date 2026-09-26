package com.nethajirepo.fileupload.service;

import com.nethajirepo.fileupload.dto.CreateFileRequest;
import com.nethajirepo.fileupload.entity.FileMetadata;
import com.nethajirepo.fileupload.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FileMetadataService {

    private final FileMetadataRepository repository;

    public FileMetadataService(FileMetadataRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FileMetadata createMetadata(CreateFileRequest request) {

        UUID fileId = UUID.randomUUID();

        String objectKey =
                "uploads/" + fileId + "/" + request.fileName();

        FileMetadata metadata = new FileMetadata();

        metadata.setId(fileId);
        metadata.setOriginalName(request.fileName());
        metadata.setFilePath(request.targetPath());
        metadata.setContentType(request.contentType());
        metadata.setFileSize(request.fileSize());
        metadata.setStatus("INITIATED");

        return repository.save(metadata);
    }


    public String getFileUrl(String fileName) {

        FileMetadata metadata =
                repository.findByOriginalName(fileName)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "File not found: " + fileName
                                ));

        return metadata.getFilePath();
    }

    public List<FileMetadata> getAll() {
        return repository.findAll();
    }
}