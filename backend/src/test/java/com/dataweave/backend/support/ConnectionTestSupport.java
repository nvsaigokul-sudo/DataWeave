package com.dataweave.backend.support;

import com.dataweave.backend.domain.ConnectionProfile;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ConnectionSessionService;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;

public final class ConnectionTestSupport {

    private ConnectionTestSupport() {}

    public static UUID createAndConnect(
            ConnectionProfileService profiles,
            ConnectionSessionService sessions,
            String host,
            int port)
            throws Exception {
        ConnectionProfile profile =
                profiles.create(
                        "test-" + UUID.randomUUID(),
                        host,
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        Assertions.assertFalse(profiles.hasLivePool(profile.id()));
        var status = sessions.connect(profile.id());
        Assertions.assertTrue(status.state().name().equals("CONNECTED"), status.message());
        return profile.id();
    }
}
