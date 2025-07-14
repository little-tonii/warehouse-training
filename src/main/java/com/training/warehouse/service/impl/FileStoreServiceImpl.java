package com.training.warehouse.service.impl;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.training.warehouse.common.provider.EnvProvider;
import com.training.warehouse.exception.FileNameIsNotValidException;
import com.training.warehouse.exception.FilePathIsNotValidException;
import com.training.warehouse.exception.FileTypeNotAllowedException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import com.training.warehouse.service.FileStoreService;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;

@Service
public class FileStoreServiceImpl implements FileStoreService {
    private final EnvProvider envProvider;
    private final MinioClient minioClient;

    public FileStoreServiceImpl(EnvProvider envProvider) {
        this.envProvider = envProvider;
        this.minioClient = MinioClient.builder()
                .endpoint("https://" + this.envProvider.getMinioDomain())
                .credentials(this.envProvider.getMinioUsername(), this.envProvider.getMinioPassword())
                .build();
    }

    @Override
    public void deleteFile(String bucketName, String filePath, String fileName) {
        String objectName = filePath.endsWith("/") ? filePath + fileName : filePath + "/" + fileName;
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(ExceptionMessage.UNKNOWN);
        }
    }

    @Override
    public void uploadFile(String bucketName, String filePath, MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new FileNameIsNotValidException(ExceptionMessage.FILENAME_IS_NOT_VALID);
        }
        String cleanedFileName = Paths.get(originalFileName).getFileName().toString();
        if (!cleanedFileName.matches("^[a-zA-Z0-9._-]{1,255}$")) {
            throw new FileNameIsNotValidException(ExceptionMessage.FILENAME_IS_NOT_VALID);
        }
        if (filePath.contains("..")) {
            throw new FilePathIsNotValidException(ExceptionMessage.FILE_PATH_IS_NOT_VALID);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileTypeNotAllowedException(ExceptionMessage.FILETYPE_NOT_ALLOWED);
        }
        String objectName = filePath.endsWith("/") ? filePath + cleanedFileName : filePath + "/" + cleanedFileName;
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(ExceptionMessage.UNKNOWN);
        }
    }

    @Override
    public byte[] getFile(String bucketName, String filePath, String fileName) {
        String objectName = filePath.endsWith("/") ? filePath + fileName : filePath + "/" + fileName;
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(ExceptionMessage.UNKNOWN);
        }
    }

    @Override
    public String getPresignedDownloadUrl(String bucketName, String filePath, String fileName) {
        String objectName = filePath.endsWith("/") ? filePath + fileName : filePath + "/" + fileName;
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(5, TimeUnit.MINUTES)
                            .extraQueryParams(
                                    Map.of("response-content-disposition", "attachment; filename=\"" + fileName + "\""))
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(ExceptionMessage.UNKNOWN);
        }
    }

}
