package iuh.fit.edu.vn.shippingservice.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import iuh.fit.edu.vn.shippingservice.dto.ShipmentRequest;
import iuh.fit.edu.vn.shippingservice.dto.ShipmentStatusUpdate;
import iuh.fit.edu.vn.shippingservice.model.Shipment;
import iuh.fit.edu.vn.shippingservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class shipmentController {
    private final ShipmentService shipmentService;

    @GetMapping
    @RateLimiter(name = "shippingApi")
    public ResponseEntity<List<Shipment>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id) {
        return shipmentService.getShipmentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Shipment>> getShipmentsByOrderId(@PathVariable Long orderId) {
        List<Shipment> shipments = shipmentService.getShipmentsByOrderId(orderId);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Shipment> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        return shipmentService.getShipmentByTrackingNumber(trackingNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CompletableFuture<Shipment>> createShipment(@RequestBody ShipmentRequest request) {
        CompletableFuture<Shipment> result = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/status")
    public ResponseEntity<Shipment> updateShipmentStatus(@RequestBody ShipmentStatusUpdate update) {
        Shipment updatedShipment = shipmentService.updateShipmentStatus(update);
        return ResponseEntity.ok(updatedShipment);
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<Boolean> processShipment(@PathVariable Long id) {
        boolean result = shipmentService.processShipment(id);
        if (result) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
        }
    }
}
