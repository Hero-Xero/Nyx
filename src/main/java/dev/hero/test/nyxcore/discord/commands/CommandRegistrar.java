package dev.hero.test.nyxcore.discord.commands;

import dev.hero.test.nyxcore.dto.CommandDto;
import dev.hero.test.nyxcore.services.registry.commands.CommandRegistryService;
import dev.hero.test.nyxcore.services.registry.hosts.HostRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandRegistrar {

    private final CommandRegistryService commandRegistryService;
    private final HostRegistryService hostRegistryService;

    public void registerCommands(Guild guild) {
        Collection<CommandDto> commands = commandRegistryService.getAllCommands();
        OptionData deviceOption = createDeviceOption();

        List<CommandData> discordCommands = new ArrayList<>();

        for (CommandDto cmd : commands) {
            discordCommands.add(buildSlashCommand(cmd, deviceOption));
        }

        guild.updateCommands().addCommands(discordCommands).queue();
        log.info("Registered {} commands successfully.", discordCommands.size());
    }

    private SlashCommandData buildSlashCommand(CommandDto cmd, OptionData deviceOption) {
        SlashCommandData slashCmd = Commands.slash(cmd.getName().toLowerCase().replaceAll("[^A-Za-z0-9_-]", ""), cmd.getDescription());

        // Does this command have subcommands? A question we all ponder ...
        if (cmd.getSubcommands() != null && !cmd.getSubcommands().isEmpty()) {
            List<SubcommandData> subCmdDataList = new ArrayList<>();

            for (CommandDto.SubcommandDefinition subDef : cmd.getSubcommands()) {
                SubcommandData subData = new SubcommandData(subDef.getName().toLowerCase(), subDef.getDescription());
                subData.addOptions(deviceOption);
                subData.addOptions(buildOptions(subDef.getFlags(), subDef.getArguments()));
                subCmdDataList.add(subData);
            }
            slashCmd.addSubcommands(subCmdDataList);

        } else {
            // It's a simple command (no subcommands)
            slashCmd.addOptions(deviceOption);
            slashCmd.addOptions(buildOptions(cmd.getFlags(), cmd.getArguments()));
        }

        return slashCmd;
    }

    // HELPER: Builds both flags and arguments into Discord options
    private List<OptionData> buildOptions(List<CommandDto.FlagDefinition> flags, List<CommandDto.ArgumentDefinition> args) {
        List<OptionData> options = new ArrayList<>();

        if (flags != null) {
            for (CommandDto.FlagDefinition flag : flags) {
                OptionType type = resolveType(flag.getType());
                OptionData option = new OptionData(type, flag.getName(), flag.getDescription(), flag.isRequired());
                addChoicesIfPresent(option, type, flag.getChoices(), flag.getName());
                options.add(option);
            }
        }

        if (args != null) {
            for (CommandDto.ArgumentDefinition arg : args) {
                OptionType type = resolveType(arg.getType());
                OptionData option = new OptionData(type, arg.getName(), arg.getDescription(), arg.isRequired());
                addChoicesIfPresent(option, type, arg.getChoices(), arg.getName());
                options.add(option);
            }
        }

        // Discord requires required options to be first
        options.sort((o1, o2) -> Boolean.compare(o2.isRequired(), o1.isRequired()));
        return options;
    }

    private void addChoicesIfPresent(OptionData option, OptionType type, Map<String, String> choices, String name) {
        if (choices == null || choices.isEmpty()) return;

        if (!supportsChoices(type)) {
            log.warn("Config Warning: '{}' is type {}, ignoring choices.", name, type);
            return;
        }

        choices.forEach((label, value) -> {
            try {
                switch (type) {
                    case INTEGER -> option.addChoice(label, Long.parseLong(value));
                    case NUMBER -> option.addChoice(label, Double.parseDouble(value));
                    default -> option.addChoice(label, value);
                }
            } catch (NumberFormatException e) {
                log.error("Choice mapping failed for '{}'", name, e);
            }
        });
    }

    private OptionData createDeviceOption() {
        OptionData deviceOption = new OptionData(OptionType.STRING, "device", "Where to execute the command", true);
        hostRegistryService.getAllHosts().forEach(h -> deviceOption.addChoice(h.getDisplayName(), h.getName()));
        return deviceOption;
    }

    private OptionType resolveType(CommandDto.Type type) {
        try { return type != null ? OptionType.valueOf(type.name()) : OptionType.STRING; }
        catch (Exception e) { return OptionType.STRING; }
    }

    private boolean supportsChoices(OptionType type) {
        return type == OptionType.STRING || type == OptionType.INTEGER || type == OptionType.NUMBER;
    }
}