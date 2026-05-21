package dev.hero.test.nyxcore.discord.bot;

import dev.hero.test.nyxcore.config.DiscordProperties;
import dev.hero.test.nyxcore.config.GuildProperties;
import dev.hero.test.nyxcore.discord.commands.Listener;
import dev.hero.test.nyxcore.discord.dashboard.AutoDashboard;
import dev.hero.test.nyxcore.discord.commands.CommandRegistrar;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordBotService {

    private final AutoDashboard autoDashboard;
    private final Listener listener;
    private final CommandRegistrar commandRegistrar;
    private final DiscordProperties discordProps;
    private final GuildProperties guildProps;

    // Expose JDA so other Spring components can grab it after startup
    @Getter
    private JDA jda;

    @PostConstruct
    public void start() {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting Discord Bot...");

                // Assign to the class variable
                this.jda = JDABuilder.createDefault(discordProps.token())
                        .addEventListeners(listener, autoDashboard)
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                        .setActivity(Activity.playing("Terminal"))
                        .build()
                        .awaitReady();

                autoDashboard.refresh(jda);

                Guild guild = jda.getGuildById(guildProps.id());
                if (guild == null) {
                    log.error("Critical Error: Could not find Guild ID: {}", guildProps.id());
                    return;
                }

                commandRegistrar.registerCommands(guild);

            } catch (Exception e) {
                log.error("CRITICAL STARTUP FAILURE: Failed to connect to Discord.", e);
            }
        });
    }
}