package com.zosh.controller;

import com.zosh.ai.ForecastService;
import com.zosh.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/forecast")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ForecastController {

    private final ForecastService forecastService;
    private final OrderItemRepository orderItemRepository;

    // 🔥 Only prediction
    @GetMapping("/{productId}")
    public double getPrediction(@PathVariable Long productId) {
        return forecastService.predict(productId);
    }

    // 🔥 Graph + Prediction
    @GetMapping("/graph/{productId}")
    public Map<String, Object> getGraph(@PathVariable Long productId) {

        List<Map<String, Object>> actual =
                orderItemRepository.getDemandData(productId);

        double prediction = forecastService.predict(productId);

        Map<String, Object> res = new HashMap<>();
        res.put("actual", actual);
        res.put("prediction", prediction);

        return res;
    }
}