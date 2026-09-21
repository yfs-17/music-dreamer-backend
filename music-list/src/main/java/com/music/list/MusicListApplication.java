package com.music.list;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.music.list.mapper")
public class MusicListApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicListApplication.class, args);
    }
}
