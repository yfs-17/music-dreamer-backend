package com.music.setting.controller;

import com.music.common.core.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/setting")
public class MusicSettingController {

    /** 用于验证服务已启动、以及网关路由是否打通 */
    @GetMapping("/ping")
    public R<String> ping() {
        return R.ok("pong from music-setting");
    }
}
