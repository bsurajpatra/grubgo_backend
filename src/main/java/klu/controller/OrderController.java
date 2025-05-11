package klu.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import klu.model.Order;
import klu.model.OrderStatus;
import klu.model.User;
import klu.repository.UserRepository;
import klu.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5173", "https://grubgo-rosy.vercel.app"}, allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<?> getOrderHistory() {
        try {
            List<Map<String, Object>> orderHistory = orderService.getAllOrderHistory();
            
            return ResponseEntity.ok(Map.of("orders", orderHistory));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to retrieve order history", 
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> orderRequest, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            Long restaurantId = Long.valueOf(orderRequest.get("restaurant_id").toString());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderRequest.get("items");
            
            for (Map<String, Object> item : items) {
                if (!item.containsKey("menu_item_id") || 
                    !item.containsKey("quantity") || 
                    !item.containsKey("item_price")) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid item data", 
                        "message", "Each item must have menu_item_id, quantity, and item_price"
                    ));
                }
                
                if (!item.containsKey("item_name")) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid item data", 
                        "message", "Each item must have an item_name"
                    ));
                }
            }
            
            Double totalAmount = Double.valueOf(orderRequest.get("total_amount").toString());
            String deliveryAddress = (String) orderRequest.get("delivery_address");
            
            
            Order order = orderService.createOrder(
                user.getId(), 
                restaurantId, 
                items, 
                totalAmount, 
                deliveryAddress
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("order_id", order.getId());
            response.put("status", order.getStatus().getDisplayName());
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
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
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

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            String newStatus = request.get("status");
            OrderStatus status;
            
            try {
                status = OrderStatus.valueOf(newStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid status",
                    "message", "Status must be one of: PLACED, PROCESSING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED"
                ));
            }

            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("order_id", updatedOrder.getId());
            response.put("status", updatedOrder.getStatus().getDisplayName());
            response.put("message", "Order status updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update order status",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        try {
            Order cancelledOrder = orderService.cancelOrderPublic(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("order_id", cancelledOrder.getId());
            response.put("status", cancelledOrder.getStatus().getDisplayName());
            response.put("message", "Order cancelled successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to cancel order",
                "message", e.getMessage()
            ));
        }
    }
} 