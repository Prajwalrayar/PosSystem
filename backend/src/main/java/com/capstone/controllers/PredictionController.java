package com.capstone.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.capstone.service.PredictionService;
import com.capstone.payload.dto.PredictionRequest;
import com.capstone.payload.dto.PredictionResponse;

@RestController
@RequestMapping("/api/predict")
@CrossOrigin
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping
    public PredictionResponse predict(@RequestBody PredictionRequest request) {
        return predictionService.getPrediction(request.getDay());
    }
}