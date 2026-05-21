package dev.hero.test.nyxcore.services.registry.commands;

import dev.hero.test.nyxcore.dto.CommandDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandRegistryService {

    private final CommandLoaderService loader;
    // Initialize with empty map to prevent NPE even if loading fails
    private Map<String, CommandDto> commands = Collections.emptyMap();

    @PostConstruct
    void init() {
        try {
            this.commands = loader.loadCommands().stream()
                    .collect(Collectors.toMap(CommandDto::getName, Function.identity(),
                            (a, b) -> {
                                throw new IllegalStateException("Duplicate command at " + a.getName() + " and " + b.getName());
                            }
                    ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load commands from JSON file", e);
        }
    }

    public CommandDto getCommand(String name) {
        return commands.get(name);
    }

    public Collection<CommandDto> getAllCommands() {
        return commands.values();
    }
}