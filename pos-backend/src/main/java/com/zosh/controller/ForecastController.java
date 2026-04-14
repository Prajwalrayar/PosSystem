package com.zosh.controller;

import com.zosh.ai.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/forecast", "/api/smart-ai"})
@RequiredArgsConstructor
@CrossOrigin("*")
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping("/{productId}")
    public Map<String, Object> getPrediction(
            @PathVariable Long productId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "day") String horizon
    ) {
        return forecastService.predictDemand(branchId, productId, horizon);
    }

    @GetMapping("/graph/{productId}")
    public Map<String, Object> getGraph(
            @PathVariable Long productId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "week") String horizon
    ) {
        return forecastService.demandSeries(branchId, productId, horizon);
    }

    @GetMapping("/demand/predict")
    public Map<String, Object> demandPredict(
            @RequestParam(required = false) Long branchId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "day") String horizon
    ) {
        return forecastService.predictDemand(branchId, productId, horizon);
    }

    @GetMapping("/demand/series")
    public Map<String, Object> demandSeries(
            @RequestParam(required = false) Long branchId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "week") String horizon
    ) {
        return forecastService.demandSeries(branchId, productId, horizon);
    }

    @PostMapping("/basket/recommend")
    public Map<String, Object> basketRecommend(@RequestBody Map<String, Object> payload) {
        Object rawItems = payload.get("items");
        List<String> items = rawItems instanceof List<?> list
                ? list.stream().map(String::valueOf).filter(item -> !item.isBlank()).toList()
                : List.of();

        String horizon = String.valueOf(payload.getOrDefault("horizon", "week"));
        int topK = payload.get("top_k") instanceof Number number
                ? number.intValue()
                : Integer.parseInt(String.valueOf(payload.getOrDefault("top_k", 10)));

        return forecastService.basketRecommend(items, horizon, topK);
    }

    @GetMapping("/basket/series")
    public Map<String, Object> basketSeries(
            @RequestParam(defaultValue = "week") String horizon,
            @RequestParam(defaultValue = "20") int topN
    ) {
        return forecastService.basketSeries(horizon, topN);
    }
}