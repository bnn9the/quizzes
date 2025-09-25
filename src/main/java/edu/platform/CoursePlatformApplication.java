package edu.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class CoursePlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoursePlatformApplication.class, args);
    }
}
