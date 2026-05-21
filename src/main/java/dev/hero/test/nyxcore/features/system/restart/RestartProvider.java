package dev.hero.test.nyxcore.features.system.restart;

public interface RestartProvider {
    boolean supports(String os);
    void restart (String user, String ip, String port, String keypath);
}