package com.capstone.service.impl;

import com.capstone.domain.StoreStatus;
import com.capstone.exceptions.UserException;
import com.capstone.mapper.StoreMapper;
import com.capstone.model.Store;
import com.capstone.model.StoreContact;
import com.capstone.model.Users;
import com.capstone.payload.dto.StoreDto;
import com.capstone.repository.StoreRepository;
import com.capstone.service.StoreService;
import com.capstone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    private final UserService userService;



    @Override
    public StoreDto createStore(StoreDto storeDto, Users user) {
        Store store = StoreMapper.toEntity(storeDto, user);
        return StoreMapper.toDto(storeRepository.save(store));
    }

    @Override
    public StoreDto getStoreById(Long id) throws Exception {

        Store store = storeRepository.findById(id).orElseThrow(
                ()-> new Exception("Store not found...")
        );
        return StoreMapper.toDto(store);
    }

    @Override
    public List<StoreDto> getAllStores() {
        List<Store> dtos = storeRepository.findAll();
        return dtos.stream().map(StoreMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Store getStoreByAdmin() throws Exception {
        Users admin = userService.getCurrentUser();

        return storeRepository.findByStoreAdminId(admin.getId());
    }

    @Override
    public StoreDto updateStore(Long id, StoreDto storeDto) throws Exception {
        Users currentUser = userService.getCurrentUser();

        Store existing = storeRepository.findByStoreAdminId(currentUser.getId());

        if(existing == null){
            throw new Exception("Store not found...");
        }

        existing.setBrand(storeDto.getBrand());
        existing.setDescription(storeDto.getDescription());

        if(storeDto.getStoreType() != null){
            existing.setStoreType(storeDto.getStoreType());
        }

        if(storeDto.getContact() != null){
            StoreContact contact = StoreContact.builder()
                    .address(storeDto.getContact().getAddress())
                    .phone(storeDto.getContact().getPhone())
                    .email(storeDto.getContact().getEmail())
                    .build();
            existing.setContact(contact);
        }

        Store updated = storeRepository.save(existing);
        return StoreMapper.toDto(updated);
    }

    @Override
    public void deleteStore(Long id) throws Exception {
        Store store = getStoreByAdmin();
        storeRepository.delete(store);
    }

    @Override
    public StoreDto moderateStore(Long id, StoreStatus action) throws Exception {
        Store store = storeRepository.findById(id).orElseThrow(
                ()-> new Exception("Store not found...")
        );
        store.setStatus(action);
        Store  updated = storeRepository.save(store);
        return StoreMapper.toDto(updated);
    }

    @Override
    public StoreDto getStoreByEmployee() throws Exception {
        Users currentUser = userService.getCurrentUser();
        if(currentUser == null) throw new UserException("You don't have an access to this Store.");
        return StoreMapper.toDto(currentUser.getStore());
    }


}
