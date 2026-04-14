package com.zosh.ai;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ForecastClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public double predict(Map<String, Object> payload) {

        String url = "http://localhost:5000/predict";

        Map response = restTemplate.postForObject(url, payload, Map.class);

        return Double.parseDouble(response.get("forecast").toString());
    }
}