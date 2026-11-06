package com.inventory.analytics.client;

import com.inventory.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnalyticsClient {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("X-API-Key", appProperties.getPythonAnalytics().getApiKey());
        return h;
    }

    public Map<String, Object> post(String path, Object body) {
        String url = appProperties.getPythonAnalytics().getBaseUrl() + path;
        HttpEntity<Object> entity = new HttpEntity<>(body, headers());
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return resp.getBody();
    }

    public Map<String, Object> get(String path) {
        String url = appProperties.getPythonAnalytics().getBaseUrl() + path;
        HttpEntity<Void> entity = new HttpEntity<>(headers());
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return resp.getBody();
    }
}
