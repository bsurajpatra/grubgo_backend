package klu.controller;

import klu.model.User;
import klu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminController {

    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @GetMapping("/restaurants")
    public ResponseEntity<?> getAllRestaurants() {
        // Implementation to fetch all restaurants
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/delivery-partners")
    public ResponseEntity<?> getAllDeliveryPartners() {
        // Implementation to fetch all delivery partners with role "DELIVERY_PARTNER"
        return ResponseEntity.ok(userRepository.findByRole("DELIVERY_PARTNER"));
    }
    
    @GetMapping("/community-presidents")
    public ResponseEntity<?> getAllCommunityPresidents() {
        // Implementation to fetch all community presidents with role "COMMUNITY_PRESIDENT"
        return ResponseEntity.ok(userRepository.findByRole("COMMUNITY_PRESIDENT"));
    }
} 