package com.example.backend.common;

import lombok.Getter;

/**
 * 业务状态码，与接口文档 1.4 保持一致。
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    STATUS_CONFLICT(409, "状态不允许"),
    STOCK_NOT_ENOUGH(422, "库存不足"),
    SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
