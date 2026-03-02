package com.grove.chassis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.grove")
@EntityScan(basePackages = "com.grove")
@EnableJpaRepositories(basePackages = "com.grove")
public class GroveChassisApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroveChassisApplication.class, args);
    }
}
