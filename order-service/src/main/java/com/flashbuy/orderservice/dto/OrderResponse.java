package com.flashbuy.orderservice.dto;

import com.flashbuy.orderservice.entity.Order;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String userId;
    private String productId;
    private Integer quantity;
    private Double totalAmount;
    private Order.OrderStatus status;
    private String paymentId;
    private String inventoryReservationId;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentId(),
                order.getInventoryReservationId(),
                order.getCreatedAt()
        );
    }
}

