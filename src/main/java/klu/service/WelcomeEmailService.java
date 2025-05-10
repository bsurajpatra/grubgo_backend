package klu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import klu.model.User;
import klu.util.EmailManager;

@Service
public class WelcomeEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(WelcomeEmailService.class);
    
    @Autowired
    private EmailManager emailManager;
    
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to GrubGo!";
        
        // Create HTML content
        String htmlContent =
        "<!DOCTYPE html>" +
        "<html>" +
        "<head>" +
        "<style>" +
        "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
        ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
        "h1 { color: #4a90e2; }" +
        ".features { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }" +
        ".footer { margin-top: 30px; font-size: 12px; color: #777; }" +
        "</style>" +
        "</head>" +
        "<body>" +
        "<div class='container'>" +
        "<h1>Welcome to GrubGo!</h1>" +
        "<p>Dear " + user.getName() + ",</p>" +
        "<p>We're thrilled to welcome you to <strong>GrubGo</strong> — a community-driven food delivery platform designed to support local restaurants and provide a fair, affordable experience for everyone.</p>" +
    
        "<div class='features'>" +
        "<h2>As a valued GrubGo customer, you can:</h2>" +
        "<ul>" +
        "<li>Browse a wide variety of local restaurants and cuisines</li>" +
        "<li>Place food orders easily and securely</li>" +
        "<li>Track your orders live in real-time</li>" +
        "<li>Support your neighborhood restaurants and delivery workers</li>" +
        "</ul>" +
        "</div>" +
    
        "<p>GrubGo is built by the community, for the community — keeping profits local, prices fair, and service personal.</p>" +
        "<p>If you ever need help or have feedback, our support team is just a message away.</p>" +
    
        "<div class='footer'>" +
        "<p>Warm regards,<br>The GrubGo Team</p>" +
        "<p><em>Powering local food delivery, together.</em></p>" +
        "</div>" +
        "</div>" +
        "</body>" +
        "</html>";
    
        
        try {
            logger.info("Sending welcome email to: {}", user.getEmail());
            emailManager.sendHtmlEmail(user.getEmail(), subject, htmlContent);
            logger.info("Welcome email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }
} 