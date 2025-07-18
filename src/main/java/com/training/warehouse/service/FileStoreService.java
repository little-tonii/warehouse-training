package com.training.warehouse.service;

import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

public interface FileStoreService {

    public static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    public static final String INBOUND_BUCKET = "inbound-bucket";
    public static final String OUTBOUND_BUCKET = "outbound-bucket";

    void deleteFile(String bucketName, String filePath, String fileName);

    void uploadFile(String bucketName, String filePath, MultipartFile file);

    void uploadFile(String bucketName, String filePath, String fileName, byte[] file);

    byte[] getFile(String bucketName, String filePath, String fileName);

    public String getPresignedDownloadUrl(String bucketName, String filePath, String fileName);
}
