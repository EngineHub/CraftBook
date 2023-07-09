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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.mechanic.MechanicTypes;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.mechanics.ic.ICManager;
import org.enginehub.craftbook.mechanics.variables.exception.VariableException;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class VariableManager extends AbstractCraftBookMechanic {

    public static final Pattern ALLOWED_VALUE_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("%(?:([a-zA-Z0-9_\\-]+)\\|)?([a-zA-Z0-9_]+)%");
    protected static final Pattern DIRECT_VARIABLE_PATTERN = Pattern.compile("^(?:([a-zA-Z0-9_\\-]+)\\|)?([a-zA-Z0-9_]+)$");

    public static final String GLOBAL_NAMESPACE = "global";

    public static VariableManager instance;

    private VariableConfiguration variableConfiguration;

    private Map<String, Map<String, String>> variableStore;

    @Override
    public void enable() throws MechanicInitializationException {
        instance = this;

        CraftBookPlugin.logDebugMessage("Initializing Variables!", "startup.variables");
        variableStore = new ConcurrentHashMap<>();

        try {
            File varFile = new File(CraftBookPlugin.inst().getDataFolder(), "variables.yml");
            if (!varFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                varFile.createNewFile();
            }
            variableConfiguration = new VariableConfiguration(new YAMLProcessor(varFile, true, YAMLFormat.EXTENDED));
            variableConfiguration.load();
        } catch (Exception e) {
            throw new MechanicInitializationException(MechanicTypes.VARIABLES, TranslatableComponent.of("craftbook.variables.failed-to-load"), e);
        }

        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
            "variables",
            Lists.newArrayList("var", "variable", "vars"),
            "CraftBook Variable Commands",
            VariableCommands::register
        );
    }

    @Override
    public void disable() {
        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.unregisterTopLevel("variables");
        registrar.unregisterTopLevel("var");
        registrar.unregisterTopLevel("variable");
        registrar.unregisterTopLevel("vars");

        if (variableConfiguration != null) {
            variableConfiguration.save();
            variableConfiguration = null;
        }

        variableStore.clear();
        instance = null;
    }

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
    @Nullable
    public String getVariable(VariableKey key) {
        checkNotNull(key);

        Map<String, String> namespacedVariables = this.variableStore.getOrDefault(key.getNamespace(), ImmutableMap.of());
        return namespacedVariables.get(key.getVariable());
    }

    /**
     * Sets the value of a variable, with the given namespace.
     *
     * <p>
     * To remove variables, use {@link VariableManager#removeVariable(VariableKey)}.
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
        resetICCache(key);
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
            resetICCache(key);
        }
    }

    private boolean signHasVariable(VariableKey variableKey, ChangedSign sign) {
        for (Component comp : sign.getLines()) {
            String line = PlainTextComponentSerializer.plainText().serialize(comp);
            if (line.contains("%" + variableKey.toString() + "%")) {
                return true;
            } else if (variableKey.getNamespace().equals(GLOBAL_NAMESPACE)
                || variableKey.getNamespace().contains("-")) {
                if (line.contains("%" + variableKey.getVariable() + "%")) {
                    return true;
                }
            }
        }

        return false;
    }

    private void resetICCache(VariableKey variableKey) {
        // TODO Revisit once doing ICs
        if (ICManager.inst() != null) { //Make sure IC's are enabled.
            ICManager.getCachedICs().entrySet()
                .removeIf(ic -> signHasVariable(variableKey, ic.getValue().getSign()));
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

    public static Collection<VariableKey> getPossibleVariables(Component line, @Nullable Actor actor) {
        return getPossibleVariables(PlainTextComponentSerializer.plainText().serialize(line), actor);
    }

    public static Collection<VariableKey> getPossibleVariables(String line, @Nullable Actor actor) {
        if (!line.contains("%")) {
            return ImmutableList.of();
        }

        Set<VariableKey> variables = new HashSet<>();

        Matcher matcher = VARIABLE_PATTERN.matcher(line);

        while (matcher.find()) {
            String namespace = matcher.group(1);
            String key = matcher.group(2);
            try {
                variables.add(VariableKey.of(namespace, key, actor));
            } catch (VariableException ignored) {
                // We can ignore this, it wasn't a valid match.
            }
        }

        return variables;
    }

    public static Component renderVariables(Component line, @Nullable Actor actor) {
        checkNotNull(line);

        for (VariableKey possibleVariable : getPossibleVariables(line, actor)) {
            CraftBookPlugin.logDebugMessage("Possible variable: (" + possibleVariable.toString() + ") detected!", "variables.line-parsing");

            if (actor != null) {
                if (!possibleVariable.hasPermission(actor, "use")) {
                    continue;
                }
            }

            CraftBookPlugin.logDebugMessage(possibleVariable + " permissions granted!", "variables.line-parsing");

            String value = instance.getVariable(possibleVariable);
            if (value != null) {
                TextReplacementConfig config = TextReplacementConfig.builder()
                    .matchLiteral("%" + possibleVariable.getOriginalForm() + "%")
                    .replacement(value).build();
                line = line.replaceText(config);
            }
        }

        TextReplacementConfig config = TextReplacementConfig.builder()
            .matchLiteral("\\%")
            .replacement("%").build();

        return line.replaceText(config);
    }

    public static String renderVariables(String line, @Nullable Actor actor) {
        return PlainTextComponentSerializer.plainText().serialize(renderVariables(Component.text(line), actor));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncChatEvent event) {
        if (playerChatOverride && event.getPlayer().hasPermission("craftbook.variables.chat")) {
            event.message(renderVariables(event.message(), CraftBookPlugin.inst().wrapPlayer(event.getPlayer())));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (playerCommandOverride && event.getPlayer().hasPermission("craftbook.variables.commands")) {
            event.setMessage(renderVariables(event.getMessage(), CraftBookPlugin.inst().wrapPlayer(event.getPlayer())));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onConsoleCommandPreprocess(ServerCommandEvent event) {
        if (consoleOverride) {
            event.setCommand(renderVariables(event.getCommand(), null));
        }
    }

    protected boolean defaultToGlobal;
    private boolean consoleOverride;
    private boolean playerCommandOverride;
    private boolean playerChatOverride;
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
