package klu.controller;

import klu.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private RestaurantService restaurantService;
    
    @GetMapping("/restaurants")
    public ResponseEntity<?> getRestaurantsByLocation(@RequestParam String location) {
        return ResponseEntity.ok(restaurantService.findByLocation(location));
    }
    
    @GetMapping("/order-history/{customerId}")
    public ResponseEntity<?> getOrderHistory(@PathVariable Long customerId) {
        // Implementation to fetch order history
        return ResponseEntity.ok().build();
    }
} 