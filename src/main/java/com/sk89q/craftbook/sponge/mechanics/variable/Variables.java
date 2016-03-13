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
package com.sk89q.craftbook.sponge.mechanics.variable;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.mechanics.variable.command.GetVariableCommand;
import com.sk89q.craftbook.sponge.mechanics.variable.command.RemoveVariableCommand;
import com.sk89q.craftbook.sponge.mechanics.variable.command.SetVariableCommand;
import com.sk89q.craftbook.sponge.util.RegexUtil;
import com.sk89q.craftbook.sponge.util.type.VariableTypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Module(moduleName = "Variables", onEnable="onInitialize", onDisable="onDisable")
public class Variables extends SpongeMechanic {

    public static Variables instance;

    Map<String, Map<String, String>> variableStore;

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        instance = this;

        if(config.getValue() != null) {
            try {
                variableStore = config.getNode("variables").getValue(new VariableTypeToken());
            } catch (ObjectMappingException e) {
                e.printStackTrace();
                System.out.println("Failed to read variables! Resetting..");
                variableStore = new HashMap<>();
            }
        } else {
            variableStore = new HashMap<>();
        }

        CommandSpec setVariable = CommandSpec.builder()
                .description(Text.of("Set the value of a variable"))
                .arguments(GenericArguments.string(Text.of("key")), GenericArguments.string(Text.of("value")))
                .executor(new SetVariableCommand(this))
                .build();

        CommandSpec getVariable = CommandSpec.builder()
                .description(Text.of("Get the value of a variable"))
                .arguments(GenericArguments.string(Text.of("key")))
                .executor(new GetVariableCommand(this))
                .build();

        CommandSpec removeVariable = CommandSpec.builder()
                .description(Text.of("Removes a variable"))
                .arguments(GenericArguments.string(Text.of("key")))
                .executor(new RemoveVariableCommand(this))
                .build();

        CommandSpec variableCommand = CommandSpec.builder()
                .description(Text.of("Base Variable command"))
                .child(setVariable, "set", "def", "define")
                .child(getVariable, "get")
                .child(removeVariable, "rm", "del", "remove")
                .build();

        Sponge.getGame().getCommandManager().register(CraftBookPlugin.<CraftBookPlugin>inst(), variableCommand, "var", "variable", "variables");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        config.getNode("variables").setValue(variableStore);
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
        if(map.size() > 0)
            variableStore.put(namespace, map);
        else
            variableStore.remove(namespace);
    }

    public String parseVariables(String line, @Nullable Player player) {
        for(String possibleVariable : getPossibleVariables(line)) {
            String namespace, name;
            boolean explicit = false;

            if(possibleVariable.contains("|")) {
                String[] bits = RegexUtil.PIPE_PATTERN.split(possibleVariable, 2);
                namespace = bits[0];
                name = bits[1];
                explicit = true;
            } else {
                namespace = "global";
                name = possibleVariable;
            }

            if(namespace.equals("global") && player != null && !explicit && getVariable(player.getUniqueId().toString(), name) != null)
                namespace = player.getUniqueId().toString();

            String variable = getVariable(namespace, name);

            if(variable != null)
                line = line.replace('%' + possibleVariable + '%', getVariable(namespace, name));
        }

        return line;
    }

    @Listener
    public void onCommandSend(SendCommandEvent event) {
        Player source = null;
        if(event.getCause().first(Player.class).isPresent())
            source = event.getCause().first(Player.class).get();
        if(event.getArguments().length() > 0)
            event.setArguments(parseVariables(event.getArguments(), source));
        event.setCommand(parseVariables(event.getCommand(), source));
    }

    @Listener
    public void onPlayerChat(MessageChannelEvent event) {
        //TODO Work out how to do proper replacements in the Text API
    }

    public static Set<String> getPossibleVariables(String line) {
        Set<String> variables = new HashSet<>();

        for(String bit : RegexUtil.PERCENT_PATTERN.split(line)) {
            if(line.indexOf(bit) > 0 && line.charAt(line.indexOf(bit)-1) == '\\') continue;
            if(!bit.trim().isEmpty() && !bit.trim().equals("|"))
                variables.add(bit.trim());
        }

        return variables;
    }

    public static boolean isValidVariableKey(String key) {
        return !key.contains("|") && !key.contains("%");
    }
}
