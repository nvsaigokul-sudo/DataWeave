package com.dataweave.backend;

import com.dataweave.backend.domain.ConnectionLifecycleState;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ConnectionSessionService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ConnectionLifecycleIntegrationTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService profiles;
    @Autowired ConnectionSessionService sessions;

    @Test
    void saveDoesNotOpenLivePool() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                profiles.create(
                        "saved-only",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        Assertions.assertFalse(profiles.hasLivePool(profile.id()));
        Assertions.assertEquals(ConnectionLifecycleState.DISCONNECTED, sessions.status(profile.id()).state());
    }

    @Test
    void connectThenDisconnect() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        UUID id =
                com.dataweave.backend.support.ConnectionTestSupport.createAndConnect(profiles, sessions, "127.0.0.1", port);
        Assertions.assertTrue(profiles.hasLivePool(id));
        sessions.disconnect(id);
        Assertions.assertFalse(profiles.hasLivePool(id));
    }

    @Test
    void invalidCredentialsDoNotStayConnected() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                profiles.create(
                        "bad-user",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "not_a_valid_pg_role",
                        "any".toCharArray(),
                        SslMode.DISABLE,
                        5);
        var status = sessions.connect(profile.id());
        Assertions.assertEquals(ConnectionLifecycleState.ERROR, status.state());
        Assertions.assertFalse(profiles.hasLivePool(profile.id()));
    }

    @Test
    void unreachableHostFailsFast() {
        var profile =
                profiles.create(
                        "nowhere",
                        "127.0.0.1",
                        1,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        1);
        var status = sessions.connect(profile.id());
        Assertions.assertEquals(ConnectionLifecycleState.ERROR, status.state());
        Assertions.assertNotNull(status.errorKind().orElse(null));
    }

    @Test
    void testDoesNotMarkConnected() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                profiles.create(
                        "probe",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        Assertions.assertTrue(profiles.test(profile.id(), null).success());
        Assertions.assertFalse(profiles.hasLivePool(profile.id()));
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
