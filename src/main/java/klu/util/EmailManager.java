package klu.util;

import org.springframework.stereotype.Component;

@Component
public class EmailManager {
    public String sendEmail(String to, String subject, String body) {
        return "Email sent successfully";
    }
}
