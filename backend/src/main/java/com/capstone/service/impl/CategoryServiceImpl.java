package com.capstone.service.impl;

import com.capstone.domain.UserRole;
import com.capstone.mapper.CategoryMapper;
import com.capstone.model.Category;
import com.capstone.model.Store;
import com.capstone.model.Users;
import com.capstone.payload.dto.CategoryDto;
import com.capstone.repository.CategoryRepository;
import com.capstone.repository.StoreRepository;
import com.capstone.service.CategoryService;
import com.capstone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final StoreRepository storeRepository;

    @Override
    public CategoryDto createCategory(CategoryDto dto) throws Exception {
        Users user = userService.getCurrentUser();

        Store store = storeRepository.findById(dto.getStoreId()).orElseThrow(
                ()-> new Exception("Store not found")
        );
        Category category = Category.builder()
                .store(store)
                .name(dto.getName())
                .build();
        checkAuthority(user,category.getStore());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategoriesByStore(Long storeId){

        List<Category> categories = categoryRepository.findByStoreId(storeId);
        return categories.stream().map(CategoryMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto dto) throws Exception {
        Category category = categoryRepository.findById(id).orElseThrow(
                ()-> new Exception("Category does not exist")
        );
        Users user = userService.getCurrentUser();
        category.setName(dto.getName());
        checkAuthority(user,category.getStore());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) throws Exception {
        Category category = categoryRepository.findById(id).orElseThrow(
                ()-> new Exception("Category does not exist")
        );

        Users user = userService.getCurrentUser();
        checkAuthority(user,category.getStore());
        categoryRepository.delete(category);
    }

    private void checkAuthority(Users user, Store store) throws Exception {
        boolean isAdmin = user.getRole().equals(UserRole.ROLE_STORE_ADMIN);
        boolean isManager = user.getRole().equals(UserRole.ROLE_STORE_MANAGER);
        boolean isSameStore = user.equals(store.getStoreAdmin());

        if(!(isAdmin && isSameStore) && !isManager){
            throw new Exception("You don't have a permission to manage this category");
        }
    }
}
