package dev.hero.test.nyxcore.dto;

/**
 * Technical event published when an action fails.
 * 
 * @param title    The high-level reason for failure (e.g., "SSH Connection Failed")
 * @param source   The specific command or action name (e.g., "dysk", "ping")
 * @param exitCode The OS exit code, or -1 if not applicable
 * @param message  The technical trace or terminal output
 * @param stdout   Captured standard output / error from the failed action, if available
 * @param host     The target device name
 */
public record AlertEvent(
        String title,
        String source,
        int exitCode,
        String message,
        String host,
        String stdout
) {
        public AlertEvent(String title, String source, int exitCode, String message, String host) {
                this(title, source, exitCode, message, host, "");
        }
}
