package com.zosh.ai;

import com.zosh.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private final OrderItemRepository repo;
    private final ForecastClient client;

    public double predict(Long productId) {

        int yesterday = repo.yesterday(productId);
        double avg7 = repo.avg7(productId);
        double avg30 = repo.avg30(productId);

        LocalDate now = LocalDate.now();

        int dayOfWeek = now.getDayOfWeek().getValue() % 7;
        int month = now.getMonthValue();
        int isWeekend = (dayOfWeek >= 5) ? 1 : 0;

        Map<String, Object> payload = new HashMap<>();
        payload.put("day_of_week", dayOfWeek);
        payload.put("month", month);
        payload.put("prev_sales", yesterday);
        payload.put("avg_7", avg7);
        payload.put("avg_30", avg30);
        payload.put("is_weekend", isWeekend);

        return client.predict(payload);
    }
}