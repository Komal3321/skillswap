package com.skillswap.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import com.skillswap.common.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Local filesystem implementation of file storage.
 */
@Service
public class LocalFileStorageService implements FileStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf");

    private final Path uploadRoot;
    private final String publicPath;

    public LocalFileStorageService(FileStorageProperties properties) {
        this.uploadRoot = Paths.get(properties.uploadDir()).toAbsolutePath().normalize();
        this.publicPath = trimTrailingSlash(properties.publicPath());
    }

    @Override
    public String store(MultipartFile file, String folder) {
        validate(file);
        String safeFolder = sanitizeFolder(folder);
        String extension = resolveExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;
        Path targetDirectory = uploadRoot.resolve(safeFolder).normalize();
        Path targetFile = targetDirectory.resolve(fileName).normalize();

        if (!targetFile.startsWith(uploadRoot)) {
            throw new BadRequestException("Invalid upload path");
        }

        try {
            Files.createDirectories(targetDirectory);
            file.transferTo(targetFile);
            return publicPath + "/" + safeFolder + "/" + fileName;
        } catch (IOException exception) {
            throw new BadRequestException("Could not store uploaded file");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Uploaded file must be 5 MB or smaller");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Unsupported file type");
        }
    }

    private String sanitizeFolder(String folder) {
        String value = folder == null ? "" : folder.trim().toLowerCase();
        if (!value.matches("[a-z0-9-]+")) {
            throw new BadRequestException("Invalid upload folder");
        }
        return value;
    }

    private String resolveExtension(String originalFilename) {
        String filename = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase();
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
