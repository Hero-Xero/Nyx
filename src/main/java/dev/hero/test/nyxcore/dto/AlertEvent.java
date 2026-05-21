package dev.hero.test.nyxcore.dto;

public record AlertEvent(
        String title,
        String source,
        String message,
        String host
) {}
