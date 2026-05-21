package dev.hero.test.nyxcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.nio.file.Path;

@ConfigurationProperties(prefix = "config")
public record ConfigProperties(
    PathConfig commands,
    PathConfig hosts,
    PathConfig dashboard
) {
    public record PathConfig(Path path) {}
}
