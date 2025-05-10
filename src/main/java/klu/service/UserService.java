package klu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import klu.model.User;
import klu.repository.RoleRepository;
import klu.repository.UserRepository;
import klu.util.EmailManager;
import klu.util.JwtUtil;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; 

    @Autowired
    private EmailManager emailManager;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private WelcomeEmailService welcomeEmailService;

    public String registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return "401::User E-mail already exists";
        }
        
        userRepository.save(user);
        
        // Send welcome email using the dedicated service
        welcomeEmailService.sendWelcomeEmail(user);
        
        return "200::User Registered Successfully!";
    }

    public String signIn(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            System.out.println("User not found: " + email);
            return "404::Invalid Credentials";
        }
        
        System.out.println("Stored password: " + user.getPassword());
        System.out.println("Entered password: " + password);
        
        // Plain text comparison for testing
        if (password.equals(user.getPassword())) {
            String token = jwtUtil.generateToken(email, user.getRole());
            System.out.println("Generated token: " + token);
            return "200::" + token;
        }
        
        return "404::Invalid Credentials";
    }

    public String getPassword(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            String message = "Dear " + user.getName() + "\n\nYour password is " + user.getPassword();
            return emailManager.sendEmail(email, "Password Recovery", message);
        }
        return "404::User not found";
    }
    
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return false;
        }
        
        // Verify current password
        if (!currentPassword.equals(user.getPassword())) {
            return false;
        }
        
        // Update password
        user.setPassword(newPassword);
        userRepository.save(user);
        
        return true;
    }
    
    public boolean deleteUserAccount(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return false;
        }
        
        userRepository.delete(user);
        return true;
    }
}