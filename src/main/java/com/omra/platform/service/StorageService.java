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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
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

    private static final Set<String> BRANDING_IMAGE_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp"
    );
    private static final Set<String> FAVICON_TYPES = Set.of(
            "image/png", "image/jpeg", "image/x-icon", "image/vnd.microsoft.icon"
    );

    public String upload(MultipartFile file, String prefix) {
        return upload(file, prefix, null);
    }

    /**
     * @param uploadType ex. {@code branding-logo}, {@code branding-favicon}, {@code document} — contrôle MIME optionnel
     */
    public String upload(MultipartFile file, String prefix, String uploadType) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided");
        }
        validateMimeIfNeeded(file, uploadType);
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

    private void validateMimeIfNeeded(MultipartFile file, String uploadType) {
        if (!StringUtils.hasText(uploadType)) {
            return;
        }
        String t = uploadType.toLowerCase(Locale.ROOT);
        String ct = file.getContentType();
        String mime = ct != null ? ct.toLowerCase(Locale.ROOT).trim() : "";
        if (t.equals("branding-logo")) {
            if (!BRANDING_IMAGE_TYPES.contains(mime)) {
                throw new BadRequestException("Logo: use PNG, JPEG, GIF or WebP");
            }
        } else if (t.equals("branding-favicon")) {
            if (!FAVICON_TYPES.contains(mime)) {
                throw new BadRequestException("Favicon: use PNG, ICO or JPEG");
            }
        }
    }

    private String uploadToLocal(MultipartFile file, String objectName) {
        try {
            Path base = resolveLocalStorageRoot();
            Path target = resolveUnderBase(base, objectName);
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + objectName.replace('\\', '/');
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Local upload failed", e);
            throw new BadRequestException("Upload failed: " + e.getMessage());
        }
    }

    /** Répertoire racine des fichiers (toujours absolu, indépendant du cwd Tomcat). */
    public Path resolveLocalStorageRoot() {
        String configured = storageProperties.getLocalPath();
        if (!StringUtils.hasText(configured)) {
            return Paths.get(System.getProperty("user.home", "."), "omra-uploads").toAbsolutePath().normalize();
        }
        return Paths.get(configured).toAbsolutePath().normalize();
    }

    private static Path resolveUnderBase(Path base, String objectName) {
        Path target = base;
        for (String segment : objectName.replace('\\', '/').split("/")) {
            if (segment.isEmpty()) {
                continue;
            }
            if (".".equals(segment) || "..".equals(segment)) {
                throw new BadRequestException("Invalid storage path");
            }
            target = target.resolve(segment);
        }
        if (!target.normalize().startsWith(base.normalize())) {
            throw new BadRequestException("Invalid storage path");
        }
        return target;
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
