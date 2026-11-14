package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.common.JwtUtil;
import com.example.backend.common.LoginUser;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.AuthDTO;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.vo.LoginVO;
import com.example.backend.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 认证服务：注册、登录、登出、登录态校验。
 * 登录签发 JWT，服务端无状态校验；登出将令牌加入黑名单，旧 Token 校验不通过。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Integer ROLE_USER = 1;

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /** 已登出令牌黑名单：token -> 原过期时间戳，过期后自动清理 */
    private final ConcurrentMap<String, Long> revokedTokens = new ConcurrentHashMap<>();

    public UserVO register(AuthDTO dto) {
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "用户名已被注册");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(md5(dto.getPassword()));
        user.setRole(ROLE_USER);
        userMapper.insert(user);
        return toUserVO(userMapper.selectById(user.getId()));
    }

    public LoginVO login(AuthDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null || !md5(dto.getPassword()).equals(user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getRole());

        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generate(loginUser));
        vo.setUser(toUserVO(user));
        return vo;
    }

    /** 登出：令牌加入黑名单，在原过期时间前一直视为失效 */
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        long now = System.currentTimeMillis();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
        Long expiry = jwtUtil.expiryOf(token);
        if (expiry != null) {
            revokedTokens.put(token, expiry);
        }
    }

    /** 校验登录态：签名有效且未过期、且未登出，否则抛出 401 */
    public LoginUser current(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        LoginUser loginUser = jwtUtil.parse(token);
        if (loginUser == null || revokedTokens.containsKey(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        return vo;
    }

    private String md5(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "密码加密失败");
        }
    }
}
