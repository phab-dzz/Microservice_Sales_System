package iuh.fit.edu.vn.shippingservice.client;


import iuh.fit.edu.vn.shippingservice.dto.InventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service")
public class InventoryClient {

    @PostMapping("/api/inventory/confirm")
    public ResponseEntity<Boolean> confirmInventoryReduction(@RequestBody InventoryRequest request);
}
