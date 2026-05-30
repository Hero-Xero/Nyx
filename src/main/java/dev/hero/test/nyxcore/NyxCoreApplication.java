package dev.hero.test.nyxcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hero.test.nyxcore.config.FileBootstrap;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class NyxCoreApplication {

    public static void main(String[] args) {
        FileBootstrap.ensureConfig();

        SpringApplication.run(NyxCoreApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}