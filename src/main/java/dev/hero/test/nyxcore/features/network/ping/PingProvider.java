package dev.hero.test.nyxcore.features.network.ping;

import dev.hero.test.nyxcore.dto.ProviderResult;

public interface PingProvider {
    /**
     * Executes a system ping and returns a ProviderResult describing the outcome.
     */
    ProviderResult ping(String ip);
}