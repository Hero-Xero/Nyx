package dev.hero.test.nyxcore.features.network.ping;

import dev.hero.test.nyxcore.config.ProviderConstants;
import dev.hero.test.nyxcore.exceptions.ActionExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(
        name = ProviderConstants.Network.PING_MODE,
        havingValue = ProviderConstants.Values.LOCAL,
        matchIfMissing = true
)
public class LocalPingProvider implements PingProvider {

    @Override
    public String ping(String ip) {
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("IP address cannot be null or empty.");
        }

        ProcessBuilder processBuilder = new ProcessBuilder("ping", "-c", "1", "-W", "3", ip);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes()).trim();

            boolean finished = process.waitFor(4, TimeUnit.SECONDS);

            if (!finished) {
                process.destroy();
                throw new ActionExecutionException("Ping process timed out for: " + ip, ActionExecutionException.NO_EXIT_CODE, output);
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new ActionExecutionException("Ping failed with exit code " + exitCode, exitCode, output);
            }

            return output;

        } catch (IOException e) {
            throw new ActionExecutionException("Fatal I/O error trying to start ping process.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ActionExecutionException("Ping process thread was interrupted.", e);
        }
    }
}