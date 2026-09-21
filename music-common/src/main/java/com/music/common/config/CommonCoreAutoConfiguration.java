package com.music.common.config;

import com.music.common.utils.JwtUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MusicJwtProperties.class)
public class CommonCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils(MusicJwtProperties properties) {
        return new JwtUtils(properties.getSecret(), properties.getExpireSeconds());
    }
}
