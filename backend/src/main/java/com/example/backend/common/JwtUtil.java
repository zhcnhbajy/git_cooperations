package com.example.backend.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：HS256 签名生成与解析登录令牌。
 * 令牌载荷包含用户 id（subject）、用户名与角色，过期时间由 jwt.expire-hours 配置。
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-hours:24}") long expireHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireHours * 3600_000L;
    }

    /** 生成登录令牌 */
    public String generate(LoginUser user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(user.id()))
                .claim("username", user.username())
                .claim("role", user.role())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    /** 解析并校验令牌，无效或已过期返回 null */
    public LoginUser parse(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            return null;
        }
        Long id = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        Integer role = claims.get("role", Integer.class);
        return new LoginUser(id, username, role);
    }

    /** 返回令牌过期时间戳（毫秒），无效令牌返回 null */
    public Long expiryOf(String token) {
        Claims claims = parseClaims(token);
        return claims == null ? null : claims.getExpiration().getTime();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
