package com.capstone.payload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PredictionResponse {
    @JsonProperty("predicted_demand")
    private double predictedDemand;
}