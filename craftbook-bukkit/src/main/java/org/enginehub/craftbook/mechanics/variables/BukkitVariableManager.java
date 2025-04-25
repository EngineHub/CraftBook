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
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.mechanic.MechanicTypes;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.mechanics.ic.ICManager;
import org.enginehub.craftbook.mechanics.variables.exception.VariableException;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static com.google.common.base.Preconditions.checkNotNull;

public class BukkitVariableManager extends AbstractVariableManager implements Listener {
    private VariableConfiguration variableConfiguration;

    public BukkitVariableManager(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() throws MechanicInitializationException {
        instance = this;

        CraftBookPlugin.logDebugMessage("Initializing Variables!", "startup.variables");

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
            List.of("var", "variable", "vars"),
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

    @Override
    public void updateForVariableChange(VariableKey key) {
        resetICCache(key);
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

    public static Collection<VariableKey> getPossibleVariables(Component line, @Nullable Actor actor) {
        return getPossibleVariables(PlainTextComponentSerializer.plainText().serialize(line), actor);
    }

    public static Collection<VariableKey> getPossibleVariables(String line, @Nullable Actor actor) {
        if (!line.contains("%")) {
            return List.of();
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
}
