CREATE TABLE workspace_projects (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX idx_workspace_projects_updated ON workspace_projects(updated_at DESC);

CREATE TABLE connection_profiles (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    host TEXT NOT NULL,
    port INTEGER NOT NULL,
    database_name TEXT NOT NULL,
    username TEXT NOT NULL,
    ssl_mode TEXT NOT NULL DEFAULT 'prefer',
    credential_key TEXT NOT NULL,
    connect_timeout_seconds INTEGER NOT NULL DEFAULT 10,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX idx_connection_profiles_name ON connection_profiles(name);

CREATE TABLE schema_versions (
    connection_id TEXT PRIMARY KEY,
    version INTEGER NOT NULL,
    refreshed_at TEXT NOT NULL
);
