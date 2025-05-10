package klu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import klu.model.User;
import klu.repository.UserRepository;

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
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/delivery-partners")
    public ResponseEntity<?> getAllDeliveryPartners() {
        return ResponseEntity.ok(userRepository.findByRole("DELIVERY_PARTNER"));
    }
    
    @GetMapping("/community-presidents")
    public ResponseEntity<?> getAllCommunityPresidents() {
        return ResponseEntity.ok(userRepository.findByRole("COMMUNITY_PRESIDENT"));
    }
} 