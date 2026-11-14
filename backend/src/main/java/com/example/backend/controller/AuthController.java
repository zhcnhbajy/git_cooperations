package com.example.backend.controller;

import com.example.backend.common.CurrentUser;
import com.example.backend.common.LoginUser;
import com.campusswap.common.Result;
import com.example.backend.config.AuthInterceptor;
import com.example.backend.dto.AuthDTO;
import com.example.backend.service.AuthService;
import com.example.backend.vo.LoginVO;
import com.example.backend.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证模块：注册 / 登录 / 登出 / 当前登录信息。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** A1 注册（只能注册普通用户） */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody AuthDTO dto) {
        return Result.success("注册成功", authService.register(dto));
    }

    /** A2 登录（用户 / 管理员共用） */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody AuthDTO dto) {
        return Result.success("登录成功", authService.login(dto));
    }

    /** A3 登出 */
    @PostMapping("/logout")
    public Result<Void> logout(@CurrentUser LoginUser user,
                               @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(AuthInterceptor.extractToken(authorization));
        return Result.success("已登出", null);
    }

    /** A4 当前登录信息 */
    @GetMapping("/me")
    public Result<LoginUser> me(@CurrentUser LoginUser user) {
        return Result.success(user);
    }
}
