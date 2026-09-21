package com.music.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于 hutool-jwt 实现，不依赖 Jackson，因此不受 Jackson 2/3 换代影响。
 */
public class JwtUtils {

    private final byte[] key;
    private final long expireSeconds;

    public JwtUtils(String secret, long expireSeconds) {
        this.key = secret.getBytes(StandardCharsets.UTF_8);
        this.expireSeconds = expireSeconds;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public String createToken(Long userId, String username) {
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> payload = new HashMap<>(4);
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("iat", now);
        payload.put("exp", now + expireSeconds);
        return JWTUtil.createToken(payload, key);
    }

    /** 验签通过且未过期才返回 true */
    public boolean verify(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        try {
            if (!JWTUtil.verify(token, key)) {
                return false;
            }
            JWTValidator.of(token).validateDate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Object value = getPayload(token, "userId");
        return value == null ? null : Long.valueOf(value.toString());
    }

    public String getUsername(String token) {
        Object value = getPayload(token, "username");
        return value == null ? null : value.toString();
    }

    private Object getPayload(String token, String name) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return JWTUtil.parseToken(token).getPayload(name);
    }
}
