package iuh.fit.edu.vn.apigateway.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    @GetMapping("/payment")
    public String paymentServiceFallback() {
        return "Payment Service không khả dụng. Vui lòng thử lại sau.";
    }

    @GetMapping("/inventory")
    public String inventoryServiceFallback() {
        return "Inventory Service không khả dụng. Vui lòng thử lại sau.";
    }

    @GetMapping("/shipping")
    public String shippingServiceFallback() {
        return "Shipping Service không khả dụng. Vui lòng thử lại sau.";
    }
}
