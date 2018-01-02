/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.variable.command;

import com.sk89q.craftbook.sponge.mechanics.variable.Variables;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class SetVariableCommand implements CommandExecutor {

    private Variables variables;

    public SetVariableCommand(Variables variables) {
        this.variables = variables;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String key = args.<String>getOne("key").get();
        boolean global = args.hasAny("g");
        String namespace = global ? Variables.GLOBAL_NAMESPACE : args.<String>getOne("namespace").orElse(((Player) src).getUniqueId().toString());
        if (namespace.equals(Variables.GLOBAL_NAMESPACE) && !global) {
            src.sendMessage(Text.of(TextColors.RED, "Invalid namespace!"));
            return CommandResult.empty();
        }

        if(!Variables.isValidVariableKey(key)) {
            src.sendMessage(Text.of(TextColors.RED, "Key contains invalid characters!"));
            return CommandResult.empty();
        }

        String value = args.<String>getOne("value").get();

        variables.addVariable(namespace, key, value);
        src.sendMessage(Text.of(TextColors.YELLOW, "Set variable " + key + " to value " + variables.getVariable(namespace, key)));

        return CommandResult.success();
    }
}
