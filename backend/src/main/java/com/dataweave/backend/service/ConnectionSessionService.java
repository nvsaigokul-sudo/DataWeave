package com.dataweave.backend.service;

import com.dataweave.backend.domain.ConnectionProfile;
import com.dataweave.backend.domain.ConnectionSessionStatus;
import com.dataweave.backend.domain.ConnectionTestResult;
import com.dataweave.backend.postgres.PostgresConnectionManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ConnectionSessionService {

    private final ConnectionProfileService connectionProfileService;
    private final PostgresConnectionManager connectionManager;
    private final Map<UUID, ConnectionSessionStatus> statusById = new ConcurrentHashMap<>();

    public ConnectionSessionService(
            ConnectionProfileService connectionProfileService, PostgresConnectionManager connectionManager) {
        this.connectionProfileService = connectionProfileService;
        this.connectionManager = connectionManager;
    }

    public ConnectionSessionStatus status(UUID connectionId) {
        return statusById.getOrDefault(connectionId, ConnectionSessionStatus.disconnected(connectionId));
    }

    public List<ConnectionSessionStatus> allStatuses() {
        return connectionProfileService.list().stream().map(p -> status(p.id())).toList();
    }

    public ConnectionSessionStatus connect(UUID connectionId) {
        return connect(connectionId, null);
    }

    public ConnectionSessionStatus connect(UUID connectionId, char[] passwordOverride) {
        ConnectionProfile profile = connectionProfileService.require(connectionId);
        statusById.put(connectionId, ConnectionSessionStatus.connecting(connectionId));
        ConnectionTestResult result = connectionManager.connect(profile, passwordOverride);
        return applyTestResult(connectionId, result);
    }

    public ConnectionSessionStatus disconnect(UUID connectionId) {
        connectionManager.disconnect(connectionId);
        ConnectionSessionStatus status = ConnectionSessionStatus.disconnected(connectionId);
        statusById.put(connectionId, status);
        return status;
    }

    public ConnectionSessionStatus reconnect(UUID connectionId) {
        statusById.put(connectionId, ConnectionSessionStatus.reconnecting(connectionId));
        connectionManager.disconnect(connectionId);
        return connect(connectionId);
    }

    public void markDisconnected(UUID connectionId) {
        statusById.put(connectionId, ConnectionSessionStatus.disconnected(connectionId));
    }

    public boolean isLiveConnected(UUID connectionId) {
        return connectionManager.isConnected(connectionId)
                && status(connectionId).state() == com.dataweave.backend.domain.ConnectionLifecycleState.CONNECTED;
    }

    private ConnectionSessionStatus applyTestResult(UUID connectionId, ConnectionTestResult result) {
        if (result.success()) {
            ConnectionSessionStatus status = ConnectionSessionStatus.connected(connectionId, result.message());
            statusById.put(connectionId, status);
            return status;
        }
        connectionManager.disconnect(connectionId);
        ConnectionSessionStatus status =
                ConnectionSessionStatus.error(connectionId, result.errorKind(), result.message());
        statusById.put(connectionId, status);
        return status;
    }
}
