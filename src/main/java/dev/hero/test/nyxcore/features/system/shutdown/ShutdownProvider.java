package dev.hero.test.nyxcore.features.system.shutdown;

import java.io.IOException;

public interface ShutdownProvider {
    boolean supports(String os);
    void shutdown(String user, String ip, String port, String keypath);
}