package iam.jsoner.service;

import iam.jsoner.config.AppConfig;
import iam.jsoner.exception.ResourceNotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public record FileService(S3Client s3Client, AppConfig appConfig) {

    private static final Resource fileResource;

    // Initialize the fileResource before any FileService method is called
    static {
        fileResource = new FileSystemResource("src/main/resources/jsonerfile.json");
    }

    public String uploadFile() {
        // check if the file exists
        if (!fileResource.exists()) {
            throw new RuntimeException("The file to be uploaded does not exist");
        }
        try {
            InputStream inputStream = fileResource.getInputStream();
            PutObjectResponse response = s3Client.putObject(PutObjectRequest.builder()
                            .bucket(appConfig.getBucketName())
                            .key(fileResource.getFilename())
                            .build(),
                    RequestBody.fromInputStream(inputStream, fileResource.contentLength()));
            log.info("Object with eTag[{}] was uploaded", response.eTag());
            return "File uploaded successfully";
        } catch (IOException e) {
            throw new RuntimeException("Error while uploading file");
        }
    }


    public ResponseInputStream<GetObjectResponse> getObjectResponseInputStream(String objectKey) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(appConfig.getBucketName())
                .key(objectKey)
                .build();
        try {
            return s3Client.getObject(objectRequest);
        } catch (S3Exception e) {
            // Handle when the object does not exist
            if ("NoSuchKey".equals(e.awsErrorDetails().errorCode())) {
                log.warn("The object with key[{}] does not exist", objectKey);
                throw new ResourceNotFound("Resource does not exist");
            } else {
                // Handle other S3 exceptions
                log.error("Error downloading object with key[{}]. Exception Message [{}]", objectKey, e.getMessage());
                throw new RuntimeException("Error downloading resource: " + e.getMessage());
            }
        }
    }

    public String readFile(String key) {
        ResponseInputStream<GetObjectResponse> response = this.getObjectResponseInputStream(key);
        try {
            String fileContent = StreamUtils.copyToString(response, StandardCharsets.UTF_8);
            log.info("fileContent: {}", fileContent);
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("Error while reading file");
        }
    }
}
