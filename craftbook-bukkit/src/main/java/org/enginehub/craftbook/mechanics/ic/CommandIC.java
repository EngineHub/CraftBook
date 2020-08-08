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

package com.sk89q.craftbook.mechanics.ic;

import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.CommandContext;

@Deprecated
public interface CommandIC {

    /**
     * Called when the {@link IC} recieves a command.
     * 
     * @param args The {@link CommandContext} for this command. Context 0 is the IC ID.
     * @param sender The {@link CommandSender} of this command.
     */
    void onICCommand(CommandContext args, CommandSender sender);

    /**
     * Check the minimum arguments this command requires to be valid.
     * 
     * @return the amount of arguments required.
     */
    int getMinCommandArgs();

    /**
     * Gives information on each command.
     * 
     * First dimension of the array is the command, each element is another command.
     * The second dimension contains each piece of data about the command.
     *  0 - Syntax
     *  1 - Permissions
     *  2 - Description
     * 
     * @return The command information
     */
    String[][] getCommandInformation();
}