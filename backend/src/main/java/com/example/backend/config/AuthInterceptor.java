package com.example.backend.config;

import com.example.backend.common.LoginUser;
import com.example.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录态拦截器：解析 Authorization 头中的 Bearer Token，
 * 有效则将当前用户放入请求属性，供 @CurrentUser 参数注入。
 * Token 无效或已登出抛出 401。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER = "LOGIN_USER";
    public static final String ATTR_TOKEN = "AUTH_TOKEN";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request.getHeader("Authorization"));
        if (token != null) {
            LoginUser loginUser = authService.current(token);
            request.setAttribute(ATTR_TOKEN, token);
            request.setAttribute(ATTR_USER, loginUser);
        }
        return true;
    }

    /** 从 "Bearer {token}" 头中提取 token，格式不符返回 null */
    public static String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
