package klu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

@Configuration
public class EnvConfig {

    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);

    @PostConstruct
    public void init() {
        try {
            logger.info("Loading environment variables from .env file");
            Dotenv dotenv = Dotenv.configure().load();
            
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
                logger.info("Loaded environment variable: {}", entry.getKey());
            });
            
            logger.info("Environment variables loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load .env file: {}", e.getMessage());
            throw new RuntimeException("Could not load required environment variables", e);
        }
    }
}