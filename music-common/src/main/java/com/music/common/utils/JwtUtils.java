package com.music.common.utils;

import cn.hutool.core.util.IdUtil;
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

    /**
     * 自动生成 jti。jti 是登出黑名单的 key —— 用 token 串本身当 key 太长，
     * 且 token 会随签名算法变更而变，用稳定的 jti 更合适。
     */
    public String createToken(Long userId, String username) {
        return createTokenWithRole(userId, username, null);
    }

    /**
     * 签发带角色信息的 token。网关会把 role 透传给下游，
     * 管理端接口据此判断是不是管理员，避免 admin 服务跨库回查用户角色。
     */
    public String createTokenWithRole(Long userId, String username, Integer role) {
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> payload = new HashMap<>(7);
        payload.put("userId", userId);
        payload.put("username", username);
        if (role != null) {
            // 改造前的老 token 没有 role，不能 put null（JWT 序列化会出问题）
            payload.put("role", role);
        }
        payload.put("jti", IdUtil.fastSimpleUUID());
        payload.put("iat", now);
        payload.put("exp", now + expireSeconds);
        return JWTUtil.createToken(payload, key);
    }

    /** 指定 jti 签发，便于测试黑名单等场景 */
    public String createToken(Long userId, String username, String jti) {
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> payload = new HashMap<>(6);
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("jti", jti);
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

    /** 老 token 没有 role 字段，返回 null，调用方按普通用户处理 */
    public Integer getRole(String token) {
        Object value = getPayload(token, "role");
        return value == null ? null : Integer.valueOf(value.toString());
    }

    public String getJti(String token) {
        Object value = getPayload(token, "jti");
        return value == null ? null : value.toString();
    }

    /**
     * token 剩余有效秒数，已过期返回 0。
     * 登出写黑名单时拿它当 TTL —— 用一个已过期的 token 登出时不该往 Redis 里塞无意义的 key。
     */
    public long getRemainingSeconds(String token) {
        Object value = getPayload(token, "exp");
        if (value == null) {
            return 0L;
        }
        // exp 经 JSON 往返后可能是 Integer 也可能是 Long，统一按 Number 取
        long exp = ((Number) value).longValue();
        long remaining = exp - System.currentTimeMillis() / 1000;
        return Math.max(remaining, 0L);
    }

    private Object getPayload(String token, String name) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return JWTUtil.parseToken(token).getPayload(name);
    }
}
