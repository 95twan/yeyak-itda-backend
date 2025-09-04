package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.config.MinioConfig;
import com.rodemtree.yeyakitda.entity.ImageDomain;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@DisplayName("비즈니스 로직 - 이미지")
@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioConfig.MinioProperties minioProperties;

    @Test
    @DisplayName("성공 - 이미지 파일이 주어지면, 고유한 파일명을 생성하여 MinIO에 업로드하고 파일명을 반환한다.")
    void uploadImageTest() throws Exception {
        // Given
        String originalFilename = "test-image.jpg";
        String bucketName = "yeyak-itda";
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        MockMultipartFile mockFile = new MockMultipartFile("image", originalFilename, "image/jpeg", "test image content".getBytes());
        given(minioProperties.getBucketName()).willReturn(bucketName);
        given(minioProperties.getBaseUrl()).willReturn("http://localhost:9000");

        try (MockedStatic<UUID> mockedUuid = mockStatic(UUID.class)) {
            mockedUuid.when(UUID::randomUUID).thenReturn(uuid);

            String expectedObjectName = ImageDomain.REVIEW.getPath() + "/" + uuid + "-" + originalFilename;
            // When
            String result = imageService.upload(mockFile, ImageDomain.REVIEW);

            // Then
            assertThat(result).isEqualTo(minioProperties.getBaseUrl() + "/" + minioProperties.getBucketName() + "/" + expectedObjectName);

            ArgumentCaptor<PutObjectArgs> putObjectArgsCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
            then(minioClient).should().putObject(putObjectArgsCaptor.capture());
            PutObjectArgs putObjectArgs = putObjectArgsCaptor.getValue();

            assertThat(putObjectArgs.bucket()).isEqualTo(bucketName);
            assertThat(putObjectArgs.object()).isEqualTo(expectedObjectName);

        }

    }
}
