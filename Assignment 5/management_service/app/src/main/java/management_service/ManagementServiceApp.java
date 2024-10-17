package management_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class ManagementServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(ManagementServiceApp.class, args);
    }
}
