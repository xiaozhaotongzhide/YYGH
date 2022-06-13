package com.example.yygh.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class UserConfig {
    @Bean
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
