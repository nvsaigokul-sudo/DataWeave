package com.dataweave.backend.startup;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/** JVM/Spring startup timings exposed to the desktop shell (no Spring types on the public API). */
public final class StartupTiming {

    private static final AtomicReference<Instant> JVM_START = new AtomicReference<>(Instant.now());
    private static volatile long springReadyMs = -1L;

    private StartupTiming() {}

    public static void markJvmStart() {
        JVM_START.set(Instant.now());
    }

    public static void markSpringReady() {
        springReadyMs = Duration.between(JVM_START.get(), Instant.now()).toMillis();
        System.setProperty("dataweave.startup.spring-ready-ms", Long.toString(springReadyMs));
    }

    public static long springReadyMs() {
        if (springReadyMs >= 0) {
            return springReadyMs;
        }
        String prop = System.getProperty("dataweave.startup.spring-ready-ms");
        if (prop != null) {
            try {
                springReadyMs = Long.parseLong(prop);
            } catch (NumberFormatException ignored) {
                return -1L;
            }
        }
        return springReadyMs;
    }
}
