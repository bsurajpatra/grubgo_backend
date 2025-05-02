package klu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrubgoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrubgoBackendApplication.class, args);
    }

    @Test
    void contextLoads() {
    }

}
