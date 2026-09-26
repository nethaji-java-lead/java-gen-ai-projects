package com.nethajirepo.fileupload.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    private UUID id;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}