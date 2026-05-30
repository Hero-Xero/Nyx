package dev.hero.test.nyxcore.services;

import java.awt.Color;
import java.time.Instant;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import dev.hero.test.nyxcore.config.DiscordProperties;
import dev.hero.test.nyxcore.discord.bot.DiscordBotService;
import dev.hero.test.nyxcore.dto.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertsService {

    private final DiscordProperties properties;
    private final DiscordBotService botService;

    @Async
    @EventListener
    public void handleAlert(AlertEvent event) {
        String alertChannelId = properties.channels().alerts();
        JDA jda = botService.getJda();

        if (alertChannelId == null || alertChannelId.isBlank()) {
            log.error("Could not find an alert channel ID in properties.");
            return;
        }

        if (jda == null) {
            log.error("JDA is not initialized yet. Cannot send alert: {}", event.title());
            return;
        }

        TextChannel channel = jda.getTextChannelById(alertChannelId);
        if (channel == null) {
            log.error("Discord channel ID {} not found.", alertChannelId);
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(event.title())
                .setColor(Color.RED)
                .addField("Source", event.source(), true);

        if (event.exitCode() != -1) {
            embedBuilder.addField("Exit Code", String.valueOf(event.exitCode()), true);
        }

        embedBuilder.addField("Target", event.host(), true)
                .setTimestamp(Instant.now());

        embedBuilder.addField("Message", "```\n" + event.message() + "\n```", false);

        if (event.stdout() != null && !event.stdout().isBlank()) {
            embedBuilder.addField("Stdout", "```\n" + event.stdout() + "\n```", false);
        }


        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}