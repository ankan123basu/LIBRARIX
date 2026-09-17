package com.librarix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibrarixApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibrarixApplication.class, args);
    }
}
