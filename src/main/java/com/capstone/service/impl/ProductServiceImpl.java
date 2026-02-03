package com.capstone.service.impl;

import com.capstone.mapper.ProductMapper;
import com.capstone.model.Category;
import com.capstone.model.Product;
import com.capstone.model.Store;
import com.capstone.model.Users;
import com.capstone.payload.dto.ProductDto;
import com.capstone.repository.CategoryRepository;
import com.capstone.repository.ProductRepository;
import com.capstone.repository.StoreRepository;
import com.capstone.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductDto createProduct(ProductDto productDto, Users user) throws Exception {
        Store store = storeRepository.findById(productDto.getStoreId())
                .orElseThrow(() -> new Exception("Store not found")
        );
        Category category = categoryRepository.findById(productDto.getCategoryId()).orElseThrow(
                ()-> new Exception("Category not found")
        );
        Product product = ProductMapper.toEntity(productDto,store,category);
        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto, Users user) throws Exception {
        Product product = productRepository.findById(id).orElseThrow(
                ()-> new Exception("Product not found")
        );

        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setSku(productDto.getSku());
        product.setImage(productDto.getImage());
        product.setMrp(productDto.getMrp());
        product.setSellingPrice(productDto.getSellingPrice());
        product.setBrand(productDto.getBrand());
        product.setUpdatedAt(LocalDateTime.now());
        if(productDto.getCategoryId()!=null){
            Category category = categoryRepository.findById(productDto.getCategoryId()).orElseThrow(
                    ()-> new Exception("Category not found")
            );
            product.setCategory(category);
        }
        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public void deleteProduct(Long id, Users user) throws Exception {
        Product product = productRepository.findById(id).orElseThrow(
                ()-> new Exception("Product not found...")
        );
        productRepository.delete(product);
    }

    @Override
    public List<ProductDto> getProductsByStoreId(Long storeId) {
        List<Product> products = productRepository.findByStoreId(storeId);
        return products.stream().map(ProductMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> searchByKeyword(Long storeId, String keyword) {
        List<Product> products = productRepository.searchByKeyword(storeId, keyword);
        return products.stream().map(ProductMapper::toDto).collect(Collectors.toList());
    }
}
