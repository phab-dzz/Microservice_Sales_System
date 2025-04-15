package iuh.fit.edu.vn.shippingservice.dto;



import iuh.fit.edu.vn.shippingservice.model.Shipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusUpdate {
    private Long shipmentId;
    private String trackingNumber;
    private Shipment.ShipmentStatus status;
    private LocalDateTime updateTime;
    private String notes;
}
