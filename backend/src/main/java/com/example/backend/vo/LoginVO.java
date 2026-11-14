package com.example.backend.vo;

import lombok.Data;

/**
 * 登录结果。
 */
@Data
public class LoginVO {

    private String token;
    private UserVO user;
}
