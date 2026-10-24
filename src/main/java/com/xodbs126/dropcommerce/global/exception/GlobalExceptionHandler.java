package com.xodbs126.dropcommerce.global.exception;


import com.xodbs126.dropcommerce.global.exception.dto.FailResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<FailResponseDto> handle(GlobalException e) {

        ErrorCode errorCode = e.getErrorCode();

        FailResponseDto response = new FailResponseDto(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

}
