package com.flashbuy.orderservice.service;

import com.flashbuy.orderservice.dto.*;
import com.flashbuy.orderservice.entity.Order;
import com.flashbuy.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${payment.service.url:http://localhost:8081}")
    private String paymentServiceUrl;

    @Value("${inventory.service.url:http://localhost:8082}")
    private String inventoryServiceUrl;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for user: {}, product: {}, quantity: {}", 
                request.getUserId(), request.getProductId(), request.getQuantity());

        // Step 1: Validate request (implicit via @Valid in controller)

        // Step 2: Call Payment Service
        PaymentRequest paymentRequest = new PaymentRequest(
                request.getUserId(),
                null, // Will be set after order creation
                request.getTotalAmount()
        );

        log.info("Calling Payment Service: {}", paymentServiceUrl + "/payments");
        PaymentResponse paymentResponse = restTemplate.postForObject(
                paymentServiceUrl + "/payments",
                paymentRequest,
                PaymentResponse.class
        );

        if (paymentResponse == null || !"SUCCESS".equals(paymentResponse.getStatus())) {
            log.error("Payment failed: {}", paymentResponse != null ? paymentResponse.getMessage() : "Null response");
            throw new RuntimeException("Payment processing failed: " + 
                    (paymentResponse != null ? paymentResponse.getMessage() : "Service unavailable"));
        }

        log.info("Payment successful: {}", paymentResponse.getPaymentId());

        // Step 3: Call Inventory Service
        InventoryReservationRequest inventoryRequest = new InventoryReservationRequest(
                request.getProductId(),
                request.getQuantity(),
                null // Will be set after order creation
        );

        log.info("Calling Inventory Service: {}", inventoryServiceUrl + "/inventory/reserve");
        InventoryReservationResponse inventoryResponse = restTemplate.postForObject(
                inventoryServiceUrl + "/inventory/reserve",
                inventoryRequest,
                InventoryReservationResponse.class
        );

        if (inventoryResponse == null || !"RESERVED".equals(inventoryResponse.getStatus())) {
            log.error("Inventory reservation failed: {}", 
                    inventoryResponse != null ? inventoryResponse.getMessage() : "Null response");
            throw new RuntimeException("Inventory reservation failed: " + 
                    (inventoryResponse != null ? inventoryResponse.getMessage() : "Service unavailable"));
        }

        log.info("Inventory reserved: {}", inventoryResponse.getReservationId());

        // Step 4: Save Order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setPaymentId(paymentResponse.getPaymentId());
        order.setInventoryReservationId(inventoryResponse.getReservationId());

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getId());

        // Step 5: Return response
        return OrderResponse.from(savedOrder);
    }
}

