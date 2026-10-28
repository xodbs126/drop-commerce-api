package com.xodbs126.dropcommerce.global.exception;


import com.xodbs126.dropcommerce.global.exception.dto.FailResponseDto;
import com.xodbs126.dropcommerce.global.exception.dto.InvalidFieldDto;
import com.xodbs126.dropcommerce.global.exception.dto.InvalidInputFailResponseDto;
import com.xodbs126.dropcommerce.global.exception.errorcode.CommonErrorCode;
import com.xodbs126.dropcommerce.global.exception.errorcode.ErrorCode;
import com.xodbs126.dropcommerce.global.exception.errorcode.Reason;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<FailResponseDto> handleGlobalException(GlobalException e) {

        ErrorCode errorCode = e.getErrorCode();

        FailResponseDto response = new FailResponseDto(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<InvalidInputFailResponseDto> handleInvalidInput(MethodArgumentNotValidException e) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;

        List<InvalidFieldDto> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new InvalidFieldDto(fieldError.getField(), toReason(fieldError.getCode())))
                .toList();

        InvalidInputFailResponseDto response = new InvalidInputFailResponseDto(
                errorCode.getCode(),
                errorCode.getMessage(),
                errors
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    private Reason toReason(String constraint) {
        return switch (constraint) {
            case "NotNull" -> Reason.REQUIRED;
            case "Size" -> Reason.OUT_OF_RANGE;
            case "Pattern" -> Reason.INVALID_FORMAT;
            default -> Reason.INVALID_VALUE;
        };
}

}
