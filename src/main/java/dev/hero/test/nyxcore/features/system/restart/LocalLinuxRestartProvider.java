package dev.hero.test.nyxcore.features.system.restart;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import dev.hero.test.nyxcore.exceptions.ActionExecutionException;
import dev.hero.test.nyxcore.dto.ProviderResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1)
public class LocalLinuxRestartProvider implements RestartProvider {

    @Override
    public boolean supports(String os) {
        return "linux".equalsIgnoreCase(os);
    }

    @Override
    public ProviderResult restart(String user, String ip, String port, String keypath) {
        List<String> command = List.of(
                "ssh",
                "-o", "StrictHostKeyChecking=no",
                "-o", "ConnectTimeout=5",
                "-p", port,
                "-i", keypath,
                user + "@" + ip,
                "sudo", "-n", "reboot"
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                String output = new String(process.getInputStream().readAllBytes());
                throw new ActionExecutionException("Restart process timed out over SSH.", ActionExecutionException.NO_EXIT_CODE, output);
            }

            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.exitValue();

            if (exitCode == 255) {
                String lower = output.toLowerCase();
                if (lower.contains("timeout") || lower.contains("refused") || lower.contains("unreachable") || lower.contains("no route")) {
                    throw new ActionExecutionException("Host is offline or unreachable.", exitCode, output);
                }
            } else if (exitCode != 0) {
                throw new ActionExecutionException("Restart command failed.", exitCode, output);
            }

            return ProviderResult.success("Restart command executed successfully.", exitCode, output);

        } catch (IOException e) {
            throw new ActionExecutionException("Fatal I/O error trying to start SSH process.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ActionExecutionException("SSH Restart process thread was interrupted.", e);
        }
    }
}