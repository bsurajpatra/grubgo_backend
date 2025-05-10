package klu.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import klu.model.User;
import klu.repository.UserRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/menu")
    public ResponseEntity<?> getDefaultMenu() {
        Map<String, Object> response = new HashMap<>();
        response.put("menuItems", new String[] {"Login", "Register"});
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/menu")
    public ResponseEntity<?> getDashboardMenu(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            logger.warn("No email provided in request");
            response.put("menuItems", new String[] {"Login", "Register"});
            return ResponseEntity.ok(response);
        }
        
        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.warn("User not found for email: {}", email);
            response.put("error", "User not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        logger.info("Returning menu items for user with role: {}", user.getRole());
        
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        
        switch (user.getRole()) {
            case "CUSTOMER":
                response.put("menuItems", new String[] {
                    "Browse Restaurants", 
                    "Order History", 
                    "Profile"
                });
                break;
            case "RESTAURANT_OWNER":
                response.put("menuItems", new String[] {
                    "View Orders", 
                    "Update Menu", 
                    "Profile"
                });
                break;
            case "DELIVERY_PARTNER":
                response.put("menuItems", new String[] {
                    "View New Deliveries", 
                    "Delivery History", 
                    "Profile"
                });
                break;
            case "COMMUNITY_PRESIDENT":
                response.put("menuItems", new String[] {
                    "View Restaurants/Partners", 
                    "Set Local Commission", 
                    "Profile"
                });
                break;
            case "SUPER_ADMIN":
                response.put("menuItems", new String[] {
                    "Manage Users", 
                    "Manage Restaurants", 
                    "Manage Delivery Partners", 
                    "Manage Community Presidents",
                    "Profile"
                });
                break;
            default:
                response.put("menuItems", new String[] {"Profile"});
        }
        
        return ResponseEntity.ok(response);
    }
} 