package iuh.fit.edu.vn.shippingservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import iuh.fit.edu.vn.shippingservice.client.InventoryClient;
import iuh.fit.edu.vn.shippingservice.dto.InventoryRequest;
import iuh.fit.edu.vn.shippingservice.dto.ShipmentRequest;
import iuh.fit.edu.vn.shippingservice.dto.ShipmentStatusUpdate;
import iuh.fit.edu.vn.shippingservice.model.Shipment;
import iuh.fit.edu.vn.shippingservice.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final InventoryClient inventoryClient;

    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    public Optional<Shipment> getShipmentById(Long id) {
        return shipmentRepository.findById(id);
    }

    public List<Shipment> getShipmentsByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    public Optional<Shipment> getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }

    @Transactional
    @CircuitBreaker(name = "shipping", fallbackMethod = "createShipmentFallback")
    @TimeLimiter(name = "shipping")
    @Retry(name = "shipping")
    public CompletableFuture<Shipment> createShipment(ShipmentRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Creating shipment for order: {}", request.getOrderId());

            Shipment shipment = new Shipment();
            shipment.setOrderId(request.getOrderId());
            shipment.setStatus(Shipment.ShipmentStatus.PENDING);
            shipment.setTrackingNumber(generateTrackingNumber());
            shipment.setCarrier(request.getCarrier());
            shipment.setShippingMethod(request.getShippingMethod());
            shipment.setCreatedAt(LocalDateTime.now());

            // Set estimated delivery date (e.g., 5 days from now)
            shipment.setEstimatedDelivery(LocalDateTime
                    .now().plusDays(5));

            // Set recipient details
            shipment.setRecipientName(request.getRecipientName());
            shipment.setRecipientAddress(request.getRecipientAddress());
            shipment.setRecipientCity(request.getRecipientCity());
            shipment.setRecipientState(request.getRecipientState());
            shipment.setRecipientZip(request.getRecipientZip());
            shipment.setRecipientCountry(request.getRecipientCountry());
            shipment.setNotes(request.getNotes());

            return shipmentRepository.save(shipment);
        });
    }

    public CompletableFuture<Shipment> createShipmentFallback(ShipmentRequest request, Exception e) {
        log.error("Fallback for create shipment activated due to: {}", e.getMessage());

        Shipment shipment = new Shipment();
        shipment.setOrderId(request.getOrderId());
        shipment.setStatus(Shipment.ShipmentStatus.PENDING);
        shipment.setTrackingNumber("FALLBACK-" + UUID.randomUUID().toString());
        shipment.setCarrier(request.getCarrier());
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setNotes("Shipment processing temporarily unavailable. Will process later.");

        // Set recipient details
        shipment.setRecipientName(request.getRecipientName());
        shipment.setRecipientAddress(request.getRecipientAddress());
        shipment.setRecipientCity(request.getRecipientCity());
        shipment.setRecipientState(request.getRecipientState());
        shipment.setRecipientZip(request.getRecipientZip());
        shipment.setRecipientCountry(request.getRecipientCountry());

        return CompletableFuture.completedFuture(shipmentRepository.save(shipment));
    }

    @Transactional
    public Shipment updateShipmentStatus(ShipmentStatusUpdate update) {
        log.info("Updating shipment status for shipmentId: {}, status: {}",
                update.getShipmentId(), update.getStatus());

        Optional<Shipment> shipmentOpt = shipmentRepository.findById(update.getShipmentId());

        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setStatus(update.getStatus());

            switch (update.getStatus()) {
                case SHIPPED:
                    shipment.setShippedAt(update.getUpdateTime());
                    break;
                case DELIVERED:
                    shipment.setDeliveredAt(update.getUpdateTime());
                    break;
                default:
                    // No special handling for other statuses
                    break;
            }

            if (update.getNotes() != null && !update.getNotes().isEmpty()) {
                String currentNotes = shipment.getNotes();
                String newNotes = (currentNotes != null && !currentNotes.isEmpty())
                        ? currentNotes + "\n" + update.getUpdateTime() + ": " + update.getNotes()
                        : update.getUpdateTime() + ": " + update.getNotes();
                shipment.setNotes(newNotes);
            }

            return shipmentRepository.save(shipment);
        } else {
            throw new RuntimeException("Shipment not found with id: " + update.getShipmentId());
        }
    }

    @CircuitBreaker(name = "inventory", fallbackMethod = "processShipmentFallback")
    @Retry(name = "inventory")
    public boolean processShipment(Long shipmentId) {
        log.info("Processing shipment: {}", shipmentId);

        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);

        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();

            // In a real system, this would call an order service to get the items in the order
            // For simplicity, we'll just simulate confirming inventory reduction
            try {
                // Mock inventory confirmation - in real system would get order details first
                InventoryRequest request = new InventoryRequest();
                request.setProductId(1L); // Sample product ID
                request.setQuantity(1);   // Sample quantity

                // Call inventory service to confirm inventory reduction
                Boolean result = inventoryClient.confirmInventoryReduction(request).getBody();

                if (Boolean.TRUE.equals(result)) {
                    // Update shipment status
                    shipment.setStatus(Shipment.ShipmentStatus.PROCESSING);
                    shipment.setNotes((shipment.getNotes() != null ? shipment.getNotes() + "\n" : "") +
                            "Inventory confirmed and processing started.");
                    shipmentRepository.save(shipment);
                    return true;
                } else {
                    log.warn("Failed to confirm inventory for shipment: {}", shipmentId);
                    return false;
                }
            } catch (Exception e) {
                log.error("Error calling inventory service: {}", e.getMessage());
                throw e;
            }
        } else {
            log.warn("Shipment not found with id: {}", shipmentId);
            return false;
        }
    }

    public boolean processShipmentFallback(Long shipmentId, Exception e) {
        log.error("Fallback for process shipment activated due to: {}", e.getMessage());

        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setNotes((shipment.getNotes() != null ? shipment.getNotes() + "\n" : "") +
                    "Processing temporarily unavailable. Will try later.");
            shipmentRepository.save(shipment);
        }

        return false;
    }

    private String generateTrackingNumber() {
        // Generate a unique tracking number
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
