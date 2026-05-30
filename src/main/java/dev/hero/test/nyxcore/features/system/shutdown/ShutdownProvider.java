package dev.hero.test.nyxcore.features.system.shutdown;

import dev.hero.test.nyxcore.dto.ProviderResult;

public interface ShutdownProvider {
    boolean supports(String os);
    ProviderResult shutdown(String user, String ip, String port, String keypath);
}