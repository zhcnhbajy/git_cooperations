package com.example.backend.common;

import com.campusswap.common.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把业务异常与参数校验异常转换为统一 Result 结构，
 * HTTP 状态码与业务 code 保持一致。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getCode())
                .body(Result.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ResultCode.PARAM_ERROR.getMessage());
        return ResponseEntity.status(ResultCode.PARAM_ERROR.getCode())
                .body(Result.error(ResultCode.PARAM_ERROR.getCode(), message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(ResultCode.PARAM_ERROR.getCode())
                .body(Result.error(ResultCode.PARAM_ERROR.getCode(), "请求体格式错误"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpected(Exception e) {
        return ResponseEntity.status(ResultCode.SERVER_ERROR.getCode())
                .body(Result.error(ResultCode.SERVER_ERROR.getCode(), ResultCode.SERVER_ERROR.getMessage()));
    }
}
