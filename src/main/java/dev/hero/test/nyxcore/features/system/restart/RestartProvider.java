package dev.hero.test.nyxcore.features.system.restart;

import dev.hero.test.nyxcore.dto.ProviderResult;

public interface RestartProvider {
    boolean supports(String os);
    ProviderResult restart (String user, String ip, String port, String keypath);
}