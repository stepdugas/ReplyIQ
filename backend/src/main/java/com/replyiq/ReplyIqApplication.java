package com.replyiq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class ReplyIqApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReplyIqApplication.class, args);
        log.info("ReplyIQ backend started successfully on port 8080");
    }
}
