package klu.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import klu.model.User;
import klu.repository.UserRepository;
import klu.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5173", "https://grubgo-rosy.vercel.app"}, allowCredentials = "true")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
        String response = userService.signIn(user.getEmail(), user.getPassword());
        String[] parts = response.split("::", 2);
        
        Map<String, Object> responseMap = new HashMap<>();
        if (parts[0].equals("200")) {
            User authenticatedUser = userRepository.findByEmail(user.getEmail());
            responseMap.put("success", true);
            responseMap.put("role", authenticatedUser.getRole());
            responseMap.put("email", user.getEmail());
            responseMap.put("name", authenticatedUser.getName());
            responseMap.put("userId", authenticatedUser.getId());
            return ResponseEntity.ok(responseMap);
        } else {
            responseMap.put("success", false);
            responseMap.put("error", parts[1]);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            String result = userService.registerUser(user);
            String[] parts = result.split("::", 2);
            
            if (parts[0].equals("200")) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("message", parts[1]);
                responseMap.put("email", user.getEmail());
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(parts[1]);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during registration: " + e.getMessage());
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Email not provided"));
        }
        
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("address", user.getAddress());
        response.put("role", user.getRole());
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, Object> requestData) {
        String email = (String) requestData.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Email not provided"));
        }
        
        User existingUser = userRepository.findByEmail(email);
        
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
        }
        
        if (requestData.containsKey("name")) {
            existingUser.setName((String) requestData.get("name"));
        }
        
        if (requestData.containsKey("phoneNumber")) {
            existingUser.setPhoneNumber((String) requestData.get("phoneNumber"));
        }
        
        if (requestData.containsKey("address")) {
            existingUser.setAddress((String) requestData.get("address"));
        }
        
        userRepository.save(existingUser);
        
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        String email = passwordData.get("email");
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (email == null || currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email, current password and new password are required"));
        }
        
        try {
            boolean success = userService.changePassword(email, currentPassword, newPassword);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Current password is incorrect"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to change password: " + e.getMessage()));
        }
    }
    
    @PostMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Email not provided"));
        }
        
        try {
            boolean success = userService.deleteUserAccount(email);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete account: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}