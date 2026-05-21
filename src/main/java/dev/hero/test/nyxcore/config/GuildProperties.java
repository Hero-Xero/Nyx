package dev.hero.test.nyxcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "guild")
public record GuildProperties(String id) {}
