package dev.hero.test.nyxcore.services.registry.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hero.test.nyxcore.config.ConfigProperties;
import dev.hero.test.nyxcore.dto.DashboardDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardLoaderService {

    private final ObjectMapper mapper;
    private final Validator validator;
    private final ConfigProperties conf;

    public DashboardDto loadDashboard() throws IOException {
        log.info("Loading dashboard configuration from: {}", conf.dashboard().path());

        DashboardDto dto = mapper.readValue(
                Files.readString(conf.dashboard().path()),
                DashboardDto.class
        );

        Set<ConstraintViolation<DashboardDto>> violations = validator.validate(dto);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            throw new IllegalStateException("Dashboard Validation Failed: " + errorMessage);
        }

        log.info("Dashboard '{}' loaded successfully.", dto.uiSettings().title());
        return dto;
    }
}
