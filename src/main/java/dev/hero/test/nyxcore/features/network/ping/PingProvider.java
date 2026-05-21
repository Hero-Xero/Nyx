package dev.hero.test.nyxcore.features.network.ping;

import java.io.IOException;

public interface PingProvider {
    /**
     * Executes a system ping and returns the raw output.
     */
    String ping(String ip);
}