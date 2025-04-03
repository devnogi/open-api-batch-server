package com.devnogi.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class DevnogiServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevnogiServerApplication.class, args);
    }
}
