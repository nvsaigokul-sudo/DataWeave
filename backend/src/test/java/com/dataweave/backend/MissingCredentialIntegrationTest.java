package com.dataweave.backend;

import com.dataweave.backend.domain.ConnectionLifecycleState;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.security.CredentialVault;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ConnectionSessionService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MissingCredentialIntegrationTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService profiles;
    @Autowired ConnectionSessionService sessions;
    @Autowired CredentialVault vault;

    @Test
    void connectWithoutVaultPasswordReportsMissingCredential() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                profiles.create(
                        "vault-gap",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        vault.delete(profile.credentialKey());
        Assertions.assertFalse(profiles.hasStoredCredential(profile.id()));

        var status = sessions.connect(profile.id());
        Assertions.assertEquals(ConnectionLifecycleState.ERROR, status.state());
        Assertions.assertTrue(status.errorKind().orElse("").contains("missing_credential"));
        Assertions.assertFalse(profiles.hasLivePool(profile.id()));
    }

    @Test
    void reconnectAfterVaultStoreSucceeds() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                profiles.create(
                        "vault-restore",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        vault.delete(profile.credentialKey());
        var fail = sessions.connect(profile.id());
        Assertions.assertEquals(ConnectionLifecycleState.ERROR, fail.state());

        vault.store(profile.credentialKey(), "postgres".toCharArray());
        var ok = sessions.connect(profile.id());
        Assertions.assertEquals(ConnectionLifecycleState.CONNECTED, ok.state());
        sessions.disconnect(profile.id());
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
