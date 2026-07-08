package com.skillswap.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Stores user-uploaded files and returns a URL or path that can be persisted.
 */
public interface FileStorageService {

    /**
     * Stores a file under a logical folder.
     *
     * @param file uploaded file
     * @param folder logical folder name
     * @return stored file URL
     */
    String store(MultipartFile file, String folder);
}
