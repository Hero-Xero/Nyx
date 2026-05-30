package dev.hero.test.nyxcore.services.engine.commands;

import dev.hero.test.nyxcore.dto.CommandDto;
import dev.hero.test.nyxcore.dto.HostDto;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandBuilderService {

    public ProcessBuilder build(CommandDto cmd, String triggeredSubcommand, HostDto host, List<OptionMapping> options) {

        StringBuilder rawCommand = new StringBuilder();
        rawCommand.append(cmd.getCommand());

        if (cmd.getFixedFlags() != null && !cmd.getFixedFlags().isBlank()) {
            rawCommand.append(" ").append(cmd.getFixedFlags());
        }

        List<CommandDto.FlagDefinition> activeFlags = cmd.getFlags();
        List<CommandDto.ArgumentDefinition> activeArgs = cmd.getArguments();

        if (triggeredSubcommand != null && cmd.getSubcommands() != null) {
            CommandDto.SubcommandDefinition subDef = cmd.getSubcommands().stream()
                    .filter(s -> s.getName().equalsIgnoreCase(triggeredSubcommand))
                    .findFirst().orElse(null);

            if (subDef != null) {
                String cliCommand = subDef.getSubcommand() != null ? subDef.getSubcommand() : subDef.getName();
                rawCommand.append(" ").append(cliCommand);

                activeFlags = subDef.getFlags();
                activeArgs = subDef.getArguments();
            }
        }

        if (activeFlags != null) {
            for (OptionMapping option : options) {
                CommandDto.FlagDefinition def = activeFlags.stream()
                        .filter(f -> f.getName().equals(option.getName()))
                        .findFirst().orElse(null);

                if (def != null) {
                    switch (option.getType()) {
                        case BOOLEAN -> { if (option.getAsBoolean()) rawCommand.append(" ").append(def.getFlag()); }
                        case USER -> rawCommand.append(" ").append(def.getFlag()).append(" ").append(option.getAsUser().getName());
                        case ROLE -> rawCommand.append(" ").append(def.getFlag()).append(" ").append(option.getAsRole().getName());
                        default -> rawCommand.append(" ").append(def.getFlag()).append(" ").append(option.getAsString());
                    }
                }
            }
        }

        if (activeArgs != null) {
            for (CommandDto.ArgumentDefinition argDef : activeArgs) {
                OptionMapping argOption = options.stream()
                        .filter(opt -> opt.getName().equals(argDef.getName()))
                        .findFirst().orElse(null);

                if (argOption != null) {
                    rawCommand.append(" ").append(argOption.getAsString());
                }
            }
        }

        StringBuilder finalPayload = new StringBuilder();
        finalPayload.append("export TERM=xterm-256color; export COLUMNS=1000; ");
        finalPayload.append(rawCommand.toString()); // THIS WAS MISSING

        List<String> sshCmd = List.of(
                "ssh", "-o", "StrictHostKeyChecking=no", "-o", "ConnectTimeout=5",
                "-p", String.valueOf(host.getPort()), "-i", host.getKeyPath(),
                host.getUser() + "@" + host.getIp(),
                finalPayload.toString()
        );

        ProcessBuilder pb = new ProcessBuilder(sshCmd);
        pb.redirectErrorStream(true);
        return pb;
    }
}