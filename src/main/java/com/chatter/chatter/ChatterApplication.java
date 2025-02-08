package com.chatter.chatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EntityScan(basePackages = "com.chatter.chatter.dto")
@EnableScheduling
public class ChatterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatterApplication.class, args);
    }

}