package com.dataweave.backend.domain;

/** Live JDBC pool state exposed to the desktop UI. */
public enum ConnectionLifecycleState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}
