package com.training.warehouse.common.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class EnvProvider {

    @Value("${JWT_SECRET_KEY}")
    private String jwtSecretKey;

    @Value("${JWT_EXPIRATION_TIME}")
    private long jwtExpirationTime;

    @Value("${MINIO_DOMAIN}")
    private String minioDomain;

    @Value("${MINIO_USERNAME}")
    private String minioUsername;

    @Value("${MINIO_PASSWORD}")
    private String minioPassword;
}
