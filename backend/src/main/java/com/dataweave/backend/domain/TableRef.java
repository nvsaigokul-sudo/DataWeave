package com.dataweave.backend.domain;

public record TableRef(String schema, String name) {
    public String qualifiedName() {
        return schema + "." + name;
    }
}
