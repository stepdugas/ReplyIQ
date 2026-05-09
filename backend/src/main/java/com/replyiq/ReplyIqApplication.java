package com.replyiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReplyIqApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReplyIqApplication.class, args);
        System.out.println("ReplyIQ backend started successfully on port 8080");
    }
}
