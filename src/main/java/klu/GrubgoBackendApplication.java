package klu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class GrubgoBackendApplication {

    public static void main(String[] args) {
        // Load environment variables before Spring starts
        try {
            System.out.println("Loading environment variables from .env file...");
            Dotenv dotenv = Dotenv.configure().load();
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
            System.out.println("Environment variables loaded successfully");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load environment variables from .env file");
            System.err.println("Cause: " + e.getMessage());
            System.err.println("Please ensure the .env file exists in the project root directory");
            System.exit(1);
        }
        
        SpringApplication.run(GrubgoBackendApplication.class, args);
    }
}