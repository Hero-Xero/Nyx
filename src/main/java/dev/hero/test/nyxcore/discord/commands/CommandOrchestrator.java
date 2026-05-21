package dev.hero.test.nyxcore.discord.commands;

import dev.hero.test.nyxcore.dto.AlertEvent;
import dev.hero.test.nyxcore.dto.CommandDto;
import dev.hero.test.nyxcore.dto.HostDto;
import dev.hero.test.nyxcore.exceptions.ActionExecutionException;
import dev.hero.test.nyxcore.discord.helpers.ImageService;
import dev.hero.test.nyxcore.services.engine.commands.CommandBuilderService;
import dev.hero.test.nyxcore.services.engine.commands.CommandExecutionerService;
import dev.hero.test.nyxcore.services.registry.commands.CommandRegistryService;
import dev.hero.test.nyxcore.services.registry.hosts.HostRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandOrchestrator {

    private final HostRegistryService hostRegistry;
    private final CommandRegistryService commandRegistry;
    private final CommandBuilderService commandBuilder;
    private final CommandExecutionerService commandExecutioner;

    private final ApplicationEventPublisher eventPublisher;

    public void handle(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String deviceName = "Unknown Device"; // Track for the catch block

        try {
            deviceName = Optional.ofNullable(event.getOption("device"))
                    .map(OptionMapping::getAsString)
                    .orElseThrow(() -> new IllegalArgumentException("Device option is missing."));

            HostDto host = hostRegistry.getHost(deviceName);
            if (host == null) {
                throw new IllegalStateException("Host not found in registry: " + deviceName);
            }

            CommandDto commandDto = commandRegistry.getCommand(commandName);
            if (commandDto == null) {
                throw new IllegalStateException("Command not found in registry: " + commandName);
            }

            List<OptionMapping> options = event.getOptions();
            String subcommandName = event.getSubcommandName();

            ProcessBuilder pb = commandBuilder.build(commandDto, subcommandName, host, options);
            String output = commandExecutioner.execute(pb);

            if (commandDto.isImage()) {
                InputStream img = ImageService.createTerminalImage(output);
                if (img != null) {
                    event.getHook().sendFiles(FileUpload.fromData(img, "status.png")).queue();
                } else {
                    event.getHook().sendMessage("Failed to render image.").queue();
                }
            } else {
                if (output.length() > 1800) {
                    output = output.substring(0, 1800) + "\n...[truncated]";
                }
                event.getHook().sendMessage("```ansi\n" + output + "\n```").queue();
            }

        } catch (IllegalArgumentException e) {
            log.warn("Validation failed for command '{}': {}", commandName, e.getMessage());
            event.getHook().sendMessage("**Validation Error:** check alerts channel for errors").queue();
            eventPublisher.publishEvent(new AlertEvent("Validation Error", commandName, e.getMessage(), deviceName));

        } catch (ActionExecutionException e) {
            log.error("Execution failed for command '{}' on '{}'", commandName, deviceName, e);
            event.getHook().sendMessage("**Execution Failed:** check alerts channel for errors").queue();

            eventPublisher.publishEvent(new AlertEvent("Command Failed", commandName, e.getMessage(), deviceName));

        } catch (IllegalStateException e) {
            log.error("Configuration error on command '{}'", commandName, e);
            event.getHook().sendMessage("**Configuration error :** check alerts channel for errors").queue();
            eventPublisher.publishEvent(new AlertEvent("Configuration error", commandName, e.getMessage(), deviceName));

        } catch (Exception e) {
            log.error("CRITICAL UNEXPECTED ERROR on command '{}'", commandName, e);

            event.getHook().sendMessage("**A critical system error occurred:** check alerts channel for errors").queue();
            eventPublisher.publishEvent(new AlertEvent("FATAL BOT CRASH", commandName, e.getMessage(), deviceName));

        }
    }
}