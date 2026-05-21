package dev.hero.test.nyxcore.services.registry.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hero.test.nyxcore.config.ConfigProperties;
import dev.hero.test.nyxcore.dto.CommandDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandLoaderService {

    private final ObjectMapper mapper;
    private final Validator validator;
    private final ConfigProperties conf;

    public List<CommandDto> loadCommands() throws IOException {
        File commandsFile = conf.commands().path().toFile();

        if (!commandsFile.exists()) {
            throw new IllegalStateException("Cannot find external commands file : " + commandsFile.getAbsolutePath());
        }

        List<CommandDto> commands = Arrays.asList(
                mapper.readValue(commandsFile, CommandDto[].class)
        );

        List<String> allErrors = new ArrayList<>();

        for (int i = 0; i < commands.size(); i++) {
            CommandDto cmd = commands.get(i);
            Set<ConstraintViolation<CommandDto>> violations = validator.validate(cmd);

            if (!violations.isEmpty()) {
                String cmdErrors = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                allErrors.add("Command '" + cmd.getName() + "' (Index " + i + "): " + cmdErrors);
            }
        }

        if (!allErrors.isEmpty()) {
            throw new IllegalStateException("Command Validation Failed:\n" + String.join("\n", allErrors));
        }

        return commands;
    }
}
