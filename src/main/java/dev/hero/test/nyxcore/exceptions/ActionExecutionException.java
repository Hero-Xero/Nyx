package dev.hero.test.nyxcore.exceptions;

import lombok.Getter;

@Getter
public class ActionExecutionException extends RuntimeException {

    public static final int NO_EXIT_CODE = -1;

    private final int exitCode;
    private final String stdOut;

    /**
     * Constructs an exception for logical command failures.
     * Use this when the underlying process executed, but returned a non-zero exit status.
     *
     * @param message  The high-level explanation of the failure.
     * @param exitCode The non-zero exit code returned by the OS or process.
     * @param stdOut   The merged standard output and error streams for diagnostic context.
     */
    public ActionExecutionException(String message, int exitCode, String stdOut) {
        super(message);
        this.exitCode = exitCode;
        this.stdOut = stdOut != null ? stdOut : "";
    }

    /**
     * Constructs an exception for systemic infrastructure failures.
     * Use this when the process failed to start, was interrupted, or threw a fatal JVM error.
     *
     * @param message The high-level explanation of the failure.
     * @param cause   The underlying infrastructure exception (e.g., IOException).
     */
    public ActionExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.exitCode = NO_EXIT_CODE;
        this.stdOut = "";
    }
}