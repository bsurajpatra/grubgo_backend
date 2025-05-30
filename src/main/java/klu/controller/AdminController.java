package klu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import klu.model.User;
import klu.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // Use the repository method to directly fetch users with CUSTOMER role
        List<User> customers = userRepository.findByRole("CUSTOMER");
        return ResponseEntity.ok(customers);
    }
    
    @GetMapping("/restaurant-owners")
    public ResponseEntity<List<User>> getRestaurantOwners() {
        // Fetch users with RESTAURANT_OWNER role
        List<User> restaurantOwners = userRepository.findByRole("RESTAURANT_OWNER");
        return ResponseEntity.ok(restaurantOwners);
    }

    @GetMapping("/community-presidents")
    public ResponseEntity<List<User>> getCommunityPresidents() {
        List<User> communityPresidents = userRepository.findByRole("COMMUNITY_PRESIDENT");
        return ResponseEntity.ok(communityPresidents);
    }
} 