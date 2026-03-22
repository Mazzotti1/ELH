package com.elh.ingestor.service;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Retry(name = "s3-upload")
    public void upload(String key, byte[] data, String contentType) {
        log.debug("Uploading {} ({} bytes) to s3://{}/{}", contentType, data.length, bucket, key);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        log.info("Upload concluido: s3://{}/{}", bucket, key);
    }

    public String generatePermanentUrl(String key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build())
                .build();

        URL url = s3Presigner.presignGetObject(presignRequest).url();
        return url.toString();
    }

    public String buildS3Key(String guildId, String mediaType, String fileName) {
        return String.format("%s/%s/%s/%s",
                guildId, mediaType.toLowerCase(), java.time.LocalDate.now(), fileName);
    }

    public String buildThumbnailKey(String originalKey) {
        int lastSlash = originalKey.lastIndexOf('/');
        return originalKey.substring(0, lastSlash) + "/thumb_" + originalKey.substring(lastSlash + 1);
    }
}
