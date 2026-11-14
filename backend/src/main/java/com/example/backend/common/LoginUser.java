package com.example.backend.common;

/**
 * 当前登录用户信息，登录成功后按 Token 保存在内存中。
 */
public record LoginUser(Long id, String username, Integer role) {

    /** 是否管理员：0-管理员 */
    public boolean isAdmin() {
        return Integer.valueOf(0).equals(role);
    }
}
