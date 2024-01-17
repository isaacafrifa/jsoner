package iam.jsoner.controller;

import iam.jsoner.service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;


@RestController
@RequestMapping("api/v1/files")
public record FileController(FileService fileService) {

    @GetMapping("/download/{file-name}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("file-name") String fileName)  {
        ResponseInputStream<GetObjectResponse> responseInputStream = fileService.getObjectResponseInputStream(fileName);
        // set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(responseInputStream.response().contentLength());
        headers.setContentType(MediaType.valueOf(responseInputStream.response().contentType()));
        headers.setContentDispositionFormData("attachment", fileName);
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(responseInputStream));
    }

    @GetMapping("/read/{file-name}")
    public String readFile(@PathVariable("file-name") String fileName)  {
        return fileService.readFile(fileName);
    }

    @PostMapping
    public String postFile() {
        // uploading a predefined file
        return fileService.uploadFile();
    }
}
