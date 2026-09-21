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

    /** 免鉴权路径，Ant 风格通配 */
    private List<String> whitelist = new ArrayList<>();
}
