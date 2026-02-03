package com.capstone.controllers;

import com.capstone.payload.dto.CategoryDto;
import com.capstone.payload.response.ApiResponse;
import com.capstone.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) throws Exception {
        return ResponseEntity.ok(
                categoryService.createCategory(categoryDto)
        );
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<CategoryDto>> getCategory(@PathVariable Long storeId) throws Exception {
        return ResponseEntity.ok(
                categoryService.getCategoriesByStore(storeId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory( @PathVariable Long id,
                                                       @RequestBody CategoryDto categoryDto) throws Exception {
        return ResponseEntity.ok(
                categoryService.updateCategory(id, categoryDto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id,
                                                      @RequestBody CategoryDto categoryDto) throws Exception {
        categoryService.deleteCategory(id);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Category deleted successfully");
        return ResponseEntity.ok(apiResponse);
    }
}
