package com.music.singer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.music.singer.mapper")
public class MusicSingerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicSingerApplication.class, args);
    }
}
