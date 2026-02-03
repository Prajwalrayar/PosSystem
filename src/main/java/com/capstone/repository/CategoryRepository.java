package com.capstone.repository;

import com.capstone.model.Category;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CategoryRepository extends CrudRepository<Category,Long> {
    List<Category> findByStoreId(Long storeId);

}
