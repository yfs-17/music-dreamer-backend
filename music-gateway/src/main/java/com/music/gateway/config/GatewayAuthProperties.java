package com.music.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "music.gateway.auth")
public class GatewayAuthProperties {

    /** 关掉后所有请求都不校验 token，仅用于本地联调 */
    private boolean enabled = true;

    /**
     * 免鉴权规则，Ant 风格通配。
     *
     * <p>两种写法：
     * <ul>
     *   <li>{@code /api/auth/login}：不限定方法</li>
     *   <li>{@code GET:/api/music/song/**}：只放行 GET（公开只读接口必须用这种写法，
     *       否则同前缀的写接口也会被一起放行）</li>
     * </ul>
     * OPTIONS 预检请求始终放行。
     */
    private List<String> whitelist = new ArrayList<>();
}
