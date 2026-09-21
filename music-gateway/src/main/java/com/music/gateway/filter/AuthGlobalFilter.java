package com.music.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.music.common.constants.CommonConstants;
import com.music.common.utils.JwtUtils;
import com.music.gateway.config.GatewayAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 统一鉴权：校验 token 后把用户信息写入请求头，下游服务直接读头即可，无需重复解析 token。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private final JwtUtils jwtUtils;
    private final GatewayAuthProperties authProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (!authProperties.isEnabled() || isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(exchange);
        if (StrUtil.isBlank(token) || !jwtUtils.verify(token)) {
            return writeUnauthorized(exchange, "未登录或登录已过期");
        }

        Long userId = jwtUtils.getUserId(token);
        if (userId == null) {
            return writeUnauthorized(exchange, "token 内容异常");
        }

        // 必须先 remove 再 set：否则客户端可以自己伪造 X-User-Id 绕过鉴权
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(headers -> headers.remove(CommonConstants.USER_ID_HEADER))
                .header(CommonConstants.USER_ID_HEADER, String.valueOf(userId))
                .header(CommonConstants.USERNAME_HEADER, StrUtil.nullToEmpty(jwtUtils.getUsername(token)))
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isWhitelisted(String path) {
        return authProperties.getWhitelist().stream()
                .anyMatch(pattern -> MATCHER.match(pattern, path));
    }

    private String resolveToken(ServerWebExchange exchange) {
        String raw = exchange.getRequest().getHeaders().getFirst(CommonConstants.TOKEN_HEADER);
        if (StrUtil.isBlank(raw)) {
            raw = exchange.getRequest().getQueryParams().getFirst("token");
        }
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        return raw.startsWith(CommonConstants.TOKEN_PREFIX)
                ? raw.substring(CommonConstants.TOKEN_PREFIX.length())
                : raw;
    }

    /** 手写 JSON 响应，避免依赖 Jackson 的具体版本 */
    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null,\"timestamp\":"
                + System.currentTimeMillis() + "}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
