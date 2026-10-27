package com.music.common.config;

import com.music.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 只在 Servlet（Spring MVC）应用中生效。
 * 条件在自动配置阶段由 ASM 读字节码判定，GlobalExceptionHandler 这个类根本不会被加载，
 * 因此网关（WebFlux）依赖 music-common 也不会被 servlet 组件污染。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(MusicInternalProperties.class)
public class CommonWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /** 保护 /internal/**，只允许服务间携带共享密钥调用 */
    @Bean
    @ConditionalOnMissingBean
    public InternalApiFilter internalApiFilter(MusicInternalProperties properties) {
        return new InternalApiFilter(properties.getToken());
    }
}
