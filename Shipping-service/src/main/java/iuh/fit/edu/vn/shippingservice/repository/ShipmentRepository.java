package iuh.fit.edu.vn.shippingservice.repository;

import iuh.fit.edu.vn.shippingservice.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByOrderId(Long orderId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByStatus(Shipment.ShipmentStatus status);
}
