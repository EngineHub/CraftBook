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
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.mechanics.variable.command.GetVariableCommand;
import com.sk89q.craftbook.sponge.mechanics.variable.command.ListVariableCommand;
import com.sk89q.craftbook.sponge.mechanics.variable.command.RemoveVariableCommand;
import com.sk89q.craftbook.sponge.mechanics.variable.command.SetVariableCommand;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.TextUtil;
import com.sk89q.craftbook.sponge.util.type.TypeTokens;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

@Module(id = "variables", name = "Variables", onEnable="onInitialize", onDisable="onDisable")
public class Variables extends SpongeMechanic implements DocumentationProvider {

    public static final String GLOBAL_NAMESPACE = "global";

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("%(?:([a-zA-Z0-9_]+)\\|)*([a-zA-Z0-9]+)%");

    public static Variables instance;

    private Map<String, Map<String, String>> variableStore;

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode setPermission = new SpongePermissionNode("craftbook.variables.set", "Allows setting variables.", PermissionDescription.ROLE_USER);
    private SpongePermissionNode setGlobalPermission = new SpongePermissionNode("craftbook.variables.set.global", "Allows setting global variables.", PermissionDescription.ROLE_STAFF);

    private SpongePermissionNode getPermission = new SpongePermissionNode("craftbook.variables.get", "Allows getting variables.", PermissionDescription.ROLE_USER);
    private SpongePermissionNode getGlobalPermission = new SpongePermissionNode("craftbook.variables.get.global", "Allows getting global variables.", PermissionDescription.ROLE_STAFF);

    private SpongePermissionNode delPermission = new SpongePermissionNode("craftbook.variables.remove", "Allows removing variables.", PermissionDescription.ROLE_USER);
    private SpongePermissionNode delGlobalPermission = new SpongePermissionNode("craftbook.variables.remove.global", "Allows removing global variables.", PermissionDescription.ROLE_STAFF);

    private SpongePermissionNode listPermission = new SpongePermissionNode("craftbook.variables.list", "Allows listing variables.", PermissionDescription.ROLE_USER);
    private SpongePermissionNode listGlobalPermission = new SpongePermissionNode("craftbook.variables.list.global", "Allows listing global variables.", PermissionDescription.ROLE_STAFF);

    private ConfigValue<Boolean> defaultToGlobal = new ConfigValue<>("default-to-global", "If no namespace is provided, default to global. "
            + "Otherwise personal namespace", true);

    private CommandMapping variableCommandMapping;

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

        setPermission.register();
        setGlobalPermission.register();

        getPermission.register();
        getGlobalPermission.register();

        delPermission.register();
        delGlobalPermission.register();

        listPermission.register();
        listGlobalPermission.register();

        defaultToGlobal.load(config);

        CommandSpec setVariable = CommandSpec.builder()
                .description(Text.of("Set the value of a variable"))
                .permission(setPermission.getNode())
                .arguments(
                        GenericArguments.string(Text.of("key")),
                        GenericArguments.string(Text.of("value")),
                        GenericArguments.flags()
                                .permissionFlag(setGlobalPermission.getNode(), "g", "-global")
                                .valueFlag(GenericArguments.string(Text.of("namespace")), "n", "-namespace")
                                .buildWith(GenericArguments.none()))
                .executor(new SetVariableCommand(this))
                .build();

        CommandSpec getVariable = CommandSpec.builder()
                .description(Text.of("Get the value of a variable"))
                .permission(getPermission.getNode())
                .arguments(
                        GenericArguments.string(Text.of("key")),
                        GenericArguments.flags()
                                .permissionFlag(getGlobalPermission.getNode(), "g", "-global")
                                .valueFlag(GenericArguments.string(Text.of("namespace")), "n", "-namespace")
                                .buildWith(GenericArguments.none()))
                .executor(new GetVariableCommand(this))
                .build();

        CommandSpec removeVariable = CommandSpec.builder()
                .description(Text.of("Removes a variable"))
                .permission(delPermission.getNode())
                .arguments(
                        GenericArguments.string(Text.of("key")),
                        GenericArguments.flags()
                                .permissionFlag(delGlobalPermission.getNode(), "g", "-global")
                                .valueFlag(GenericArguments.string(Text.of("namespace")), "n", "-namespace")
                                .buildWith(GenericArguments.none()))
                .executor(new RemoveVariableCommand(this))
                .build();

        CommandSpec listVariable = CommandSpec.builder()
                .description(Text.of("List variables"))
                .permission(listPermission.getNode())
                .arguments(
                        GenericArguments.flags()
                                .permissionFlag(listPermission.getNode(), "g", "-global")
                                .valueFlag(GenericArguments.string(Text.of("namespace")), "n", "-namespace")
                                .buildWith(GenericArguments.none()))
                .executor(new ListVariableCommand(this))
                .build();

        CommandSpec variableCommand = CommandSpec.builder()
                .description(Text.of("Base Variable command"))
                .child(setVariable, "set", "def", "define")
                .child(getVariable, "get")
                .child(removeVariable, "rm", "del", "remove")
                .child(listVariable, "list", "ls")
                .build();

        variableCommandMapping = Sponge.getGame().getCommandManager().register(CraftBookPlugin.spongeInst(), variableCommand, "var", "variable", "variables").orElse(null);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            config.getNode("variables").setValue(new TypeToken<Map<String, Map<String, String>>>() {}, variableStore);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        if (variableCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(variableCommandMapping);
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

    public Map<String, String> getVariables(String namespace) {
        return variableStore.getOrDefault(namespace, Collections.emptyMap());
    }

    public String parseVariables(String line, @Nullable Player player) {
        for(Pair<String, String> possibleVariable : getPossibleVariables(line)) {
            String namespace = possibleVariable.getLeft();
            String name = possibleVariable.getRight();

            boolean explicit = true;
            if (namespace == null) {
                if (defaultToGlobal.getValue()) {
                    namespace = GLOBAL_NAMESPACE;
                    if(player != null && getVariable(player.getUniqueId().toString(), name) != null) {
                        namespace = player.getUniqueId().toString();
                    }
                } else {
                    if (player != null) {
                        namespace = player.getUniqueId().toString();
                    } else {
                        continue;
                    }
                }
                explicit = false;
            }

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

    @Override
    public String getPath() {
        return "mechanics/variables";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                defaultToGlobal
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                setPermission,
                setGlobalPermission,
                getPermission,
                getGlobalPermission,
                delPermission,
                delGlobalPermission,
                listPermission,
                listGlobalPermission
        };
    }
}
