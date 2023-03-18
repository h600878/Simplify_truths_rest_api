package com.github.martials.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

    @Value("${martials.version}")
    private String version;

    @Value("${martials.api.dev-url}")
    private String devUrl;

    @Value("${martials.api.prod-url}")
    private String prodUrl;

    @Value("${martials.contact.name}")
    private String name;

    @Value("${martials.contact.email}")
    private String email;

    @Value("${martials.contact.url}")
    private String contactUrl;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Server URL in Production environment");

        Contact contact = new Contact();
        contact.setEmail(email);
        contact.setName(name);
        contact.setUrl(contactUrl);

        License license = new License().name("None");

        Info info = new Info()
                .title("Simplify Truth-values API")
                .version(version)
                .contact(contact)
                .description("Simplify Truth-values and generate truth tables.")
                .license(license);

        return new OpenAPI().info(info).servers(List.of(devServer, prodServer));
    }
}