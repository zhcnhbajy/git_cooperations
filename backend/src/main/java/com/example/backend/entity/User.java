package com.example.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应表 user。
 */
@Data
public class User {

    /** 主键ID */
    private Long id;

    /** 用户名，全局唯一 */
    private String username;

    /** 密码（MD5 存储） */
    private String password;

    /** 角色：0-管理员，1-普通用户 */
    private Integer role;

    /** 注册时间 */
    private LocalDateTime createTime;
}
