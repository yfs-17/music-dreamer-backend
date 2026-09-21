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
}
