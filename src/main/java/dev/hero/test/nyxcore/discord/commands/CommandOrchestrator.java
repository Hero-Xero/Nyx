package dev.hero.test.nyxcore.discord.commands;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import dev.hero.test.nyxcore.dto.ExecutionResult;
import org.springframework.stereotype.Service;

import dev.hero.test.nyxcore.discord.helpers.ImageService;
import dev.hero.test.nyxcore.dto.CommandDto;
import dev.hero.test.nyxcore.dto.HostDto;
import dev.hero.test.nyxcore.services.ActionContext;
import dev.hero.test.nyxcore.services.engine.commands.CommandBuilderService;
import dev.hero.test.nyxcore.services.engine.commands.CommandExecutionerService;
import dev.hero.test.nyxcore.services.registry.commands.CommandRegistryService;
import dev.hero.test.nyxcore.services.registry.hosts.HostRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandOrchestrator {

    private final HostRegistryService hostRegistry;
    private final CommandRegistryService commandRegistry;
    private final CommandBuilderService commandBuilder;
    private final CommandExecutionerService commandExecutioner;

    public void handle(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String deviceName = Optional.ofNullable(event.getOption("device"))
                .map(OptionMapping::getAsString)
                .orElse("Unknown Device");

        try {
            ActionContext.set(commandName, deviceName);
            event.deferReply().queue();

            HostDto host = hostRegistry.getHost(deviceName);
            if (host == null) {
                event.getHook().sendMessage("❌ Host not found in registry: " + deviceName).queue();
                return;
            }

            CommandDto commandDto = commandRegistry.getCommand(commandName);
            if (commandDto == null) {
                event.getHook().sendMessage("❌ Command not found in registry: " + commandName).queue();
                return;
            }

            List<OptionMapping> options = event.getOptions();
            String subcommandName = event.getSubcommandName();

            ProcessBuilder pb = commandBuilder.build(commandDto, subcommandName, host, options);
            ExecutionResult result = commandExecutioner.execute(pb);

            // If the Aspect caught an error, it returns success = false
            if (!result.success()) {
                event.getHook().sendMessage("❌ **Command Failed:** " + result.message()).queue();
                return;
            }

            // If success, extract the string to manipulate it
            String outputText = result.message();

            if (commandDto.isImage()) {
                InputStream img = ImageService.createTerminalImage(outputText);
                if (img != null) {
                    event.getHook().sendFiles(FileUpload.fromData(img, "status.png")).queue();
                } else {
                    event.getHook().sendMessage("Failed to render image.").queue();
                }
            } else {
                if (outputText.length() > 1800) {
                    outputText = outputText.substring(0, 1800) + "\n...[truncated]";
                }
                event.getHook().sendMessage("```ansi\n" + outputText + "\n```").queue();
            }

        } finally {
            ActionContext.remove();
        }
    }
}