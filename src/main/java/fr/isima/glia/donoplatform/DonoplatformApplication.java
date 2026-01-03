package fr.isima.glia.donoplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DonoplatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(DonoplatformApplication.class, args);
    }
}