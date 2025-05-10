package klu.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailManager {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailManager.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    public String sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            logger.info("Sending email to: {}", to);
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
            
            return "200::Email sent successfully";
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", to, e);
            return "500::Failed to send email: " + e.getMessage();
        }
    }
    
    public String sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content
            
            logger.info("Sending HTML email to: {}", to);
            mailSender.send(message);
            logger.info("HTML email sent successfully to: {}", to);
            
            return "200::Email sent successfully";
        } catch (MessagingException e) {
            logger.error("Failed to send HTML email to: {}", to, e);
            return "500::Failed to send email: " + e.getMessage();
        }
    }
}
