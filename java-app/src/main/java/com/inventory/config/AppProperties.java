package com.inventory.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private Jwt jwt = new Jwt();
    private PythonAnalytics pythonAnalytics = new PythonAnalytics();
    private Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs;
    }

    @Data
    public static class PythonAnalytics {
        private String baseUrl;
        private String apiKey;
        private long timeoutMs;
    }

    @Data
    public static class Cors {
        private String allowedOrigins;
    }
}
