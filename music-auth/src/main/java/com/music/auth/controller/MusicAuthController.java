package com.music.auth.controller;

import cn.hutool.core.util.StrUtil;
import com.music.auth.dto.LoginDTO;
import com.music.auth.dto.RegisterDTO;
import com.music.auth.dto.UpdatePasswordDTO;
import com.music.auth.dto.UpdateProfileDTO;
import com.music.auth.service.AuthService;
import com.music.auth.service.ProfileService;
import com.music.auth.vo.LoginVO;
import com.music.auth.vo.UserInfoVO;
import com.music.common.constants.CommonConstants;
import com.music.common.core.R;
import com.music.common.core.ResultCode;
import com.music.common.exception.BizException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MusicAuthController {

    private final AuthService authService;
    private final ProfileService profileService;

    /** 用于验证服务已启动、以及网关路由是否打通 */
    @GetMapping("/ping")
    public R<String> ping() {
        return R.ok("pong from music-auth");
    }

    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public R<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return R.ok("注册成功", authService.register(dto));
    }

    /**
     * Authorization 头会被网关原样透传到下游，所以这里直接读它拿原始 token 取 jti。
     * 不能靠 X-User-Id —— 黑名单是 per-token 的，光有 userId 定位不到具体哪个 token 要失效。
     */
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader(value = CommonConstants.TOKEN_HEADER, required = false) String authorization) {
        String token = StrUtil.removePrefix(authorization, CommonConstants.TOKEN_PREFIX);
        authService.logout(token);
        return R.ok("已退出登录", null);
    }

    /** userId 来自网关校验 token 后写入的请求头，服务本身不再解析 token */
    @GetMapping("/me")
    public R<UserInfoVO> me(@RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) Long userId) {
        if (userId == null) {
            // 绕过网关直连本服务时会走到这里
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return R.ok(authService.currentUser(userId));
    }

    /**
     * 修改个人资料。PATCH 语义：只改请求体里出现过的字段，没出现的保持原值；
     * 头像、生日、签名传空串表示清空。接口返回更新后的完整信息，前端可据此直接刷新本地缓存。
     */
    @PatchMapping("/profile")
    public R<UserInfoVO> updateProfile(
            @RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) Long userId,
            @Valid @RequestBody UpdateProfileDTO dto) {
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return R.ok("保存成功", profileService.updateProfile(userId, dto));
    }

    /**
     * 修改密码。成功后当前 token 会被拉黑，客户端必须清掉本地登录态并跳登录页。
     * 同 logout：Authorization 头由网关原样透传，用它定位要失效的具体那个 token ——
     * 光有 userId 定位不到，黑名单是 per-token 的。
     */
    @PutMapping("/password")
    public R<Void> changePassword(
            @RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) Long userId,
            @RequestHeader(value = CommonConstants.TOKEN_HEADER, required = false) String authorization,
            @Valid @RequestBody UpdatePasswordDTO dto) {
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        String token = StrUtil.removePrefix(authorization, CommonConstants.TOKEN_PREFIX);
        authService.changePassword(userId, token, dto);
        return R.ok("密码修改成功，请重新登录", null);
    }
}
