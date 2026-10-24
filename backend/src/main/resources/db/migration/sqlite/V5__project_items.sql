CREATE TABLE project_items (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    item_type TEXT NOT NULL,
    ref_id TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    label TEXT NOT NULL DEFAULT '',
    created_at TEXT NOT NULL,
    FOREIGN KEY (project_id) REFERENCES workspace_projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_project_items_project ON project_items(project_id, sort_order);
