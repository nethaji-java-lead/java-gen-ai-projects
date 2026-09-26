package com.nethajirepo.fileupload.controller;

import com.nethajirepo.fileupload.dto.CreateFileRequest;
import com.nethajirepo.fileupload.entity.FileMetadata;
import com.nethajirepo.fileupload.service.FileMetadataService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final Path FILE_UPLOAD_DIRECTORY = Paths.get("uploads");

    private final FileMetadataService fileMetadataService;

    public FileUploadController(FileMetadataService fileMetadataService) {
        this.fileMetadataService = fileMetadataService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("File is empty");
            }

            Files.createDirectories(FILE_UPLOAD_DIRECTORY);

            String fileName = Paths.get(Objects.requireNonNull(file.getOriginalFilename()))
                    .getFileName()
                    .toString();

            Path targetPath = FILE_UPLOAD_DIRECTORY.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            CreateFileRequest createFileRequest =
                    new CreateFileRequest(fileName, "", targetPath.toString(), file.getSize());

            FileMetadata fileMetadata = fileMetadataService.createMetadata(createFileRequest);

            return ResponseEntity.ok(
                    "File uploaded successfully: " + fileMetadata.getFilePath()
            );

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("File upload failed: " + e.getMessage());
        }
    }


    @GetMapping("/download")
    public ResponseEntity<List<FileMetadata>> getAll() {

        return ResponseEntity.ok(
                fileMetadataService.getAll()
        );
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileName") String fileName) throws MalformedURLException {
        Path filePath = Paths.get(FILE_UPLOAD_DIRECTORY.toUri()).resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("File not found");
        }
    }
}
