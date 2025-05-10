package klu.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import klu.model.User;
import klu.repository.UserRepository;
import klu.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        String result = userService.registerUser(user);
        String[] parts = result.split("::", 2);
        
        Map<String, Object> responseMap = new HashMap<>();
        if (parts[0].equals("200")) {
            responseMap.put("message", parts[1]);
            responseMap.put("email", user.getEmail());
            return ResponseEntity.ok(responseMap);
        } else {
            responseMap.put("error", parts[1]);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }

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

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        User user = userRepository.findByEmail(email);
        
        Map<String, Object> responseMap = new HashMap<>();
        if (user != null) {
            responseMap.put("valid", true);
            responseMap.put("role", user.getRole());
            responseMap.put("name", user.getName());
            responseMap.put("userId", user.getId());
            return ResponseEntity.ok(responseMap);
        } else {
            responseMap.put("valid", false);
            responseMap.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<?> adminEndpoint() {
        return ResponseEntity.ok("Admin content");
    }
}