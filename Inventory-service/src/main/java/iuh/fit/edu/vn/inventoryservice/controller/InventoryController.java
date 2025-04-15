package iuh.fit.edu.vn.inventoryservice.controller;


import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import iuh.fit.edu.vn.inventoryservice.dto.InventoryRequest;
import iuh.fit.edu.vn.inventoryservice.model.InventoryItem;
import iuh.fit.edu.vn.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping
    @RateLimiter(name = "inventoryApi")
    public ResponseEntity<List<InventoryItem>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getItemById(@PathVariable Long id) {
        return inventoryService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryItem> getItemByProductId(@PathVariable Long productId) {
        return inventoryService.getItemByProductId(productId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<InventoryItem> getItemBySku(@PathVariable String sku) {
        return inventoryService.getItemBySku(sku)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @PostMapping
    public ResponseEntity<InventoryItem> addInventoryItem(@RequestBody InventoryItem item) {
        InventoryItem savedItem = inventoryService.addInventoryItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @PostMapping("/reserve")
    public ResponseEntity<Boolean> reserveInventory(@RequestBody InventoryRequest request) {
        boolean result = inventoryService.reserveInventory(request);
        if (result) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<Boolean> confirmInventoryReduction(@RequestBody InventoryRequest request) {
        boolean result = inventoryService.confirmInventoryReduction(request);
        if (result) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }

    @PostMapping("/release")
    public ResponseEntity<Boolean> releaseReservedInventory(@RequestBody InventoryRequest request) {
        boolean result = inventoryService.releaseReservedInventory(request);
        if (result) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }
    @PostMapping("/restock")
    public ResponseEntity<Boolean> restockInventory(@RequestBody InventoryRequest request) {
        boolean result = inventoryService.restockInventory(request);
        if (result) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }
}
