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

import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.mechanics.ic.ICManager;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.craftbook.util.exceptions.CraftbookException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import org.bukkit.ChatColor;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(VariableManager.instance.hasVariable(variable, key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");

            checkModifyPermissions(actor, key, variable);

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(value).find())
                throw new CraftbookException("Invalid Variable Value!");
            VariableManager.instance.setVariable(variable, key, value);
            resetICCache(variable, key);
            actor.print("Variable is now: " + VariableManager.instance.getVariable(variable, key));
        } else
            throw new CraftbookException("Unknown Variable!");
    }

    @Command(name = "define", desc = "Defines a variable.")
    @CommandPermissions({"craftbook.variables.define"})
    public void define(Actor actor, @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The variable value") String value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {

        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(!VariableManager.instance.hasVariable(variable, key)) {

            if(!hasVariablePermission(actor, key, variable, "define"))
                throw new AuthorizationException();
            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");
            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(value).find())
                throw new CraftbookException("Invalid Variable Value!");
            VariableManager.instance.setVariable(variable, key, value);
            resetICCache(variable, key);
            actor.print("Variable is now: " + VariableManager.instance.getVariable(variable, key));
        } else
            throw new CraftbookException("Existing Variable!");
    }

    @Command(name = "get", desc = "Checks a variable.")
    @CommandPermissions({"craftbook.variables.get"})
    public void get(Actor actor, @Arg(desc = "The variable name") String variable,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(VariableManager.instance.hasVariable(variable, key)) {

            if(!hasVariablePermission(actor, key, variable, "get"))
                throw new AuthorizationException();
            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");
            actor.print(variable + ": " + VariableManager.instance.getVariable(variable, key));
        } else
            throw new CraftbookException("Unknown Variable!");
    }

    @Command(name = "list", desc = "Lists variables")
    @CommandPermissions({"craftbook.variables.list"})
    public void list(Actor actor,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace,
            @Switch(name = 'a', desc = "List all variables") boolean all, @ArgFlag(name = 'p', desc = "Select page number", def = "1") int page) {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if (all)
            key = null;

        List<String> variablesLines = new ArrayList<>();

        for (Entry<Tuple2<String, String>, String> entry : VariableManager.instance.getVariableStore().entrySet()) {
            if (key != null && !entry.getKey().b.equals(key)) {
                continue;
            }

            String keyName = entry.getKey().a;
            if (key == null) {
                keyName = entry.getKey().b + '|' + keyName;
            }

            variablesLines.add(ChatColor.YELLOW + keyName + ChatColor.WHITE + ": " + ChatColor.GREEN + entry.getValue());
        }

        String[] lines = variablesLines.toArray(new String[variablesLines.size()]);
        int pages = (lines.length - 1) / 9 + 1;
        int accessedPage;

        try {
            accessedPage = page - 1;
            if (accessedPage < 0 || accessedPage >= pages) {
                actor.printError("Invalid page \"" + page + '"');
                return;
            }
        } catch (NumberFormatException e) {
            actor.printError("Invalid page \"" + page + '"');
            return;
        }

        actor.printRaw(ChatColor.BLUE + "  ");
        actor.printRaw(ChatColor.BLUE + "Variables (Page " + (accessedPage + 1) + " of " + pages + "):");

        for (int i = accessedPage * 9; i < lines.length && i < (accessedPage + 1) * 9; i++) {
            actor.printRaw(lines[i]);
        }
    }

    @Command(name = "remove", aliases = {"erase","delete","rm"}, desc = "Erase a variable.")
    @CommandPermissions({"craftbook.variables.remove"})
    public void remove(Actor actor, @Arg(desc = "The variable name") String variable,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(VariableManager.instance.hasVariable(variable, key)) {

            if(!hasVariablePermission(actor, key, variable, "erase"))
                throw new AuthorizationException();
            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");
            VariableManager.instance.removeVariable(variable, key);
            resetICCache(variable, key);
            actor.print("Removed variable: " + variable);
        } else
            throw new CraftbookException("Unknown Variable!");
    }

    private static void resetICCache(String variable, String namespace) {

        if(ICManager.inst() != null) {//Make sure IC's are enabled.

            ICManager.getCachedICs().entrySet()
                    .removeIf(ic -> ic.getValue().getSign().hasVariable(namespace + '|' + variable) || ic.getValue().getSign().hasVariable(variable));
        }
    }

    @Command(name = "append", desc = "Append to a variable.")
    @CommandPermissions({"craftbook.variables.append"})
    public void append(Actor actor, @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The appended value") String value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(VariableManager.instance.hasVariable(variable, key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");

            checkModifyPermissions(actor, key, variable);

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(value).find())
                throw new CraftbookException("Invalid Variable Value!");
            VariableManager.instance.setVariable(variable, key, VariableManager.instance.getVariable(variable, key) + value);
            resetICCache(variable, key);
            actor.print("Variable is now: " + VariableManager.instance.getVariable(variable, key));
        } else
            throw new CraftbookException("Unknown Variable!");
    }

    @Command(name = "prepend", desc = "Prepend to a variable.")
    @CommandPermissions({"craftbook.variables.prepend"})
    public void prepend(Actor actor, @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The prepended value") String value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(VariableManager.instance.hasVariable(variable, key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");

            checkModifyPermissions(actor, key, variable);

            if(!RegexUtil.VARIABLE_VALUE_PATTERN.matcher(value).find())
                throw new CraftbookException("Invalid Variable Value!");
            VariableManager.instance.setVariable(variable, key, value + VariableManager.instance.getVariable(variable, key));
            resetICCache(variable, key);
            actor.print("Variable is now: " + VariableManager.instance.getVariable(variable, key));
        } else
            throw new CraftbookException("Unknown Variable!");
    }

    @Command(name = "toggle", desc = "Toggle a boolean.")
    @CommandPermissions({"craftbook.variables.toggle"})
    public void toggle(Actor actor, @Arg(desc = "The variable name") String variable,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(VariableManager.instance.hasVariable(variable, key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");

            checkModifyPermissions(actor, key, variable);

            String var = VariableManager.instance.getVariable(variable, key);
            if(var.equalsIgnoreCase("0") || var.equalsIgnoreCase("1"))
                var = var.equalsIgnoreCase("1") ? "0" : "1";
            else if(var.equalsIgnoreCase("true") || var.equalsIgnoreCase("false"))
                var = var.equalsIgnoreCase("true") ? "false" : "true";
            else if(var.equalsIgnoreCase("yes") || var.equalsIgnoreCase("no"))
                var = var.equalsIgnoreCase("yes") ? "no" : "yes";
            else
                throw new CraftbookException("Variable not of boolean type!");
            VariableManager.instance.setVariable(variable, key, var);
            resetICCache(variable, key);
            actor.print("Variable is now: " + var);
        } else
            throw new CraftbookException("Unknown Variable!");
    }

    @Command(name = "add", desc = "Add to a numeric variable.")
    @CommandPermissions({"craftbook.variables.add"})
    public void add(Actor actor, @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The added value") double value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(VariableManager.instance.hasVariable(variable, key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");

            checkModifyPermissions(actor, key, variable);

            String var = VariableManager.instance.getVariable(variable, key);
            try {

                double f = Double.parseDouble(var);
                f += value;
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch(Exception e) {
                throw new CraftbookException("Variable not of numeric type!");
            }
            VariableManager.instance.setVariable(variable, key, var);
            resetICCache(variable, key);
            actor.print("Variable is now: " + var);
        } else
            throw new CraftbookException("Unknown Variable!");
    }

    @Command(name = "subtract", desc = "Subtract from a numeric variable.")
    @CommandPermissions({"craftbook.variables.subtract"})
    public void subtract(Actor actor, @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The subtracted value") double value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(VariableManager.instance.hasVariable(variable, key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");

            checkModifyPermissions(actor, key, variable);

            String var = VariableManager.instance.getVariable(variable, key);
            try {

                double f = Double.parseDouble(var);
                f -= value;
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch(Exception e) {
                throw new CraftbookException("Variable not of numeric type!");
            }
            VariableManager.instance.setVariable(variable, key, var);
            resetICCache(variable, key);
            actor.print("Variable is now: " + var);
        } else
            throw new CraftbookException("Unknown Variable!");
    }

    @Command(name = "multiply", desc = "Multiply a numeric variable.")
    @CommandPermissions({"craftbook.variables.multiply"})
    public void multiply(Actor actor, @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The multiplying value") double value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(VariableManager.instance.hasVariable(variable, key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");

            checkModifyPermissions(actor, key, variable);

            String var = VariableManager.instance.getVariable(variable, key);
            try {

                double f = Double.parseDouble(var);
                f *= value;
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch(Exception e) {
                throw new CraftbookException("Variable not of numeric type!");
            }
            VariableManager.instance.setVariable(variable, key, var);
            resetICCache(variable, key);
            actor.print("Variable is now: " + var);
        } else
            throw new CraftbookException("Unknown Variable!");
    }

    @Command(name = "divide", desc = "Divide a numeric variable.")
    @CommandPermissions({"craftbook.variables.divide"})
    public void divide(Actor actor, @Arg(desc = "The variable name") String variable,
            @Arg(desc = "The divide value") double value,
            @ArgFlag(name = 'n', desc = "The namespace of the variable") String namespace) throws CraftbookException, AuthorizationException {
        String key = "global";

        if(!VariableManager.instance.defaultToGlobal && actor instanceof CraftBookPlayer)
            key = ((CraftBookPlayer) actor).getCraftBookId();

        if(namespace != null) {
            key = namespace;
        }

        if(VariableManager.instance.hasVariable(variable, key)) {

            if(!RegexUtil.VARIABLE_KEY_PATTERN.matcher(variable).find())
                throw new CraftbookException("Invalid Variable Name!");

            checkModifyPermissions(actor, key, variable);

            String var = VariableManager.instance.getVariable(variable, key);
            try {

                double f = Double.parseDouble(var);
                if(f == 0)
                    throw new CraftbookException("Can't divide by 0!");
                f /= value;
                var = String.valueOf(f);
                if (var.endsWith(".0"))
                    var = var.replace(".0", "");
            } catch (RuntimeException e) {
                throw e;
            } catch(Exception e) {
                throw new CraftbookException("Variable not of numeric type!");
            }
            VariableManager.instance.setVariable(variable, key, var);
            resetICCache(variable, key);
            actor.print("Variable is now: " + var);
        } else
            throw new CraftbookException("Unknown Variable!");
    }

    private static void checkModifyPermissions(Actor actor, String key, String var) throws AuthorizationException {
        if(!hasVariablePermission(actor, key, var, "modify"))
            throw new AuthorizationException();
    }

    /**
     * Checks a players ability to interact with variables.
     * 
     * @param actor The one who is attempting to interact.
     * @param namespace The namespace
     * @param var The variable
     * @param action The action
     * @return true if allowed.
     */
    public static boolean hasVariablePermission(Actor actor, String namespace, String var, String action) {

        if(actor instanceof CraftBookPlayer && namespace.equalsIgnoreCase(((CraftBookPlayer) actor).getCraftBookId()))
            if(actor.hasPermission("craftbook.variables." + action + ".self") || actor.hasPermission("craftbook.variables." + action + ".self." + var))
                return true;

        return !(!actor.hasPermission("craftbook.variables." + action + "")
                && !actor.hasPermission("craftbook.variables." + action + '.' + namespace)
                && !actor.hasPermission("craftbook.variables." + action + '.' + namespace + '.' + var));

    }
}