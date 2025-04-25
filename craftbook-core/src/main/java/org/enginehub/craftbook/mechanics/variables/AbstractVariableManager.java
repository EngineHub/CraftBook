/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.mechanics.variables;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sk89q.util.yaml.YAMLProcessor;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractVariableManager extends AbstractCraftBookMechanic {

    public static final Pattern ALLOWED_VALUE_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("%(?:([a-zA-Z0-9_\\-]+)\\|)?([a-zA-Z0-9_]+)%");
    public static final Pattern DIRECT_VARIABLE_PATTERN = Pattern.compile("^(?:([a-zA-Z0-9_\\-]+)\\|)?([a-zA-Z0-9_]+)$");

    public static final String GLOBAL_NAMESPACE = "global";

    public static @Nullable AbstractVariableManager instance;

    protected final Map<String, Map<String, String>> variableStore = new ConcurrentHashMap<>();

    public AbstractVariableManager(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    public abstract void updateForVariableChange(VariableKey key);

    /**
     * Gets whether a variable is set.
     *
     * @param key The variable key
     * @return If the variable exists
     */
    public boolean hasVariable(VariableKey key) {
        checkNotNull(key);

        Map<String, String> namespacedVariables = this.variableStore.getOrDefault(key.getNamespace(), ImmutableMap.of());
        return namespacedVariables.containsKey(key.getVariable());
    }

    /**
     * Gets the value of a variable, with the given namespace.
     *
     * @param key The variable key
     * @return The value, or null if unset
     */
    public @Nullable String getVariable(VariableKey key) {
        checkNotNull(key);

        Map<String, String> namespacedVariables = this.variableStore.getOrDefault(key.getNamespace(), ImmutableMap.of());
        return namespacedVariables.get(key.getVariable());
    }

    /**
     * Sets the value of a variable, with the given namespace.
     *
     * <p>
     * To remove variables, use {@link AbstractVariableManager#removeVariable(VariableKey)}.
     * </p>
     *
     * @param key The variable key
     * @param value The value to set
     */
    public void setVariable(VariableKey key, String value) {
        checkNotNull(key);
        checkNotNull(value);
        checkArgument(value.length() <= maxVariableSize, "Variable value too large");

        Map<String, String> namespacedVariables = this.variableStore.computeIfAbsent(key.getNamespace(), s -> Maps.newHashMap());
        namespacedVariables.put(key.getVariable(), value);
        updateForVariableChange(key);
    }

    /**
     * Removes a variable, with the given namespace.
     *
     * @param key The variable key
     */
    public void removeVariable(VariableKey key) {
        checkNotNull(key);

        Map<String, String> namespacedVariables = this.variableStore.get(key.getNamespace());
        if (namespacedVariables != null) {
            namespacedVariables.remove(key.getVariable());
            updateForVariableChange(key);
        }
    }

    /**
     * Gets an immutable copy of the current variable store.
     *
     * @return The variable store
     */
    public Map<String, Map<String, String>> getVariableStore() {
        return ImmutableMap.copyOf(this.variableStore);
    }

    protected boolean defaultToGlobal;
    protected boolean consoleOverride;
    protected boolean playerCommandOverride;
    protected boolean playerChatOverride;
    private int maxVariableSize;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("default-to-global", "Whether to default to global or the player's namespace when no namespace is provided");
        defaultToGlobal = config.getBoolean("default-to-global", false);

        config.setComment("enable-in-console", "Allows variables to work when used in console commands");
        consoleOverride = config.getBoolean("enable-in-console", false);

        config.setComment("enable-in-player-commands", "Allows variables to work when used in player commands");
        playerCommandOverride = config.getBoolean("enable-in-player-commands", false);

        config.setComment("enable-in-player-chat", "Allows variables to work when used in chat");
        playerChatOverride = config.getBoolean("enable-in-player-chat", false);

        config.setComment("max-variable-length", "The maximum length of a value in a variable");
        maxVariableSize = config.getInt("max-variable-length", 256);
    }

}
