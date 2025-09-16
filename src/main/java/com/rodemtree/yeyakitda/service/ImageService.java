package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.config.MinioConfig;
import com.rodemtree.yeyakitda.entity.ImageDomain;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final MinioClient minioClient;
    private final MinioConfig.MinioProperties minioProperties;

    public String upload(MultipartFile file, ImageDomain domain) {
        String originalFilename = file.getOriginalFilename();
        String objectName = domain.getPath() + "/" + UUID.randomUUID() + "-" + originalFilename;

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return minioProperties.getBaseUrl() + "/" + minioProperties.getBucketName() + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }

    }

}
