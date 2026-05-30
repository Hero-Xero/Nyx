package dev.hero.test.nyxcore.features.network.wol;

import dev.hero.test.nyxcore.dto.ProviderResult;

public interface WolProvider {
    /**
     * Sends a Wake-on-LAN magic packet to the specified MAC address and returns a ProviderResult.
     */
    ProviderResult wake(String macStr);
}