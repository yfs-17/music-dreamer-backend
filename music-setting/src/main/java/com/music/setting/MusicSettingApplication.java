package com.music.setting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.music.setting.mapper")
public class MusicSettingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicSettingApplication.class, args);
    }
}
