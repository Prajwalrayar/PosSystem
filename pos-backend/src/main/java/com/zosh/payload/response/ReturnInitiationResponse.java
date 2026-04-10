package com.zosh.payload.response;

import com.zosh.payload.dto.OrderItemDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReturnInitiationResponse {
    private UUID returnSessionId;
    private List<OrderItemDTO> eligibleItems;
}
