package klu.util;

import org.springframework.stereotype.Component;

@Component
public class EmailManager {
    public String sendEmail(String to, String subject, String body) {
        // Implement your email sending logic here
        // For example, using JavaMailSender
        return "Email sent successfully"; // Return appropriate response
    }
}
