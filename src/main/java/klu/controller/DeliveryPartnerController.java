package klu.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery-partner")
public class DeliveryPartnerController {

    @GetMapping("/deliveries/new/{partnerId}")
    public ResponseEntity<?> getNewDeliveries(@PathVariable Long partnerId) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/deliveries/history/{partnerId}")
    public ResponseEntity<?> getDeliveryHistory(@PathVariable Long partnerId) {
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/deliveries/{deliveryId}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable Long deliveryId, @RequestBody Map<String, String> status) {
        return ResponseEntity.ok().build();
    }
} 