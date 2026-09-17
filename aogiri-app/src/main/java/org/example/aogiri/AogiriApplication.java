package org.example.aogiri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "org.example")
public class AogiriApplication {
    public static void main(String[] args) {
        SpringApplication.run(AogiriApplication.class, args);
    }
}