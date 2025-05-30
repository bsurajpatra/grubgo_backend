package klu.controller;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import klu.model.User;
import klu.repository.UserRepository;
import klu.service.OrderService;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/order-history")
    public ResponseEntity<?> getOrderHistory() {
        try {
          
            List<Map<String, Object>> orderHistory = orderService.getAllOrderHistory();
            
            if (orderHistory == null || orderHistory.isEmpty()) {
               
                Map<String, Object> mockOrder = new HashMap<>();
                mockOrder.put("order_id", 1);
                mockOrder.put("customer_name", "Test User");
                mockOrder.put("customer_email", "test@example.com");
                mockOrder.put("item_name", "Test Item");
                mockOrder.put("total_amount", 25.99);
                mockOrder.put("status", "Delivered");
                mockOrder.put("order_date", new Date());
                mockOrder.put("delivery_address", "123 Test St");
                
                return ResponseEntity.ok(Collections.singletonList(mockOrder));
            }
            
            return ResponseEntity.ok(orderHistory);
        } catch (Exception e) {
            System.err.println("Error fetching order history: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch order history", "message", e.getMessage()));
        }
    }
    
    
    @GetMapping("/authenticated-order-history")
    public ResponseEntity<?> getAuthenticatedOrderHistory(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Map<String, Object>> orderHistory = orderService.getDetailedOrderHistoryByCustomerId(user.getId());
        
        if (orderHistory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(orderHistory);
    }
} 
