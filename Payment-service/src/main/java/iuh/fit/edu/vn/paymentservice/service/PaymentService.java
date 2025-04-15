package iuh.fit.edu.vn.paymentservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import iuh.fit.edu.vn.paymentservice.model.Payment;
import iuh.fit.edu.vn.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@Slf4j

public class PaymentService {

    private final PaymentRepository paymentRepository;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @CircuitBreaker(name = "payment", fallbackMethod = "processPaymentFallback")
    @TimeLimiter(name = "payment")
    @Retry(name = "payment")
    public CompletableFuture<Payment> processPayment(Payment payment) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate payment processing
            try {
                // Simulate a 3rd party payment API call
                Thread.sleep(500);

                // In a real scenario, we would integrate with a payment gateway
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setTransactionId("TXN-" + System.currentTimeMillis());

                log.info("Payment processed successfully: {}", payment);
                return paymentRepository.save(payment);
            } catch (Exception e) {
                log.error("Error processing payment: {}", e.getMessage());
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setNotes("Payment processing failed: " + e.getMessage());
                return paymentRepository.save(payment);
            }
        });
    }

    public CompletableFuture<Payment> processPaymentFallback(Payment payment, Exception e) {
        log.warn("Fallback for payment processing activated due to: {}", e.getMessage());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setNotes("Payment processing temporarily unavailable. Will retry later.");
        Payment savedPayment = paymentRepository.save(payment);
        return CompletableFuture.completedFuture(savedPayment);
    }

    public Payment refundPayment(Long paymentId, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            payment.setNotes("Refunded: " + reason);
            return paymentRepository.save(payment);
        }
        throw new RuntimeException("Payment not found with id: " + paymentId);
    }
}
