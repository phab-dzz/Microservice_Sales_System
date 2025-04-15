package iuh.fit.edu.vn.inventoryservice.repository;

import iuh.fit.edu.vn.inventoryservice.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findBySku(String sku);
    Optional<InventoryItem> findByProductId(Long productId);
    List<InventoryItem> findByStatus(InventoryItem.InventoryStatus status);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity - i.reservedQuantity < 5 AND i.quantity - i.reservedQuantity > 0")
    List<InventoryItem> findLowStockItems();
}
