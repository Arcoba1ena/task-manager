package ru.task_manager.controller;

import java.util.Map;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        if (ex instanceof NoResourceFoundException) {
            NoResourceFoundException nrfe = (NoResourceFoundException) ex;
            nrfe.getResourcePath();
            if (nrfe.getResourcePath().equals("favicon.ico")) {
                return ResponseEntity.notFound().build();
            }
        }

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", "500");

        if (!(ex instanceof NoResourceFoundException) ||
                !((NoResourceFoundException) ex).getResourcePath().equals("favicon.ico")) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}