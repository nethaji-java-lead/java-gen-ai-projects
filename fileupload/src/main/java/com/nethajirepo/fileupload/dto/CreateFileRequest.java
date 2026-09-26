package com.nethajirepo.fileupload.dto;

public record CreateFileRequest(
        String fileName,
        String contentType,
        String targetPath,
        Long fileSize
) {
}