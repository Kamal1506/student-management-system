package com.sms.student_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.sms")
@EntityScan(basePackages = "com.sms.model")
@EnableAsync
@EnableJpaRepositories(basePackages = "com.sms.repository")
public class StudentManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementSystemApplication.class, args);
    }
}
