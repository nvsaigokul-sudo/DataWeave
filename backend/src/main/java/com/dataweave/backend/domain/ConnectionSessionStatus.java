package com.dataweave.backend.domain;

import java.util.Optional;
import java.util.UUID;

public record ConnectionSessionStatus(
        UUID connectionId,
        ConnectionLifecycleState state,
        String message,
        Optional<String> errorKind) {

    public static ConnectionSessionStatus disconnected(UUID id) {
        return new ConnectionSessionStatus(id, ConnectionLifecycleState.DISCONNECTED, "Not connected", Optional.empty());
    }

    public static ConnectionSessionStatus connecting(UUID id) {
        return new ConnectionSessionStatus(id, ConnectionLifecycleState.CONNECTING, "Connecting…", Optional.empty());
    }

    public static ConnectionSessionStatus reconnecting(UUID id) {
        return new ConnectionSessionStatus(id, ConnectionLifecycleState.RECONNECTING, "Reconnecting…", Optional.empty());
    }

    public static ConnectionSessionStatus connected(UUID id, String detail) {
        return new ConnectionSessionStatus(
                id, ConnectionLifecycleState.CONNECTED, detail == null ? "Connected" : detail, Optional.empty());
    }

    public static ConnectionSessionStatus error(UUID id, String errorKind, String message) {
        return new ConnectionSessionStatus(
                id,
                ConnectionLifecycleState.ERROR,
                message == null ? "Connection failed" : message,
                Optional.ofNullable(errorKind));
    }
}
