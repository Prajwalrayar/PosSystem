package com.zosh.service;


import com.zosh.domain.StoreStatus;
import com.zosh.exception.ResourceNotFoundException;
import com.zosh.exception.UserException;
import com.zosh.modal.Store;
import com.zosh.modal.User;
import com.zosh.payload.dto.StoreDTO;
import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.request.CommissionUpdateRequest;
import com.zosh.payload.request.StoreSettingsRequest;
import com.zosh.payload.response.CommissionResponse;
import com.zosh.payload.response.StoreSettingsResponse;

import java.util.List;

public interface StoreService {
    StoreDTO createStore(StoreDTO storeDto, User user);
    StoreDTO getStoreById(Long id) throws ResourceNotFoundException;
    List<StoreDTO> getAllStores(StoreStatus status);
    Store getStoreByAdminId() throws UserException;
    StoreDTO getStoreByEmployee() throws UserException;
    StoreDTO updateStore(Long id, StoreDTO storeDto) throws ResourceNotFoundException, UserException;
    StoreSettingsResponse getStoreSettings(Long storeId) throws UserException;
    void deleteStore() throws ResourceNotFoundException, UserException;
    UserDTO addEmployee(Long id, UserDTO userDto) throws UserException;
    List<UserDTO> getEmployeesByStore(Long storeId) throws UserException;

    StoreDTO moderateStore(Long storeId, StoreStatus action) throws ResourceNotFoundException;
    List<CommissionResponse> getCommissionList();
    CommissionResponse updateCommission(Long storeId, CommissionUpdateRequest request) throws ResourceNotFoundException;
    StoreSettingsResponse updateStoreSettings(Long storeId, StoreSettingsRequest request) throws ResourceNotFoundException;

}

