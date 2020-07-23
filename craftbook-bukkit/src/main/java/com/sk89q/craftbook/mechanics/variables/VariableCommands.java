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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.mechanics.variables.exception.ExistingVariableException;
import com.sk89q.craftbook.mechanics.variables.exception.InvalidVariableException;
import com.sk89q.craftbook.mechanics.variables.exception.UnknownVariableException;
import com.sk89q.craftbook.mechanics.variables.exception.VariableException;
import com.sk89q.craftbook.util.exceptions.CraftBookException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.component.InvalidComponentException;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.bukkit.Bukkit;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class VariableCommands {

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration) {
        registration.register(
                commandManager,
                VariableCommandsRegistration.builder(),
                new VariableCommands()
        );
    }

    @Command(name = "set", desc = "Sets a variable.")
    @CommandPermissions({"craftbook.variables.set"})
    public void set(Actor actor,
            @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The variable value") String value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace
    ) throws CraftBookException, AuthorizationException {
        VariableKey key = VariableKey.of(namespace, variable, actor);
        checkVariableExists(key);
        checkModifyPermissions(actor, key);
        checkVariableValue(value);

        VariableManager.instance.setVariable(key, value);
        actor.printInfo(TranslatableComponent.of(
                "craftbook.variables.set",
                key.getRichName(),
                TextComponent.of(value, TextColor.WHITE)
        ));
    }

    @Command(name = "define", desc = "Defines a variable.")
    @CommandPermissions({"craftbook.variables.define"})
    public void define(Actor actor,
            @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The variable value") String value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace
    ) throws CraftBookException, AuthorizationException {
        VariableKey key = VariableKey.of(namespace, variable, actor);

        if (VariableManager.instance.hasVariable(key)) {
            throw new ExistingVariableException(key);
        }

        if (!key.hasPermission(actor, "define")) {
            throw new AuthorizationException();
        }

        checkVariableValue(value);

        VariableManager.instance.setVariable(key, value);
        actor.printInfo(TranslatableComponent.of(
                "craftbook.variables.set",
                key.getRichName(),
                TextComponent.of(value, TextColor.WHITE)
        ));
    }

    @Command(name = "get", desc = "Checks a variable.")
    @CommandPermissions({"craftbook.variables.get"})
    public void get(Actor actor,
            @Arg(desc = "The variable name") String variable,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftBookException, AuthorizationException {
        VariableKey key = VariableKey.of(namespace, variable, actor);
        checkVariableExists(key);

        if (!key.hasPermission(actor, "get")) {
            throw new AuthorizationException();
        }

        String value = VariableManager.instance.getVariable(key);

        actor.printInfo(TranslatableComponent.of(
                "craftbook.variables.get",
                key.getRichName(),
                value == null
                        ? TranslatableComponent.of("craftbook.variables.undefined", TextColor.WHITE)
                        : TextComponent.of(value, TextColor.WHITE)
        ));
    }

    @Command(name = "list", desc = "Lists variables")
    @CommandPermissions({"craftbook.variables.list"})
    public void list(Actor actor,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace,
            @Switch(name = 'a', desc = "List all variables") boolean all,
            @ArgFlag(name = 'p', desc = "Select page number", def = "1") int page
    ) throws InvalidComponentException, VariableException {
        if (!all && namespace == null) {
            if (VariableManager.instance.defaultToGlobal || !(actor instanceof CraftBookPlayer)) {
                namespace = VariableManager.GLOBAL_NAMESPACE;
            } else {
                namespace = actor.getUniqueId().toString();
            }
        } else if (all && namespace != null) {
            actor.printError(TranslatableComponent.of("craftbook.variables.list.all-and-namespace"));
            return;
        }

        List<VariableKey> variableKeys = new ArrayList<>();

        if (namespace != null) {
            Set<String> variableNames = VariableManager.instance.getVariableStore().getOrDefault(namespace, ImmutableMap.of()).keySet();
            for (String variableName : variableNames) {
                variableKeys.add(VariableKey.of(namespace, variableName, actor));
            }
        } else {
            for (Entry<String, Map<String, String>> namespaceEntry : VariableManager.instance.getVariableStore().entrySet()) {
                for (String variable : namespaceEntry.getValue().keySet()) {
                    variableKeys.add(VariableKey.of(namespaceEntry.getKey(), variable, actor));
                }
            }
        }

        VariableListPaginationBox variableListBox = new VariableListPaginationBox(
                variableKeys,
                namespace,
                "/variables list -p %page% " + (namespace == null ? "-a" : "-n " + namespace)
        );
        actor.printInfo(variableListBox.create(page));
    }

    @Command(name = "remove", aliases = {"erase","delete","rm"}, desc = "Erase a variable.")
    @CommandPermissions({"craftbook.variables.remove"})
    public void remove(Actor actor,
            @Arg(desc = "The variable name") String variable,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace
    ) throws CraftBookException, AuthorizationException {
        VariableKey key = VariableKey.of(namespace, variable, actor);
        checkVariableExists(key);

        if (!key.hasPermission(actor, "erase")) {
            throw new AuthorizationException();
        }

        VariableManager.instance.removeVariable(key);
        actor.printInfo(TranslatableComponent.of("craftbook.variables.remove", key.getRichName()));
    }

    @Command(name = "append", desc = "Append to a variable.")
    @CommandPermissions({"craftbook.variables.append"})
    public void append(Actor actor,
            @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The appended value") String value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace
    ) throws CraftBookException, AuthorizationException {
        VariableKey key = VariableKey.of(namespace, variable, actor);
        checkVariableExists(key);
        checkModifyPermissions(actor, key);
        checkVariableValue(value);

        VariableManager.instance.setVariable(key, VariableManager.instance.getVariable(key) + value);
        actor.printInfo(TranslatableComponent.of(
                "craftbook.variables.set",
                key.getRichName(),
                TextComponent.of(Objects.requireNonNull(VariableManager.instance.getVariable(key)), TextColor.WHITE)
        ));
    }

    @Command(name = "prepend", desc = "Prepend to a variable.")
    @CommandPermissions({"craftbook.variables.prepend"})
    public void prepend(Actor actor,
            @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The prepended value") String value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace
    ) throws CraftBookException, AuthorizationException {
        VariableKey key = VariableKey.of(namespace, variable, actor);
        checkVariableExists(key);
        checkModifyPermissions(actor, key);
        checkVariableValue(value);

        VariableManager.instance.setVariable(key, value + VariableManager.instance.getVariable(key));
        actor.printInfo(TranslatableComponent.of(
                "craftbook.variables.set",
                key.getRichName(),
                TextComponent.of(Objects.requireNonNull(VariableManager.instance.getVariable(key)), TextColor.WHITE)
        ));
    }

    @Command(name = "toggle", desc = "Toggle a boolean.")
    @CommandPermissions({"craftbook.variables.toggle"})
    public void toggle(Actor actor,
            @Arg(desc = "The variable name") String variable,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace
    ) throws CraftBookException, AuthorizationException {
        VariableKey key = VariableKey.of(namespace, variable, actor);
        checkVariableExists(key);
        checkModifyPermissions(actor, key);

        String var = VariableManager.instance.getVariable(key);
        if (var != null) {
            String result;
            if (var.equalsIgnoreCase("0") || var.equalsIgnoreCase("1")) {
                result = var.equalsIgnoreCase("1") ? "0" : "1";
            } else if (var.equalsIgnoreCase("true") || var.equalsIgnoreCase("false")) {
                result = var.equalsIgnoreCase("true") ? "false" : "true";
            } else if (var.equalsIgnoreCase("yes") || var.equalsIgnoreCase("no")) {
                result = var.equalsIgnoreCase("yes") ? "no" : "yes";
            } else {
                throw new InvalidVariableException(TranslatableComponent.of(
                        "craftbook.variables.not-boolean",
                        key.getRichName()
                ));
            }

            VariableManager.instance.setVariable(key, result);
            actor.printInfo(TranslatableComponent.of(
                    "craftbook.variables.set",
                    key.getRichName(),
                    TextComponent.of(result, TextColor.WHITE)
            ));
        }
    }

    @Command(name = "setexpr", desc = "Set a variable to the result of a calculation.")
    @CommandPermissions({"craftbook.variables.setexpr"})
    public void setExpr(Actor actor,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace,
            @Arg(desc = "The variable name") String variable,
            @Arg(desc = "Expression to evaluate", variable = true) List<String> input
    ) throws CraftBookException, AuthorizationException {
        VariableKey key = VariableKey.of(namespace, variable, actor);
        checkVariableExists(key);
        checkModifyPermissions(actor, key);

        String var = VariableManager.instance.getVariable(key);
        if (var != null) {
            double f;
            try {
                f = Double.parseDouble(var);
            } catch (NumberFormatException e) {
                throw new InvalidVariableException(TranslatableComponent.of(
                        "craftbook.variables.not-numeric",
                        key.getRichName()
                ));
            }

            Expression expression;
            try {
                expression = Expression.compile(String.join(" ", input), "var");
            } catch (ExpressionException e) {
                actor.printError(TranslatableComponent.of("worldedit.calc.invalid", TextComponent.of(String.join(" ", input))));
                return;
            }

            double result = expression.evaluate(f);

            VariableManager.instance.setVariable(key, String.valueOf(result));
            actor.printInfo(TranslatableComponent.of(
                    "craftbook.variables.set",
                    key.getRichName(),
                    TextComponent.of(result, TextColor.WHITE)
            ));
        }
    }

    private void checkModifyPermissions(Actor actor, VariableKey variableKey) throws AuthorizationException {
        if (!variableKey.hasPermission(actor, "modify")) {
            throw new AuthorizationException();
        }
    }

    private void checkVariableExists(VariableKey key) throws UnknownVariableException {
        if (!VariableManager.instance.hasVariable(key)) {
            throw new UnknownVariableException(key);
        }
    }

    private void checkVariableValue(String value) throws InvalidVariableException {
        if (!VariableManager.ALLOWED_VALUE_PATTERN.matcher(value).find()) {
            throw new InvalidVariableException(TranslatableComponent.of(
                    "craftbook.variables.invalid-value",
                    TextComponent.of(value)
            ));
        }
    }

    private static class VariableListPaginationBox extends PaginationBox {

        private final List<VariableKey> variableKeys;
        private final boolean singleNamespace;

        private static String getFriendlyNamespaceName(String namespace) {
            if (namespace == null) {
                return "All";
            }
            if (namespace.contains("-")) {
                return Bukkit.getOfflinePlayer(UUID.fromString(namespace)).getName();
            }
            return namespace;
        }

        protected VariableListPaginationBox(List<VariableKey> variableKeys, String namespace, String pageCommand) {
            super("Variables (" + getFriendlyNamespaceName(namespace) + ")", pageCommand);

            this.variableKeys = variableKeys;
            this.singleNamespace = namespace != null;
        }

        @Override
        public Component getComponent(int number) {
            checkArgument(number < this.variableKeys.size() && number >= 0);

            VariableKey key = this.variableKeys.get(number);

            String label = key.getVariable();
            if (!this.singleNamespace) {
                label = key.toString();
            }

            String varValue = VariableManager.instance.getVariable(key);
            Component value;
            TextColor valueColor = TextColor.GRAY;
            if (varValue == null) {
                value = TranslatableComponent.of("craftbook.variables.undefined");
                valueColor = TextColor.DARK_GRAY;
            } else {
                value = TextComponent.of(varValue);
            }

            TextComponent labelComponent = TextComponent.of(label)
                    .color(TextColor.YELLOW)
                    .clickEvent(ClickEvent.copyToClipboard(key.toString()));
            Component copyComponent = TranslatableComponent.of("craftbook.variables.list.copy");
            if (this.singleNamespace) {
                labelComponent = labelComponent
                        .hoverEvent(HoverEvent.showText(TextComponent.of(key.toString()).append(TextComponent.newline().append(copyComponent))));
            } else {
                labelComponent = labelComponent
                        .hoverEvent(HoverEvent.showText(copyComponent));
            }

            return TextComponent.builder()
                    .content("")
                    .append(labelComponent)
                    .append(TextComponent.of("="))
                    .append(value.color(valueColor))
                    .build();
        }

        @Override
        public int getComponentsSize() {
            return this.variableKeys.size();
        }
    }

}
