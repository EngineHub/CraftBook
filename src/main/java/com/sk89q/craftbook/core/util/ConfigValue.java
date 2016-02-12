package com.sk89q.craftbook.core.util;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ConfigValue<T> {

    private String key;
    private String comment;
    private T defaultValue;
    private T value;

    private TypeToken<T> typeToken;

    public ConfigValue(String key, String comment, T value) {
        this(key, comment, value, null);
    }

    public ConfigValue(String key, String comment, T value, TypeToken<T> typeToken) {
        this.key = key;
        this.comment = comment;
        this.defaultValue = value;
        this.typeToken = typeToken;
    }

    public ConfigValue<T> load(ConfigurationNode configurationNode) {
        this.value = getValueInternal(configurationNode);
        return this;
    }

    public ConfigValue<T> save(ConfigurationNode configurationNode) {
        setValueInternal(configurationNode);
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

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public TypeToken<T> getTypeToken() {
        return this.typeToken == null ? TypeToken.of((Class<T>) defaultValue.getClass()) : typeToken;
    }

    private void setValueInternal(ConfigurationNode configurationNode) {
        ConfigurationNode node = configurationNode.getNode(key);
        if(comment != null && node instanceof CommentedConfigurationNode) {
            ((CommentedConfigurationNode)node).setComment(comment);
        }

        if(typeToken != null) {
            try {
                node.setValue(typeToken, value);
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }
        } else {
            node.setValue(value);
        }
    }

    private T getValueInternal(ConfigurationNode configurationNode) {
        ConfigurationNode node = configurationNode.getNode(key);
        if(node.isVirtual()) {
            setValueInternal(configurationNode);
        }

        if(comment != null && node instanceof CommentedConfigurationNode) {
            ((CommentedConfigurationNode)node).setComment(comment);
        }

        try {
            if(typeToken != null)
                return node.getValue(typeToken, defaultValue);
            else
                return (T) node.getValue(defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }
    }
}
