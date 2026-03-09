package com.capstone.service;


import com.capstone.domain.StoreStatus;
import com.capstone.model.Store;
import com.capstone.model.Users;
import com.capstone.payload.dto.StoreDto;

import java.util.List;

public interface StoreService {
    StoreDto createStore(StoreDto storeDto, Users user);
    StoreDto getStoreById(Long id) throws Exception;
    List<StoreDto> getAllStores();
    Store getStoreByAdmin() throws Exception;
    StoreDto getStoreByEmployee() throws Exception;
    StoreDto updateStore(Long id, StoreDto storeDto) throws Exception;
    void deleteStore(Long id) throws Exception;
//    void deleteStore();
//    UserDto addEmployee(Long id, UserDto userDto) throws Exception;
//    List<UserDto> getEmployeesByStore(Long storeId) throws UserException;

    // Approve Status
    StoreDto moderateStore(Long id, StoreStatus action) throws Exception;
}
