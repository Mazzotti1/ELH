package com.elh.poll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PollSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PollSchedulerApplication.class, args);
    }
}
