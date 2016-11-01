/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.craftbook.core.util;

import com.google.common.reflect.TypeToken;
import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.Objects;

public class ConfigValue<T> {

    private String key;
    private String comment;
    private T defaultValue;
    private T value;

    private TypeToken<T> typeToken;

    private boolean modified;

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
        save(configurationNode);
        return this;
    }

    public ConfigValue<T> save(ConfigurationNode configurationNode) {
        setValueInternal(configurationNode);
        return this;
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
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
        this.modified = true;
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

        if (this.modified) {
            if (typeToken != null) {
                try {
                    node.setValue(typeToken, value);
                } catch (ObjectMappingException e) {
                    CraftBookAPI.<CraftBookPlugin>inst().getLogger().error("Failed to map value!", e);
                }
            } else {
                node.setValue(value);
            }
            this.modified = false;
        }
    }

    private T getValueInternal(ConfigurationNode configurationNode) {
        ConfigurationNode node = configurationNode.getNode(key);
        if(node.isVirtual()) {
            this.modified = true;
        }

        if(comment != null && node instanceof CommentedConfigurationNode) {
            ((CommentedConfigurationNode)node).setComment(comment);
        }

        try {
            if(typeToken != null)
                return node.getValue(typeToken, defaultValue);
            else
                return node.getValue(new TypeToken<T>(defaultValue.getClass()){}, defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }
    }
}
