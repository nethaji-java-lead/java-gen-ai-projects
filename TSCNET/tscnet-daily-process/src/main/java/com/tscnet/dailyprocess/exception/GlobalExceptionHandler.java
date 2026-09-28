package com.tscnet.dailyprocess.exception;

import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.xml.sax.SAXException;

import java.io.IOException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid argument exception encountered: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(SAXException.class)
    public ResponseEntity<String> handleSAXException(SAXException e) {
        log.error("XML SAX validation error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body("XML Validation Error: " + e.getMessage());
    }

    @ExceptionHandler(JAXBException.class)
    public ResponseEntity<String> handleJAXBException(JAXBException e) {
        log.error("JAXB Parsing error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to parse XML document: " + e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException e) {
        log.error("SFTP / File I/O Error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("SFTP File Processing Error: " + e.getMessage());
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<String> handleMessagingException(MessagingException e) {
        log.error("Messaging / Kafka Event error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Notification Event Delivery Error: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        log.error("Unhandled process execution exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred during execution: " + e.getMessage());
    }

    @ExceptionHandler(NonBusinessDayException.class)
    public ResponseEntity<String> handleNonBusinessDayException(NonBusinessDayException e) {
        log.error("The provided date is not a valid business day: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("The provided date is not a valid business day: " + e.getMessage());
    }
}