package iuh.fit.edu.vn.inventoryservice.service;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import iuh.fit.edu.vn.inventoryservice.dto.InventoryRequest;
import iuh.fit.edu.vn.inventoryservice.model.InventoryItem;
import iuh.fit.edu.vn.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    public List<InventoryItem> getAllItems() {
        return inventoryRepository.findAll();
    }

    public Optional<InventoryItem> getItemById(Long id) {
        return inventoryRepository.findById(id);
    }

    public Optional<InventoryItem> getItemByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public Optional<InventoryItem> getItemBySku(String sku) {
        return inventoryRepository.findBySku(sku);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public InventoryItem addInventoryItem(InventoryItem item) {
        item.setLastUpdated(LocalDateTime.now());
        item.updateStatus();
        return inventoryRepository.save(item);
    }

    @Transactional
    @CircuitBreaker(name = "inventory", fallbackMethod = "reserveInventoryFallback")
    @Retry(name = "inventory")
    public boolean reserveInventory(InventoryRequest request) {
        log.info("Attempting to reserve inventory for productId: {}, quantity: {}",
                request.getProductId(), request.getQuantity());

        Optional<InventoryItem> itemOpt = inventoryRepository.findByProductId(request.getProductId());

        if (itemOpt.isPresent()) {
            InventoryItem item = itemOpt.get();

            if (item.isAvailable(request.getQuantity())) {
                item.setReservedQuantity(item.getReservedQuantity() + request.getQuantity());
                item.setLastUpdated(LocalDateTime.now());
                item.updateStatus();
                inventoryRepository.save(item);
                log.info("Successfully reserved inventory for productId: {}", request.getProductId());
                return true;
            } else {
                log.warn("Insufficient inventory for productId: {}", request.getProductId());
                return false;
            }
        } else {
            log.warn("Product not found in inventory: {}", request.getProductId());
            return false;
        }
    }

    public boolean reserveInventoryFallback(InventoryRequest request, Exception e) {
        log.error("Fallback for reserve inventory activated due to: {}", e.getMessage());
        // In a fallback scenario, we might want to track the failed reservation
        // and trigger a manual review or notification
        return false;
    }

    @Transactional
    public boolean confirmInventoryReduction(InventoryRequest request) {
        Optional<InventoryItem> itemOpt = inventoryRepository.findByProductId(request.getProductId());

        if (itemOpt.isPresent()) {
            InventoryItem item = itemOpt.get();

            // Reduce the actual quantity and remove the reservation
            item.setQuantity(item.getQuantity() - request.getQuantity());
            item.setReservedQuantity(item.getReservedQuantity() - request.getQuantity());
            item.setLastUpdated(LocalDateTime.now());
            item.updateStatus();

            inventoryRepository.save(item);
            log.info("Confirmed inventory reduction for productId: {}", request.getProductId());
            return true;
        }

        return false;
    }

    @Transactional
    public boolean releaseReservedInventory(InventoryRequest request) {
        Optional<InventoryItem> itemOpt = inventoryRepository.findByProductId(request.getProductId());

        if (itemOpt.isPresent()) {
            InventoryItem item = itemOpt.get();

            // Remove the reservation
            item.setReservedQuantity(Math.max(0, item.getReservedQuantity() - request.getQuantity()));
            item.setLastUpdated(LocalDateTime.now());
            item.updateStatus();

            inventoryRepository.save(item);
            log.info("Released reserved inventory for productId: {}", request.getProductId());
            return true;
        }

        return false;
    }

    @Transactional
    public boolean restockInventory(InventoryRequest request) {
        Optional<InventoryItem> itemOpt = inventoryRepository.findByProductId(request.getProductId());

        if (itemOpt.isPresent()) {
            InventoryItem item = itemOpt.get();

            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setLastUpdated(LocalDateTime.now());
            item.updateStatus();

            inventoryRepository.save(item);
            log.info("Restocked inventory for productId: {}", request.getProductId());
            return true;
        }

        return false;
    }
}
