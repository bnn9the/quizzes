package edu.platform.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "edu.platform")
@EnableJpaRepositories(basePackages = "edu.platform")
@EntityScan(basePackages = "edu.platform")
@EnableTransactionManagement
@EnableAsync
public class CoursePlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoursePlatformApplication.class, args);
    }
}
