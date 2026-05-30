package dev.hero.test.nyxcore.services;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local storage for metadata about the current bot action.
 * This allows the AlertAspect to know the context (Source/Host) of an error 
 * without having to pass it into every single method.
 */
@Slf4j
public class ActionContext {

    private static final ThreadLocal<Metadata> context = new ThreadLocal<>();

    public record Metadata(String source, String hostName) {}

    public static void set(String source, String hostName) {
        context.set(new Metadata(source, hostName));
    }

    public static Metadata get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }

    /**
     * This is for backwards compatibility as some callers use `remove()`
     */
    @Deprecated
    public static void remove() {
        clear();
    }
}
