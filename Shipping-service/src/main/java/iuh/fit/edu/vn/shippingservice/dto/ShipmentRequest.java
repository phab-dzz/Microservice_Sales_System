package iuh.fit.edu.vn.shippingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {
    private Long orderId;
    private String recipientName;
    private String recipientAddress;
    private String recipientCity;
    private String recipientState;
    private String recipientZip;
    private String recipientCountry;
    private String carrier;
    private String shippingMethod;
    private String notes;
}
