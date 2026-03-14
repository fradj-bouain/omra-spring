package com.omra.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OmraPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmraPlatformApplication.class, args);
    }
}
