package com.dataweave.backend.security;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class LinuxSecretCredentialVault implements CredentialVault {

    private static final Logger log = LoggerFactory.getLogger(LinuxSecretCredentialVault.class);
    private static final String SERVICE = "DataWeave";

    private final Map<String, char[]> memoryFallback = new ConcurrentHashMap<>();
    private final boolean secretToolAvailable = detectSecretTool();

    @Override
    public void store(String key, char[] secret) {
        if (secretToolAvailable) {
            try {
                runInput(
                        new String[] {
                            "secret-tool", "store", "--label=DataWeave connection", "service", SERVICE, "key", key
                        },
                        new String(secret));
                return;
            } catch (Exception ex) {
                log.warn("secret-tool store failed, using session memory fallback: {}", ex.getMessage());
            }
        }
        memoryFallback.put(key, secret.clone());
    }

    @Override
    public Optional<char[]> load(String key) {
        if (secretToolAvailable) {
            try {
                String value =
                        runOutput(new String[] {"secret-tool", "lookup", "service", SERVICE, "key", key});
                if (value != null && !value.isBlank()) {
                    return Optional.of(value.toCharArray());
                }
            } catch (Exception ex) {
                log.debug("secret-tool lookup failed for {}: {}", key, ex.getMessage());
            }
        }
        char[] cached = memoryFallback.get(key);
        return cached == null ? Optional.empty() : Optional.of(cached.clone());
    }

    @Override
    public void delete(String key) {
        memoryFallback.remove(key);
        if (secretToolAvailable) {
            try {
                runOutput(new String[] {"secret-tool", "clear", "service", SERVICE, "key", key});
            } catch (Exception ex) {
                log.debug("secret-tool clear failed for {}: {}", key, ex.getMessage());
            }
        }
    }

    private static boolean detectSecretTool() {
        try {
            Process p = new ProcessBuilder("secret-tool", "--version").start();
            return p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private static void runInput(String[] command, String stdin) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        process.getOutputStream().write(stdin.getBytes(StandardCharsets.UTF_8));
        process.getOutputStream().close();
        if (!process.waitFor(15, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("secret-tool timed out");
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException("secret-tool exit " + process.exitValue());
        }
    }

    private static String runOutput(String[] command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        if (!process.waitFor(15, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("secret-tool timed out");
        }
        String output;
        try (InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException("secret-tool exit " + process.exitValue() + ": " + output);
        }
        return output;
    }
}
