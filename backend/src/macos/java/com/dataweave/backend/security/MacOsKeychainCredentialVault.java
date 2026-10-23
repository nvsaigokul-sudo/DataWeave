package com.dataweave.backend.security;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MacOsKeychainCredentialVault implements CredentialVault {

    private static final Logger log = LoggerFactory.getLogger(MacOsKeychainCredentialVault.class);
    private static final String SERVICE_PREFIX = "DataWeave/";

    @Override
    public void store(String key, char[] secret) {
        String service = SERVICE_PREFIX + key;
        try {
            run("security", "add-generic-password", "-U", "-s", service, "-a", "dataweave", "-w", new String(secret));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to store credential in Keychain", ex);
        }
    }

    @Override
    public Optional<char[]> load(String key) {
        String service = SERVICE_PREFIX + key;
        try {
            String password = run("security", "find-generic-password", "-s", service, "-a", "dataweave", "-w");
            return password == null || password.isBlank() ? Optional.empty() : Optional.of(password.toCharArray());
        } catch (Exception ex) {
            log.debug("Keychain read failed for {}: {}", key, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        String service = SERVICE_PREFIX + key;
        try {
            run("security", "delete-generic-password", "-s", service, "-a", "dataweave");
        } catch (Exception ex) {
            log.warn("Keychain delete failed for {}: {}", key, ex.getMessage());
        }
    }

    private static String run(String... command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        boolean finished = process.waitFor(15, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("Command timed out: " + String.join(" ", command));
        }
        String output;
        try (InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException("Exit " + process.exitValue() + ": " + output);
        }
        return output;
    }
}
