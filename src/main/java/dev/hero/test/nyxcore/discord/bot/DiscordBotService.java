package dev.hero.test.nyxcore.discord.bot;

import dev.hero.test.nyxcore.config.DiscordProperties;
import dev.hero.test.nyxcore.config.GuildProperties;
import dev.hero.test.nyxcore.discord.commands.Listener;
import dev.hero.test.nyxcore.discord.dashboard.AutoDashboard;
import dev.hero.test.nyxcore.discord.commands.CommandRegistrar;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

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

    // Replaced "Completable Future" and @PostConstruct with this because discord started before spring did
    // ApplicationReadyEvent is the last thing spring fires, so everything else should be working as well.
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        try {
            log.info("Spring Context fully refreshed. Starting Discord Bot...");

            this.jda = JDABuilder.createDefault(discordProps.token())
                    .addEventListeners(listener, autoDashboard)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .setActivity(Activity.playing("Terminal"))
                    .build()
                    .awaitReady(); // It is now safe to block here

            autoDashboard.refresh(jda);

            Guild guild = jda.getGuildById(guildProps.id());
            if (guild == null) {
                log.error("Critical Error: Could not find Guild ID: {}", guildProps.id());
                return;
            }

            commandRegistrar.registerCommands(guild);

            log.info("Discord Bot is online and fully synced with Spring.");

        } catch (Exception e) {
            log.error("CRITICAL STARTUP FAILURE: Failed to connect to Discord.", e);
        }
    }
}