/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package com.sk89q.craftbook.mechanics.variables;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.craftbook.mechanics.variables.exception.InvalidVariableException;
import com.sk89q.craftbook.mechanics.variables.exception.VariableException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class VariableKey {

    private static final Pattern ALLOWED_KEY_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");

    private final String namespace;
    private final String variable;
    private final boolean explicitNamespace;

    private VariableKey(String namespace, String variable, boolean explicitNamespace) {
        checkNotNull(namespace);
        checkNotNull(variable);

        this.namespace = namespace;
        this.variable = variable;
        this.explicitNamespace = explicitNamespace;
    }

    /**
     * Gets the namespace of this variable key.
     *
     * @return The namespace
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Gets the variable name of this variable key
     *
     * @return The variable name
     */
    public String getVariable() {
        return this.variable;
    }

    /**
     * Gets the original form this variable was written in.
     *
     * <p>
     *     If the namespace was not provided, it will not be shown here.
     * </p>
     *
     * @return the original form.
     */
    public String getOriginalForm() {
        if (explicitNamespace) {
            return namespace + "|" + variable;
        }
        return variable;
    }

    public Component getRichName() {
        TextComponent mainText;
        if (this.namespace.contains("-")) {
            String fakeNamespace = Bukkit.getOfflinePlayer(UUID.fromString(this.namespace)).getName();
            if (fakeNamespace != null) {
                mainText = TextComponent.empty().append(
                        TextComponent.of("*" + fakeNamespace, TextColor.GRAY, TextDecoration.ITALIC)
                ).append(TextComponent.of("|" + this.variable, TextColor.WHITE));
            } else {
                mainText = TextComponent.of(toString(), TextColor.WHITE);
            }
        } else {
            mainText = TextComponent.of(toString());
        }

        Component copyComponent = TranslatableComponent.of("craftbook.variables.list.copy");
        if (this.namespace.contains("-")) {
            mainText = mainText
                    .hoverEvent(HoverEvent.showText(TextComponent.of(toString()).append(TextComponent.newline().append(copyComponent))));
        } else {
            mainText = mainText
                    .hoverEvent(HoverEvent.showText(copyComponent));
        }

        return mainText.clickEvent(ClickEvent.copyToClipboard(toString()));
    }

    @Override
    public String toString() {
        return namespace + "|" + variable;
    }

    /**
     * Checks a players ability to interact with variables.
     *
     * @param actor The one who is attempting to interact.
     * @param action The action
     * @return true if allowed.
     */
    public boolean hasPermission(Actor actor, String action) {
        if (getNamespace().equals(actor.getUniqueId().toString())) {
            if (actor.hasPermission("craftbook.variables." + action + ".self")
                    || actor.hasPermission("craftbook.variables." + action + ".self." + getVariable())) {
                return true;
            }
        }

        return actor.hasPermission("craftbook.variables." + action + "")
                || actor.hasPermission("craftbook.variables." + action + '.' + getNamespace())
                || actor.hasPermission("craftbook.variables." + action + '.' + getNamespace() + '.' + getVariable());
    }

    /**
     * Parse a variable key from a string.
     *
     * <p>
     *     Note: This refers to a directly entered variable
     *     `a|b` rather than in-line format `%a|b%`.
     * </p>
     *
     * <p>
     *     If an actor is provided, the user's namespace <em>may</em> be used.
     * </p>
     *
     * @param line The line
     * @param actor The actor to use for default namespacing
     * @return The variable key
     */
    public static VariableKey fromString(String line, @Nullable Actor actor) throws VariableException {
        Matcher matcher = VariableManager.DIRECT_VARIABLE_PATTERN.matcher(line);

        if (matcher.find()) {
            String namespace = matcher.group(1);
            String key = matcher.group(2);

            return of(namespace, key, actor);
        } else {
            return null;
        }
    }

    /**
     * Create a new VariableKey from a namespace and key.
     *
     * <p>
     *      If an actor is provided, the user's namespace <em>may</em> be used.
     * </p>
     *
     * @param namespace The namespace
     * @param key The key
     * @param actor The actor, if applicable
     * @return The VariableKey, if valid
     */
    public static VariableKey of(@Nullable String namespace, String key, @Nullable Actor actor) throws VariableException {
        boolean explicit = namespace != null;

        if (!ALLOWED_KEY_PATTERN.matcher(key).matches()) {
            throw new InvalidVariableException(TranslatableComponent.of("craftbook.variables.invalid-key", TextComponent.of(key)));
        }

        if (namespace == null || namespace.trim().isEmpty()) {
            if (VariableManager.instance.defaultToGlobal || actor == null) {
                namespace = VariableManager.GLOBAL_NAMESPACE;
            } else {
                namespace = actor.getUniqueId().toString();
            }
        } else {
            if (namespace.contains("-")) {
                // Namespaces may only contain '-' if they are UUIDs.
                try {
                    //noinspection ResultOfMethodCallIgnored
                    UUID.fromString(namespace);
                } catch (IllegalArgumentException ignored) {
                    throw new InvalidVariableException(TranslatableComponent.of("craftbook.variables.invalid-namespace", TextComponent.of(namespace)));
                }
            } else if (!ALLOWED_KEY_PATTERN.matcher(namespace).matches()) {
                throw new InvalidVariableException(TranslatableComponent.of("craftbook.variables.invalid-namespace", TextComponent.of(namespace)));
            }
        }

        return new VariableKey(namespace, key, explicit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VariableKey that = (VariableKey) o;
        return namespace.equals(that.namespace) &&
                variable.equals(that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, variable);
    }
}
