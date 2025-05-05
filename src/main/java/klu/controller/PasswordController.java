package klu.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import klu.dto.ForgotPasswordRequest;
import klu.service.PasswordService;

@RestController
@RequestMapping("/api/password")
@CrossOrigin(origins = "http://localhost:3000")
public class PasswordController {

    @Autowired
    private PasswordService passwordService;
    
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        boolean result = passwordService.processForgotPassword(request.getEmail());
        
        if (result) {
            return ResponseEntity.ok().body(Map.of("message", "Password sent to your email"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Email not found"));
        }
    }
} 