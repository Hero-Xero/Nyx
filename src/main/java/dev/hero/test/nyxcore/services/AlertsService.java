package dev.hero.test.nyxcore.services;

import dev.hero.test.nyxcore.config.DiscordProperties;
import dev.hero.test.nyxcore.discord.bot.DiscordBotService;
import dev.hero.test.nyxcore.dto.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertsService {

    // Make these final so Spring properly injects them
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

        // making sure the async Discord boot actually finished before trying to send an alert
        if (jda == null) {
            log.error("JDA is not initialized yet. Cannot send alert: {}", event.title());
            return;
        }

        TextChannel channel = jda.getTextChannelById(alertChannelId);

        if (channel == null) {
            log.error("Cannot send alert. Discord channel ID {} not found. Check bot permissions.", alertChannelId);
            return;
        }

        String eventMessage = "```" + event.message() + "```";

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(event.title())
                .setColor(Color.RED)
                .addField("Source", event.source(), true)
                .addField("Error Message", eventMessage, false)
                .setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}