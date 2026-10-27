package com.music.common.constants;

public final class CommonConstants {

    private CommonConstants() {
    }

    /** 客户端携带 token 的请求头 */
    public static final String TOKEN_HEADER = "Authorization";

    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 网关校验 token 后写入的用户 ID 请求头。
     * 下游服务的 Controller 直接读这个头即可拿到当前用户，无需再解析 token。
     * 注意：网关必须先 remove 再 set，防止客户端伪造。
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    public static final String USERNAME_HEADER = "X-Username";

    /**
     * 网关校验 token 后写入的用户角色请求头：1 普通用户 2 歌手 3 管理员。
     * 老 token 没有 role 时网关不写这个头，下游按普通用户处理。
     */
    public static final String USER_ROLE_HEADER = "X-User-Role";

    /**
     * 服务间内部接口的共享密钥请求头。
     * /internal/** 由 music-common 的 InternalApiFilter 统一校验，网关不会路由这些路径。
     */
    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    /**
     * 登出黑名单的 Redis key 前缀，完整 key 为 前缀 + token 的 jti。
     * 写入方是 music-auth（登出），读取方是 music-gateway（每个请求校验），故放在公共模块。
     */
    public static final String AUTH_BLACKLIST_PREFIX = "music:auth:blacklist:";
}
