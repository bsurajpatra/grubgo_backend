package klu.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import klu.dto.PasswordResetRequestDTO;
import klu.service.PasswordService;

@RestController
@RequestMapping("/api/password")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class PasswordController {

    @Autowired
    private PasswordService passwordService;
    
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordResetRequestDTO request) {
        String result = passwordService.processForgotPassword(request.getEmail());
        
        Map<String, String> response = new HashMap<>();
        if (result.startsWith("200")) {
            response.put("status", "success");
            response.put("message", "Password sent to your email. Please change it after logging in.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", result.substring(result.indexOf("::") + 2));
            return ResponseEntity.badRequest().body(response);
        }
    }
} 