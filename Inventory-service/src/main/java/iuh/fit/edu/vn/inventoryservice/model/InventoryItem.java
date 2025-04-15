package iuh.fit.edu.vn.inventoryservice.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String sku;
    private String name;
    private Integer quantity;
    private Integer reservedQuantity;
    private String location;
    private LocalDateTime lastUpdated;
    private String notes;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    public enum InventoryStatus {
        IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    }

    public boolean isAvailable(int requiredQuantity) {
        return quantity - reservedQuantity >= requiredQuantity;
    }

    public void updateStatus() {
        int availableQuantity = quantity - reservedQuantity;
        if (availableQuantity <= 0) {
            this.status = InventoryStatus.OUT_OF_STOCK;
        } else if (availableQuantity < 5) { // Threshold for low stock
            this.status = InventoryStatus.LOW_STOCK;
        } else {
            this.status = InventoryStatus.IN_STOCK;
        }
    }
}
