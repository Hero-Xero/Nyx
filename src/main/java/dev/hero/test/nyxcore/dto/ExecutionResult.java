package dev.hero.test.nyxcore.dto;

public record ExecutionResult(
        boolean success,
        String message
) {
    public static ExecutionResult pass(String message) {
        return new ExecutionResult(true, message);
    }

    public static ExecutionResult fail(String message) {
        return new ExecutionResult(false, message);
    }
}