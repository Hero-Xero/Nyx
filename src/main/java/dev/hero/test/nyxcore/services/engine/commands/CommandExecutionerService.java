package dev.hero.test.nyxcore.services.engine.commands;

import dev.hero.test.nyxcore.annotations.MonitoredAction;
import dev.hero.test.nyxcore.dto.ExecutionResult;
import dev.hero.test.nyxcore.exceptions.ActionExecutionException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class CommandExecutionerService {

    private static final Pattern INTERACTIVE_PATTERNS = Pattern.compile(
            "(?i)(\\[Y/n]|\\(y/n\\)|password:|are you sure|confirmation|continue\\?)");

    private static final int TIMEOUT_SECONDS = 15;

    @MonitoredAction
    public ExecutionResult execute(ProcessBuilder pb) {
        try {
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        synchronized (output) {
                            output.append(line).append("\n");
                        }
                    }
                } catch (IOException ignored) { }
            });
            outputReader.start();

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();

                String currentOutput;
                synchronized (output) {
                    currentOutput = output.toString().trim();
                }
                String lastLine = getLastLine(currentOutput);

                if (INTERACTIVE_PATTERNS.matcher(lastLine).find()) {
                    throw new ActionExecutionException(
                            "Interactive Prompt Detected! The command got stuck asking: `" + lastLine + "`",
                            ActionExecutionException.NO_EXIT_CODE, currentOutput);
                } else {
                    throw new ActionExecutionException(
                            "Command Timed Out (" + TIMEOUT_SECONDS + "s). Host unreachable or command too slow.",
                            ActionExecutionException.NO_EXIT_CODE, currentOutput);
                }
            }

            outputReader.join();
            String result = output.toString()
                    .replaceAll("Connection to .* closed\\.", "")
                    .replace("\r", "")
                    .trim();

            if (process.exitValue() != 0) {
                throw new ActionExecutionException("Command failed during execution.", process.exitValue(), result);
            }

            return ExecutionResult.pass(result);

        } catch (IOException e) {
            throw new ActionExecutionException("Fatal I/O error trying to execute command.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ActionExecutionException("Command execution thread was interrupted.", e);
        }
    }

    private String getLastLine(String text) {
        if (text == null || text.isEmpty()) return "";
        String[] lines = text.split("\n");
        return lines[lines.length - 1];
    }
}