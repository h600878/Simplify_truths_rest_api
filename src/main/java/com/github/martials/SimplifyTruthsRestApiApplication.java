package com.github.martials;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class SimplifyTruthsRestApiApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SimplifyTruthsRestApiApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(@NotNull SpringApplicationBuilder builder) {
        return builder.sources(SimplifyTruthsRestApiApplication.class);
    }

}
