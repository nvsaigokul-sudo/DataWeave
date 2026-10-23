package com.dataweave.backend.security;

import java.util.Optional;

public interface CredentialVault {
    void store(String key, char[] secret);

    Optional<char[]> load(String key);

    void delete(String key);

    default void store(String key, String secret) {
        store(key, secret == null ? new char[0] : secret.toCharArray());
    }
}
