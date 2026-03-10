package com.capstone.service;

import com.capstone.model.Users;
import com.capstone.payload.dto.ProductDto;

import java.util.List;

public interface ProductService {

    ProductDto createProduct(ProductDto productDto, Users user) throws Exception;

    ProductDto updateProduct(Long id, ProductDto productDto, Users user) throws Exception;

    void deleteProduct(Long id, Users user) throws Exception;

    List<ProductDto> getProductsByStoreId(Long storeId);

    List<ProductDto> searchByKeyword(Long storeId, String keyword);



}
