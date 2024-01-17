package iam.jsoner.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record APIError(String message,
                       @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
                       LocalDateTime timeStamp) {

    // Custom constructor
    public APIError(String message) {
        this(message, LocalDateTime.now());
    }
}
