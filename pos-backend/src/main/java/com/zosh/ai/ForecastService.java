package com.zosh.ai;

import com.zosh.modal.Branch;
import com.zosh.modal.User;
import com.zosh.repository.BranchRepository;
import com.zosh.repository.OrderItemRepository;
import com.zosh.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private final OrderItemRepository repo;
    private final BranchRepository branchRepository;
    private final UserService userService;
    private final ForecastClient client;

    public double predict(Long productId) {
        Map<String, Object> result = predictDemand(null, productId, "day");
        Object average = result.get("average");
        if (average instanceof Number number) {
            return number.doubleValue();
        }

        Object forecast = result.get("predicted_values");
        if (forecast instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Number number) {
            return number.doubleValue();
        }

        return 0.0;
    }

    public Map<String, Object> predictDemand(Long branchId, Long productId, String horizon) {
        Long resolvedBranchId = resolveBranchId(branchId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("branch_id", resolvedBranchId);
        payload.put("product_id", productId);
        payload.put("horizon", horizon);

        Map<String, Object> result = new HashMap<>(client.predictDemand(payload));
        result.putIfAbsent("branch_id", resolvedBranchId);
        result.putIfAbsent("product_id", productId);
        result.putIfAbsent("horizon", horizon);
        return result;
    }

    public Map<String, Object> demandSeries(Long branchId, Long productId, String horizon) {
        Long resolvedBranchId = resolveBranchId(branchId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("branch_id", resolvedBranchId);
        payload.put("product_id", productId);
        payload.put("horizon", horizon);

        Map<String, Object> result = new HashMap<>(client.demandSeries(payload));
        Map<String, Object> forecast = predictDemand(resolvedBranchId, productId, horizon);
        result.put("forecast", forecast.get("predicted_values"));
        result.put("predicted_values", forecast.get("predicted_values"));
        result.put("average", forecast.get("average"));
        result.put("trend", forecast.get("trend"));
        result.put("branch_id", resolvedBranchId);
        result.put("product_id", productId);
        result.put("horizon", horizon);
        return result;
    }

    public Map<String, Object> basketRecommend(List<String> items, String horizon, int topK) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("items", items);
        payload.put("horizon", horizon);
        payload.put("top_k", topK);
        return new HashMap<>(client.basketRecommend(payload));
    }

    public Map<String, Object> basketSeries(String horizon, int topN) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("horizon", horizon);
        payload.put("top_n", topN);
        return new HashMap<>(client.basketSeries(payload));
    }

    private Long resolveBranchId(Long branchId) {
        if (branchId != null) {
            return branchId;
        }

        User currentUser = userService.getCurrentUser();
        if (currentUser.getBranch() != null && currentUser.getBranch().getId() != null) {
            return currentUser.getBranch().getId();
        }

        if (currentUser.getStore() != null) {
            return branchRepository.findByStoreId(currentUser.getStore().getId())
                    .stream()
                    .findFirst()
                    .map(Branch::getId)
                    .orElseThrow(() -> new IllegalStateException("Branch ID is required for SmartAi forecasting"));
        }

        throw new IllegalStateException("Branch ID is required for SmartAi forecasting");
    }
}