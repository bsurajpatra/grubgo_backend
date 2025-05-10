package klu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import klu.model.User;
import klu.repository.UserRepository;
import klu.util.EmailManager;

@Service
public class PasswordService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailManager emailManager;
    
    public String processForgotPassword(String email) {
        logger.info("Processing forgot password request for email: {}", email);
        
        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.warn("User not found for email: {}", email);
            return "404::User not found";
        }
        
        // Create email content
        String subject = "Password Recovery - GrubGo";
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Dear ").append(user.getName()).append(",\n\n");
        messageBuilder.append("We received a request to recover your password for your GrubGo account.\n\n");
        messageBuilder.append("Your current password is: ").append(user.getPassword()).append("\n\n");
        messageBuilder.append("For security reasons, we recommend changing your password after logging in.\n\n");
        messageBuilder.append("If you did not request this password recovery, please secure your account immediately.\n\n");
        messageBuilder.append("Best regards,\n");
        messageBuilder.append("The GrubGo Team");
        
        // Send email
        String result = emailManager.sendEmail(email, subject, messageBuilder.toString());
        logger.info("Password recovery email result: {}", result);
        
        return result;
    }
} 