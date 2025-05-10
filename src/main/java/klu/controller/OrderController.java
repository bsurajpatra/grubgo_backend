package klu.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import klu.model.Order;
import klu.model.User;
import klu.repository.UserRepository;
import klu.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> orderRequest, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        try {
            // Get user from authentication
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            // Extract order details from request
            Long restaurantId = Long.valueOf(orderRequest.get("restaurant_id").toString());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderRequest.get("items");
            
            // Validate that each item has the required fields including item_name
            for (Map<String, Object> item : items) {
                if (!item.containsKey("menu_item_id") || 
                    !item.containsKey("quantity") || 
                    !item.containsKey("item_price")) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid item data", 
                        "message", "Each item must have menu_item_id, quantity, and item_price"
                    ));
                }
                
                // Ensure item_name is present
                if (!item.containsKey("item_name")) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid item data", 
                        "message", "Each item must have an item_name"
                    ));
                }
            }
            
            Double totalAmount = Double.valueOf(orderRequest.get("total_amount").toString());
            String deliveryAddress = (String) orderRequest.get("delivery_address");
            
            // Create order
            Order order = orderService.createOrder(
                user.getId(), 
                restaurantId, 
                items, 
                totalAmount, 
                deliveryAddress
            );
            
            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("order_id", order.getId());
            response.put("status", order.getStatus());
            response.put("message", "Order placed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to place order", 
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/{orderId}/confirmation")
    public ResponseEntity<?> getOrderConfirmation(@PathVariable Long orderId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        try {
            // Get user from authentication
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            // Get order confirmation
            Map<String, Object> confirmation = orderService.getOrderConfirmation(orderId, user.getId());
            
            if (confirmation == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Order not found or access denied"));
            }
            
            return ResponseEntity.ok(confirmation);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to retrieve order confirmation", 
                "message", e.getMessage()
            ));
        }
    }
} 