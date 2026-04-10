package com.capstone.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.capstone.payload.dto.PredictionRequest;
import com.capstone.payload.dto.PredictionResponse;

@Service
public class PredictionService {

    private final String AI_URL = "http://localhost:5001/predict";

    public PredictionResponse getPrediction(int day) {
        RestTemplate restTemplate = new RestTemplate();

        PredictionRequest request = new PredictionRequest();
        request.setDay(day);

        return restTemplate.postForObject(
                AI_URL,
                request,
                PredictionResponse.class
        );
    }
}