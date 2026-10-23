package com.dataweave.backend.platform;

import java.nio.file.Path;

public final class PlatformInfo {

    private PlatformInfo() {}

    public static OperatingSystem os() {
        String name = System.getProperty("os.name", "").toLowerCase();
        if (name.contains("win")) {
            return OperatingSystem.WINDOWS;
        }
        if (name.contains("mac") || name.contains("darwin")) {
            return OperatingSystem.MACOS;
        }
        if (name.contains("nux") || name.contains("linux")) {
            return OperatingSystem.LINUX;
        }
        return OperatingSystem.OTHER;
    }

    public static Path dataWeaveHome() {
        return Path.of(System.getProperty("user.home"), ".dataweave");
    }

    public static String cpuArchitecture() {
        return System.getProperty("os.arch", "unknown");
    }

    public static boolean isAppleSilicon() {
        String arch = cpuArchitecture().toLowerCase();
        return os() == OperatingSystem.MACOS && (arch.contains("aarch64") || arch.contains("arm64"));
    }

    public enum OperatingSystem {
        WINDOWS,
        MACOS,
        LINUX,
        OTHER
    }
}
