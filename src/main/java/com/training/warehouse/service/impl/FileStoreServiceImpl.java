package com.training.warehouse.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.minio.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.training.warehouse.common.provider.EnvProvider;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import com.training.warehouse.service.FileStoreService;

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
            minioClient.statObject( //kiểm tra tồn tại file
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void uploadFile(String bucketName, String filePath, MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID);
        }
        String cleanedFileName = Paths.get(originalFileName).getFileName().toString();
        if (!cleanedFileName.matches("^[a-zA-Z0-9._-]{1,255}$")) {
            throw new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID);
        }
        if (filePath.contains("..")) {
            throw new BadRequestException(ExceptionMessage.FILE_PATH_IS_NOT_VALID);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException(ExceptionMessage.FILETYPE_NOT_ALLOWED);
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
            throw new RuntimeException(e.getMessage());
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
            throw new RuntimeException(e.getMessage());
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
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void uploadFile(String bucketName, String filePath, String fileName, byte[] file) {
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID);
        }
        String cleanedFileName = Paths.get(fileName).getFileName().toString();
        if (!cleanedFileName.matches("^[a-zA-Z0-9._-]{1,255}$")) {
            throw new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID);
        }
        if (filePath.contains("..")) {
            throw new BadRequestException(ExceptionMessage.FILE_PATH_IS_NOT_VALID);
        }
        String contentType = URLConnection.guessContentTypeFromName(cleanedFileName);
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException(ExceptionMessage.FILETYPE_NOT_ALLOWED);
        }
        String objectName = filePath.endsWith("/") ? filePath + cleanedFileName : filePath + "/" + cleanedFileName;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(file)) {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(bais, file.length, -1)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
