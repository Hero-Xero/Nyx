package dev.hero.test.nyxcore.config;

public final class ProviderConstants {

    // Prevent anyone from accidentally doing 'new ProviderConstants()'
    private ProviderConstants() {}

    public static final class Network {
        public static final String PING_MODE = "features.network.ping";
        public static final String WOL_MODE  = "features.network.wol";
    }

    public static final class Values {
        public static final String LOCAL = "local";
    }
}