package com.skillswap.service.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for local file storage.
 */
@ConfigurationProperties(prefix = "app.file-storage")
public record FileStorageProperties(
        String uploadDir,
        String publicPath) {

    public FileStorageProperties {
        uploadDir = uploadDir == null || uploadDir.isBlank() ? "uploads" : uploadDir;
        publicPath = publicPath == null || publicPath.isBlank() ? "/uploads" : publicPath;
    }
}
