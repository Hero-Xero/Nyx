package dev.hero.test.nyxcore.dto;

/**
 * Result object returned by providers for normal outcomes.
 */
public record ProviderResult(boolean success, String message, int exitCode, String stdOut) {
    public static ProviderResult success(String message, int exitCode, String stdOut) {
        return new ProviderResult(true, message, exitCode, stdOut == null ? "" : stdOut);
    }

    public static ProviderResult success(String message) {
        return success(message, -1, "");
    }

    public static ProviderResult fail(String message, int exitCode, String stdOut) {
        return new ProviderResult(false, message, exitCode, stdOut == null ? "" : stdOut);
    }
}
