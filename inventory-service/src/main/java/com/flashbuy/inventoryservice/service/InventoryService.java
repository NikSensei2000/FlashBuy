package com.flashbuy.inventoryservice.service;

import com.flashbuy.inventoryservice.dto.ReservationRequest;
import com.flashbuy.inventoryservice.dto.ReservationResponse;
import com.flashbuy.inventoryservice.entity.InventoryItem;
import com.flashbuy.inventoryservice.entity.Reservation;
import com.flashbuy.inventoryservice.repository.InventoryRepository;
import com.flashbuy.inventoryservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    @Value("${inventory.simulate.failure.rate:0}")
    private double simulateFailureRate;

    @Value("${inventory.default.stock:100}")
    private int defaultStock;

    @Transactional
    public ReservationResponse reserveInventory(ReservationRequest request) {
        log.info("Reserving inventory for product: {}, quantity: {}", 
                request.getProductId(), request.getQuantity());

        // Simulate random failures (for failure scenario testing)
        if (simulateFailureRate > 0) {
            Random random = new Random();
            if (random.nextDouble() < simulateFailureRate) {
                log.error("Simulating inventory reservation failure (rate: {})", simulateFailureRate);
                
                Reservation reservation = new Reservation();
                reservation.setProductId(request.getProductId());
                reservation.setQuantity(request.getQuantity());
                reservation.setOrderId(request.getOrderId());
                reservation.setStatus(Reservation.ReservationStatus.FAILED);
                reservationRepository.save(reservation);

                return new ReservationResponse(
                        reservation.getReservationId(),
                        "FAILED",
                        "Inventory reservation failed (simulated)"
                );
            }
        }

        // Check and create inventory item if it doesn't exist
        Optional<InventoryItem> itemOpt = inventoryRepository.findByProductId(request.getProductId());
        InventoryItem item;

        if (itemOpt.isEmpty()) {
            log.info("Product {} not found, creating with default stock: {}", 
                    request.getProductId(), defaultStock);
            item = new InventoryItem(request.getProductId(), defaultStock);
            item = inventoryRepository.save(item);
        } else {
            item = itemOpt.get();
        }

        // Check availability
        int availableQuantity = item.getAvailableQuantity() - item.getReservedQuantity();
        if (availableQuantity < request.getQuantity()) {
            log.error("Insufficient inventory for product: {}. Available: {}, Requested: {}", 
                    request.getProductId(), availableQuantity, request.getQuantity());
            
            Reservation reservation = new Reservation();
            reservation.setProductId(request.getProductId());
            reservation.setQuantity(request.getQuantity());
            reservation.setOrderId(request.getOrderId());
            reservation.setStatus(Reservation.ReservationStatus.FAILED);
            reservationRepository.save(reservation);

            return new ReservationResponse(
                    reservation.getReservationId(),
                    "FAILED",
                    "Insufficient inventory available"
            );
        }

        // Reserve inventory
        item.setReservedQuantity(item.getReservedQuantity() + request.getQuantity());
        inventoryRepository.save(item);

        // Create reservation record
        Reservation reservation = new Reservation();
        reservation.setProductId(request.getProductId());
        reservation.setQuantity(request.getQuantity());
        reservation.setOrderId(request.getOrderId());
        reservation.setStatus(Reservation.ReservationStatus.RESERVED);
        Reservation savedReservation = reservationRepository.save(reservation);

        log.info("Inventory reserved successfully: {}", savedReservation.getReservationId());

        return new ReservationResponse(
                savedReservation.getReservationId(),
                "RESERVED",
                "Inventory reserved successfully"
        );
    }
}

