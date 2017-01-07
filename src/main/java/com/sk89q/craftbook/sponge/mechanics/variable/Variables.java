/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.variable;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.mechanics.variable.command.GetVariableCommand;
import com.sk89q.craftbook.sponge.mechanics.variable.command.RemoveVariableCommand;
import com.sk89q.craftbook.sponge.mechanics.variable.command.SetVariableCommand;
import com.sk89q.craftbook.sponge.util.TextUtil;
import com.sk89q.craftbook.sponge.util.type.TypeTokens;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Module(moduleName = "Variables", onEnable="onInitialize", onDisable="onDisable")
public class Variables extends SpongeMechanic {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("%(?:([a-zA-Z0-9]+)\\|)*([a-zA-Z0-9]+)%");

    public static Variables instance;

    private Map<String, Map<String, String>> variableStore;

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        instance = this;

        try {
            variableStore = config.getNode("variables").getValue(new TypeTokens.VariableTypeToken(), Maps.newHashMap());
        } catch (ObjectMappingException e) {
            CraftBookPlugin.spongeInst().getLogger().warn("Failed to read variables! Resetting..", e);
            variableStore = new HashMap<>();
        }

        CommandSpec setVariable = CommandSpec.builder()
                .description(Text.of("Set the value of a variable"))
                .arguments(GenericArguments.string(Text.of("key")), GenericArguments.string(Text.of("value")))
                .executor(new SetVariableCommand(this, false))
                .build();

        CommandSpec setGlobalVariable = CommandSpec.builder()
                .description(Text.of("Set the value of a global variable"))
                .arguments(GenericArguments.string(Text.of("key")), GenericArguments.string(Text.of("value")))
                .executor(new SetVariableCommand(this, true))
                .build();

        CommandSpec getVariable = CommandSpec.builder()
                .description(Text.of("Get the value of a variable"))
                .arguments(GenericArguments.string(Text.of("key")))
                .executor(new GetVariableCommand(this, false))
                .build();

        CommandSpec getGlobalVariable = CommandSpec.builder()
                .description(Text.of("Get the value of a global variable"))
                .arguments(GenericArguments.string(Text.of("key")))
                .executor(new GetVariableCommand(this, true))
                .build();

        CommandSpec removeVariable = CommandSpec.builder()
                .description(Text.of("Removes a variable"))
                .arguments(GenericArguments.string(Text.of("key")))
                .executor(new RemoveVariableCommand(this, false))
                .build();

        CommandSpec removeGlobalVariable = CommandSpec.builder()
                .description(Text.of("Removes a global variable"))
                .arguments(GenericArguments.string(Text.of("key")))
                .executor(new RemoveVariableCommand(this, true))
                .build();

        CommandSpec variableCommand = CommandSpec.builder()
                .description(Text.of("Base Variable command"))
                .child(setVariable, "set", "def", "define")
                .child(setGlobalVariable, "setglobal", "defglobal", "defineglobal")
                .child(getVariable, "get")
                .child(getGlobalVariable, "getglobal")
                .child(removeVariable, "rm", "del", "remove")
                .child(removeGlobalVariable, "rmglobal", "delglobal", "removeglobal")
                .build();

        Sponge.getGame().getCommandManager().register(CraftBookPlugin.spongeInst(), variableCommand, "var", "variable", "variables");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            config.getNode("variables").setValue(new TypeToken<Map<String, Map<String, String>>>() {}, variableStore);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public void addVariable(String namespace, String key, String value) {
        Map<String, String> map = variableStore.getOrDefault(namespace, new HashMap<>());
        map.put(key, value);
        variableStore.put(namespace, map);
    }

    public String getVariable(String namespace, String key) {
        Map<String, String> map = variableStore.getOrDefault(namespace, new HashMap<>());
        return map.getOrDefault(key, null);
    }

    public void removeVariable(String namespace, String key) {
        Map<String, String> map = variableStore.getOrDefault(namespace, new HashMap<>());
        map.remove(key);
        if(!map.isEmpty())
            variableStore.put(namespace, map);
        else
            variableStore.remove(namespace);
    }

    public String parseVariables(String line, @Nullable Player player) {
        for(Pair<String, String> possibleVariable : getPossibleVariables(line)) {
            String namespace = possibleVariable.getLeft();
            String name = possibleVariable.getRight();

            boolean explicit = true;
            if (namespace == null) {
                namespace = "global";
                explicit = false;
            }

            if("global".equals(namespace) && player != null && !explicit && getVariable(player.getUniqueId().toString(), name) != null)
                namespace = player.getUniqueId().toString();

            String variable = getVariable(namespace, name);

            if(variable != null) {
                String variableText = (explicit ? (namespace + '|') : "") + name;
                line = line.replace('%' + variableText + '%', getVariable(namespace, name));
            }
        }

        return line;
    }

    @Listener
    public void onCommandSend(SendCommandEvent event) {
        Player source = null;
        if(event.getCause().first(Player.class).isPresent())
            source = event.getCause().first(Player.class).get();
        if(!event.getArguments().isEmpty())
            event.setArguments(parseVariables(event.getArguments(), source));
        event.setCommand(parseVariables(event.getCommand(), source));
    }

    @Listener
    public void onPlayerChat(MessageChannelEvent event) {
        event.setMessage(TextUtil.transform(event.getMessage(), old -> {
            if (old instanceof LiteralText) {
                LiteralText literal = (LiteralText) old;
                return literal.toBuilder().content(parseVariables(literal.getContent(), event.getCause().first(Player.class).orElse(null))).toText();
            }

            return old;
        }));
    }

    public static Set<Pair<String, String>> getPossibleVariables(String line) {
        Set<Pair<String, String>> variables = new HashSet<>();

        if(!line.contains("%"))
            return variables;

        Matcher matcher = VARIABLE_PATTERN.matcher(line);

        while (matcher.find()) {
            String namespace = matcher.group(1);
            String key = matcher.group(2);
            variables.add(new ImmutablePair<>(namespace, key));
        }

        return variables;
    }

    public static boolean isValidVariableKey(String key) {
        return !key.contains("|") && !key.contains("%");
    }
}
