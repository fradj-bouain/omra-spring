package com.omra.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private String type = "local";
    private String endpoint = "http://localhost:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String bucket = "omra-files";
    private String region = "us-east-1";
    /** Dossier disque lorsque MinIO n’est pas utilisé ; défaut YAML = {@code ${user.home}/omra-uploads}. */
    private String localPath = "";
    /**
     * URL publique de l’API pour préfixer les chemins /uploads/... (ex. https://api.example.com).
     * Vide = déduit de la requête HTTP (Host / X-Forwarded-*).
     */
    private String publicBaseUrl = "";
}
