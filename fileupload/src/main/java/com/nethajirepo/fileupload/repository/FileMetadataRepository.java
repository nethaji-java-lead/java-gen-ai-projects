package com.nethajirepo.fileupload.repository;

import com.nethajirepo.fileupload.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileMetadataRepository
        extends JpaRepository<FileMetadata, UUID> {

    Optional<FileMetadata> findByOriginalName(String originalName);
}