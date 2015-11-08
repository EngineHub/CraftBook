package com.sk89q.craftbook.core.util;

import com.sk89q.craftbook.sponge.SpongeConfiguration;
import ninja.leaping.configurate.ConfigurationNode;

public class ConfigValue<T> {

    private String key;
    private String comment;
    private T defaultValue;
    private T value;

    public ConfigValue(String key, String comment, T value) {
        this.key = key;
        this.comment = comment;
        this.defaultValue = value;
    }

    public ConfigValue<T> load(ConfigurationNode configurationNode) {
        this.value = SpongeConfiguration.getValue(configurationNode.getNode(key), defaultValue, comment);
        return this;
    }

    public ConfigValue<T> save(ConfigurationNode configurationNode) {
        SpongeConfiguration.setValue(configurationNode.getNode(key), value, comment);
        return this;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public T getValue() {
        return this.value == null ? this.defaultValue : this.value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
