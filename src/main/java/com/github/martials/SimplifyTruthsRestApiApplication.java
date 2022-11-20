package com.github.martials;

import com.github.martials.enums.Language;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimplifyTruthsRestApiApplication {

    public static Language lang = Language.norwegianBokmaal; // TODO do not use public variable, pass as reference

    public static void main(String[] args) {
        SpringApplication.run(SimplifyTruthsRestApiApplication.class, args);
    }

}
