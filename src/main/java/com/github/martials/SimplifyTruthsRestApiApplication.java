package com.github.martials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimplifyTruthsRestApiApplication {

    public static Language lang = Language.norwegianBokmaal;

    public static void main(String[] args) {
        SpringApplication.run(SimplifyTruthsRestApiApplication.class, args);
    }

}
