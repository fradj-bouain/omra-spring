package com.omra.platform.service;

import com.omra.platform.config.StorageProperties;
import com.omra.platform.exception.BadRequestException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    private final StorageProperties storageProperties;

    @Autowired(required = false)
    private MinioClient minioClient;

    public StorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Value("${storage.local-path:./uploads}")
    private String localPath;

    public String upload(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "file";
        }
        String ext = "";
        int i = originalName.lastIndexOf('.');
        if (i > 0) ext = originalName.substring(i);
        String objectName = (prefix != null && !prefix.isBlank() ? prefix + "/" : "") + UUID.randomUUID() + ext;

        if (minioClient != null) {
            return uploadToMinio(file, objectName);
        }
        return uploadToLocal(file, objectName);
    }

    private String uploadToMinio(MultipartFile file, String objectName) {
        ensureBucketExists();
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                            .build()
            );
        } catch (Exception e) {
            log.error("Upload failed", e);
            throw new BadRequestException("Upload failed: " + e.getMessage());
        }
        return String.format("%s/%s/%s", storageProperties.getEndpoint(), storageProperties.getBucket(), objectName);
    }

    private String uploadToLocal(MultipartFile file, String objectName) {
        try {
            Path dir = Paths.get(localPath, objectName).getParent();
            if (dir != null) Files.createDirectories(dir);
            Path target = Paths.get(localPath, objectName);
            file.transferTo(target.toFile());
            return "/uploads/" + objectName;
        } catch (Exception e) {
            log.error("Local upload failed", e);
            throw new BadRequestException("Upload failed: " + e.getMessage());
        }
    }

    private void ensureBucketExists() {
        if (minioClient == null) return;
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(storageProperties.getBucket()).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(storageProperties.getBucket()).build());
            }
        } catch (Exception e) {
            log.warn("Could not ensure bucket exists", e);
        }
    }
}
