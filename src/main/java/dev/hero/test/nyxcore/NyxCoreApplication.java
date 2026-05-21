package dev.hero.test.nyxcore;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hero.test.nyxcore.config.FileBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
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