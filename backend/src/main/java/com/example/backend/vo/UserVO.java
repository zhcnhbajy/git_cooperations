package com.example.backend.vo;

import lombok.Data;

/**
 * 用户信息（不含密码）。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private Integer role;
}
