package com.capstone.payload.StoreAnalysis;

import com.capstone.payload.dto.BranchDto;
import com.capstone.payload.dto.ProductDto;
import com.capstone.payload.dto.RefundDto;
import com.capstone.payload.dto.UserDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StoreAlertDto {
    private List<ProductDto> lowStockAlerts;
    private List<BranchDto> noSalesToday;
    private List<RefundDto> refundSpikeAlerts;
    private List<UserDto> inactiveCashiers;

}
