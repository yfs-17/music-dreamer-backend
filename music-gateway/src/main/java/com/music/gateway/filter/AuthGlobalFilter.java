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
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 统一鉴权：校验 token 后把用户信息写入请求头，下游服务直接读头即可，无需重复解析 token。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /** 规则里出现这些前缀时视为「方法:路径」写法，否则整条规则都当路径 */
    private static final Set<String> ALLOWED_METHODS =
            Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS", "TRACE");

    private final JwtUtils jwtUtils;
    private final GatewayAuthProperties authProperties;
    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        if (!authProperties.isEnabled() || isWhitelisted(method, path)) {
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

        Integer role = jwtUtils.getRole(token);

        // 必须先 remove 再 set：否则客户端可以自己伪造 X-User-Id / X-User-Role 绕过鉴权。
        // 注意不能写成 headers.remove(a).remove(b) —— 头不存在时 remove 返回 null，链式调用会 NPE。
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(CommonConstants.USER_ID_HEADER);
                    headers.remove(CommonConstants.USER_ROLE_HEADER);
                    headers.set(CommonConstants.USER_ID_HEADER, String.valueOf(userId));
                    headers.set(CommonConstants.USERNAME_HEADER,
                            StrUtil.nullToEmpty(jwtUtils.getUsername(token)));
                    // 老 token 没有 role，不写这个头，下游按普通用户处理
                    if (role != null) {
                        headers.set(CommonConstants.USER_ROLE_HEADER, String.valueOf(role));
                    }
                })
                .build();

        // 验签只能证明 token 是本站签发的，不能证明它没被登出，所以还要查一次黑名单
        return isBlacklisted(token)
                .flatMap(blacklisted -> blacklisted
                        ? writeUnauthorized(exchange, "登录已失效，请重新登录")
                        : chain.filter(exchange.mutate().request(mutated).build()));
    }

    /** 查询 token 是否已被登出（music-auth 写入的 jti 黑名单） */
    private Mono<Boolean> isBlacklisted(String token) {
        String jti = jwtUtils.getJti(token);
        if (StrUtil.isBlank(jti)) {
            // 改造前签发的老 token 没有 jti，无从查起，只能放行
            return Mono.just(Boolean.FALSE);
        }
        return redisTemplate.hasKey(CommonConstants.AUTH_BLACKLIST_PREFIX + jti)
                // fail-open：JWT 签名本身已经验过，黑名单只承载「登出后立即失效」这一个语义。
                // 让 Redis 抖动演变成全站 401 不划算，记 error 便于事后发现。
                .onErrorResume(e -> {
                    log.error("查询登出黑名单失败，本次请求放行: {}", e.getMessage());
                    return Mono.just(Boolean.FALSE);
                })
                // hasKey 理论上不会发空值，返回 null 时按未命中处理更安全
                .map(Boolean.TRUE::equals)
                .defaultIfEmpty(Boolean.FALSE);
    }

    /**
     * 白名单支持两种写法：{@code /path/**}（任意方法）与 {@code GET:/path/**}（限定方法）。
     * 公开只读接口必须用后者，避免同前缀的写接口被一并放行。
     */
    private boolean isWhitelisted(String method, String path) {
        // CORS 预检不带业务凭证，拦下来会让浏览器里的跨域请求全部失败
        if (HttpMethod.OPTIONS.matches(method)) {
            return true;
        }
        for (String rule : authProperties.getWhitelist()) {
            String pattern = rule;
            String ruleMethod = null;
            int separator = rule.indexOf(':');
            if (separator > 0) {
                String prefix = rule.substring(0, separator).trim().toUpperCase();
                if (ALLOWED_METHODS.contains(prefix)) {
                    ruleMethod = prefix;
                    pattern = rule.substring(separator + 1);
                }
            }
            if (ruleMethod != null && !ruleMethod.equals(method)) {
                continue;
            }
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
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
