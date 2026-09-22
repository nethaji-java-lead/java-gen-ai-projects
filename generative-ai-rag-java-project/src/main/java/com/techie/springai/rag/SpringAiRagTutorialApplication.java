package com.techie.springai.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringAiRagTutorialApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiRagTutorialApplication.class, args);
    }
}
