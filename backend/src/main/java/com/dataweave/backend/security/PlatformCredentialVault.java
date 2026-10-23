package com.dataweave.backend.security;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Default vault: in-memory secrets for non-Windows builds and tests. When Gradle runs on Windows,
 * {@code WindowsCredentialVault} (under {@code src/windows/java}) is compiled and marked {@code @Primary}.
 */
@Component
@ConditionalOnMissingBean(name = "windowsCredentialVault")
public class PlatformCredentialVault implements CredentialVault {

    private static final Logger log = LoggerFactory.getLogger(PlatformCredentialVault.class);
    private final Map<String, char[]> secrets = new ConcurrentHashMap<>();
    private volatile boolean warned;

    @Override
    public void store(String key, char[] secret) {
        warnOnceIfNeeded();
        secrets.put(key, secret.clone());
    }

    @Override
    public Optional<char[]> load(String key) {
        char[] value = secrets.get(key);
        return value == null ? Optional.empty() : Optional.of(value.clone());
    }

    @Override
    public void delete(String key) {
        secrets.remove(key);
    }

    public static boolean isWindowsHost() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private void warnOnceIfNeeded() {
        if (!warned && isWindowsHost()) {
            warned = true;
            log.warn(
                    "Windows Credential Manager vault not on classpath; using in-memory fallback. Build on Windows with src/windows/java enabled.");
        } else if (!warned && !isWindowsHost()) {
            warned = true;
            log.warn("Using in-memory credential fallback (non-Windows host).");
        }
    }
}
