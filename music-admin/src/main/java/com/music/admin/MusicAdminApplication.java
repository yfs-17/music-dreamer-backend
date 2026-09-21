package com.music.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.music.admin.mapper")
public class MusicAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicAdminApplication.class, args);
    }
}
