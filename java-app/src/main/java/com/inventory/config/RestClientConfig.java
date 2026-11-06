package com.inventory.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestClientConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, @org.springframework.beans.factory.annotation.Qualifier("appProperties") AppProperties props) {
        return builder
                .setConnectTimeout(Duration.ofMillis(props.getPythonAnalytics().getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(props.getPythonAnalytics().getTimeoutMs()))
                .build();
    }
}
