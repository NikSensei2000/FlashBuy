package com.flashbuy.paymentservice.service;

import com.flashbuy.paymentservice.dto.PaymentRequest;
import com.flashbuy.paymentservice.dto.PaymentResponse;
import com.flashbuy.paymentservice.entity.Payment;
import com.flashbuy.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${payment.simulate.delay:0}")
    private long simulateDelay;

    @Value("${payment.simulate.failure:false}")
    private boolean simulateFailure;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for user: {}, amount: {}", request.getUserId(), request.getAmount());

        // Simulate payment processing delay (for failure scenario testing)
        if (simulateDelay > 0) {
            try {
                log.info("Simulating payment delay: {}ms", simulateDelay);
                Thread.sleep(simulateDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Payment processing interrupted", e);
            }
        }

        // Simulate payment failure (for failure scenario testing)
        if (simulateFailure) {
            log.error("Simulating payment failure");
            Payment payment = new Payment();
            payment.setUserId(request.getUserId());
            payment.setOrderId(request.getOrderId());
            payment.setAmount(request.getAmount());
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);

            return new PaymentResponse(
                    payment.getPaymentId(),
                    "FAILED",
                    "Payment processing failed (simulated)"
            );
        }

        // Normal payment processing - simulate successful payment
        Payment payment = new Payment();
        payment.setUserId(request.getUserId());
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus(Payment.PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment processed successfully: {}", savedPayment.getPaymentId());

        return new PaymentResponse(
                savedPayment.getPaymentId(),
                "SUCCESS",
                "Payment processed successfully"
        );
    }
}

