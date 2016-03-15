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

public class GetVariableCommand implements CommandExecutor {

    Variables variables;
    boolean global;

    public GetVariableCommand(Variables variables, boolean global) {
        this.variables = variables;
        this.global = global;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String key = args.<String>getOne("key").get();

        src.sendMessage(Text.of(TextColors.YELLOW, "Variable " + key +  " is set to " + variables.getVariable(global ? "global" : ((Player) src).getUniqueId().toString(), key)));

        return CommandResult.success();
    }
}
