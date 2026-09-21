package com.music.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "music.jwt")
public class MusicJwtProperties {

    /** HMAC 签名密钥，建议不少于 32 字节 */
    private String secret = "music-dreamer-please-change-this-secret-key-32bytes";

    /** token 有效期（秒），默认 7 天 */
    private long expireSeconds = 604800L;
}
