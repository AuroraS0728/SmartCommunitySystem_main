package com.smartcommunity.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() == null
                ? "参数校验失败"
                : e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail(StatusCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        return Result.fail(StatusCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        String msg = e.getMessage();
        Throwable cause = e.getCause();
        while ((msg == null || msg.isBlank()) && cause != null) {
            msg = cause.getMessage();
            cause = cause.getCause();
        }
        if (msg == null || msg.isBlank()) {
            msg = e.getClass().getSimpleName();
        }
        return Result.fail(StatusCode.ERROR, msg);
    }
}
