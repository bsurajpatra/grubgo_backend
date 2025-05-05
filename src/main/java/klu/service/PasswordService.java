package klu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import klu.model.User;
import klu.repository.UserRepository;

@Service
public class PasswordService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    public boolean processForgotPassword(String email) {
        User user = userRepository.findByEmail(email);
        
        if (user != null) {
            String password = user.getPassword();
            
            // Send email with password
            String subject = "GrubGo - Your Password";
            String body = "Hello " + user.getName() + ",\n\n" +
                          "Your password is: " + password + "\n\n" +
                          "For security reasons, please change your password after logging in.\n\n" +
                          "Regards,\nGrubGo Team";
            
            emailService.sendEmail(email, subject, body);
            return true;
        }
        
        return false;
    }
} 