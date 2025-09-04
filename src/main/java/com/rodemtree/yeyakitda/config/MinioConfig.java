package com.rodemtree.yeyakitda.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioConfig.MinioProperties.class)
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Getter
    @RequiredArgsConstructor
    @ConfigurationProperties(prefix = "minio")
    public static class MinioProperties {
        private final String baseUrl;
        private final String endpoint;
        private final String accessKey;
        private final String secretKey;
        private final String bucketName;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}
