package klu.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<?> getDashboardMenu() {
        Map<String, Object> response = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.info("Auth principal: {}", auth.getPrincipal());
            logger.info("Auth authorities: {}", auth.getAuthorities());
            logger.info("Auth name: {}", auth.getName());
        } else {
            logger.warn("No authentication found in SecurityContext");
        }
        
        String email = null;
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            email = auth.getName();
        } else {
            logger.warn("User is not authenticated or is anonymous");
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