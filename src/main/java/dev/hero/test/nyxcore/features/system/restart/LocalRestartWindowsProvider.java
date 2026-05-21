package dev.hero.test.nyxcore.features.system.restart;

import dev.hero.test.nyxcore.exceptions.ActionExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(2)
public class LocalRestartWindowsProvider implements RestartProvider {

    @Override
    public boolean supports(String os) {
        return "windows".equalsIgnoreCase(os);
    }

    @Override
    public void restart(String user, String ip, String port, String keypath) {
        List<String> command = List.of(
                "ssh",
                "-o", "StrictHostKeyChecking=no",
                "-o", "ConnectTimeout=5",
                "-p", port,
                "-i", keypath,
                user + "@" + ip,
                "shutdown.exe /r /t 0 || /mnt/c/Windows/System32/shutdown.exe /r /t 0"
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            // Wait first so the timeout actually works, as seen in LocalLinuxRestartProvider
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new ActionExecutionException("Restart process timed out over SSH.", ActionExecutionException.NO_EXIT_CODE, "");
            }

            String output = new String(process.getInputStream().readAllBytes());

            int exitCode = process.exitValue();
            if (exitCode != 0 && exitCode != 255) {
                throw new ActionExecutionException("Restart command failed.", exitCode, output);
            }

        } catch (IOException e) {
            throw new ActionExecutionException("Fatal I/O error trying to start SSH process.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ActionExecutionException("SSH Restart process thread was interrupted.", e);
        }
    }
}
