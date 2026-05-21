package dev.hero.test.nyxcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord")
public record DiscordProperties(
    String token,
    Channels channels
) {
    public record Channels(
        String general,
        String alerts,
        String dashboard
    ) {}
}
