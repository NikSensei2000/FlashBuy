package com.flashbuy.inventoryservice.controller;

import com.flashbuy.inventoryservice.dto.ReservationRequest;
import com.flashbuy.inventoryservice.dto.ReservationResponse;
import com.flashbuy.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveInventory(@Valid @RequestBody ReservationRequest request) {
        log.info("Received inventory reservation request: {}", request);
        ReservationResponse response = inventoryService.reserveInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory Service is UP");
    }
}

