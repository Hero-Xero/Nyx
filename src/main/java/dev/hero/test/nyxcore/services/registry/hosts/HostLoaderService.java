package dev.hero.test.nyxcore.services.registry.hosts;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hero.test.nyxcore.config.ConfigProperties;
import dev.hero.test.nyxcore.dto.HostDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HostLoaderService {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final ConfigProperties conf;

    public List<HostDto> loadHosts() throws IOException {
        Path hostsPath = conf.hosts().path();
        File hostFile = hostsPath.toFile();

        if (!hostFile.exists()) {
            throw new IllegalStateException("Cannot find external hosts file: " + hostFile.getAbsolutePath());
        }

        List<HostDto> hosts = Arrays.asList(objectMapper.readValue(hostFile, HostDto[].class));

        for (HostDto host : hosts) {
            Set<ConstraintViolation<HostDto>> violations = validator.validate(host);
            if (!violations.isEmpty()) {
                String messages = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                throw new IllegalStateException("Invalid host config: " + messages);
            }

            Path key = Path.of(host.getKeyPath());
            if (!Files.exists(key)) {
                throw new IllegalStateException("SSH key does not exist for host " + host.getName() + " at path " + host.getKeyPath());
            }
            if (!Files.isRegularFile(key)) {
                throw new IllegalStateException("SSH key path is a directory, not a file: " + host.getKeyPath());
            }
        }

        return hosts;
    }
}
