package dev.hero.test.nyxcore.features.network.wol;

import java.io.IOException;

public interface WolProvider {
    /**
     * Sends a Wake-on-LAN magic packet to the specified MAC address.
     */
    void wake(String macStr);
}