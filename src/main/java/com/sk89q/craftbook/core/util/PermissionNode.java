package com.sk89q.craftbook.core.util;

public abstract class PermissionNode {

    private final String node;
    private final String description;
    private final String defaultRole;

    public PermissionNode(String node, String description, String defaultRole) {
        this.node = node;
        this.description = description;
        this.defaultRole = defaultRole;
    }

    public String getNode() {
        return this.node;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDefaultRole() {
        return this.defaultRole;
    }

    public abstract void register();
}
