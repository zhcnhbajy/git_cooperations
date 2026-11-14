package com.example.backend.common;

import lombok.Getter;

/**
 * 业务异常，由全局异常处理器转换为统一响应。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode, resultCode.getMessage());
    }
}
