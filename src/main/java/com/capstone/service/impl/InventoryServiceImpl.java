package com.capstone.service.impl;

import com.capstone.mapper.InventoryMapper;
import com.capstone.model.Branch;
import com.capstone.model.Inventory;
import com.capstone.model.Product;
import com.capstone.payload.dto.InventoryDto;
import com.capstone.repository.BranchRepository;
import com.capstone.repository.InventoryRepository;
import com.capstone.repository.ProductRepository;
import com.capstone.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    @Override
    public InventoryDto createInventory(InventoryDto inventoryDto) throws Exception {
        Branch branch = branchRepository.findById(inventoryDto.getBranchId()).orElseThrow(
                ()-> new Exception("Branch does not Exist...")
        );
        Product product = productRepository.findById(inventoryDto.getProductId()).orElseThrow(
                ()-> new Exception("Product Does not Exist...")
        );
        Inventory inventory = InventoryMapper.toEntity(inventoryDto,branch,product);
        Inventory savedInventory = inventoryRepository.save(inventory);
        return InventoryMapper.toDto(savedInventory);
    }

    @Override
    public InventoryDto updateInventory(Long id,InventoryDto inventoryDto) throws Exception {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                ()-> new Exception("Inventory not Found...")
        );
        inventory.setQuantity(inventoryDto.getQuantity());
        Inventory updatedInventory =  inventoryRepository.save(inventory);
        return InventoryMapper.toDto(updatedInventory);
    }

    @Override
    public void deleteInventory(Long id) throws Exception {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                ()-> new Exception("Inventory Not Found...")
        );
        inventoryRepository.delete(inventory);
    }

    @Override
    public InventoryDto getInventoryById(Long id) throws Exception {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                ()-> new Exception("Inventory Not Found...")
        );
        return InventoryMapper.toDto(inventory);
    }

    @Override
    public InventoryDto getInventoryByProductIdAndBranchId(Long productId, Long branchId) {
        Inventory inventory = inventoryRepository.findByProductIdAndBranchId(productId,branchId);
        return InventoryMapper.toDto(inventory);
    }

    @Override
    public List<InventoryDto> getAllInventoryByBranchId(Long branchId) {
        List<Inventory> inventories = inventoryRepository.findByBranchId(branchId);
        return inventories.stream().map(InventoryMapper::toDto).collect(Collectors.toList() );
    }
}
