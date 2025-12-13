package com.saas.platform.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfigConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
