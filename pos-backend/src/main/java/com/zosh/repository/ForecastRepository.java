package com.zosh.repository;

import com.zosh.modal.Forecast;
import org.springframework.data.repository.CrudRepository;

public interface ForecastRepository extends CrudRepository<Forecast, Long> {
    Forecast findByProductId(Long productId);
}
