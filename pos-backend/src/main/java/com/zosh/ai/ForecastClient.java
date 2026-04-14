package com.zosh.ai;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ForecastClient {

    private static final String BASE_URL = "http://localhost:5001/api/ml";
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> predictDemand(Map<String, Object> payload) {
        return post("/demand/predict", payload);
    }

    public Map<String, Object> demandSeries(Map<String, Object> payload) {
        return get("/demand/series", payload);
    }

    public Map<String, Object> basketRecommend(Map<String, Object> payload) {
        return post("/basket/recommend", payload);
    }

    public Map<String, Object> basketSeries(Map<String, Object> payload) {
        return get("/basket/series", payload);
    }

    private Map<String, Object> post(String path, Map<String, Object> payload) {
        Map response = restTemplate.postForObject(BASE_URL + path, payload, Map.class);
        return response == null ? new LinkedHashMap<>() : new LinkedHashMap<>(response);
    }

    private Map<String, Object> get(String path, Map<String, Object> payload) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL + path);
        payload.forEach((key, value) -> {
            if (value != null) {
                builder.queryParam(key, value);
            }
        });

        Map response = restTemplate.getForObject(builder.toUriString(), Map.class);
        return response == null ? new LinkedHashMap<>() : new LinkedHashMap<>(response);
    }
}