package com.flashbuy.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationRequest {
    private String productId;
    private Integer quantity;
    private String orderId;
}

