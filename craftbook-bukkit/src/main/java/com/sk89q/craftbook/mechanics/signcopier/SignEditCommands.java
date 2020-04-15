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

package com.sk89q.craftbook.mechanics.signcopier;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.exceptions.FastCommandException;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SignEditCommands {

    public SignEditCommands(CraftBookPlugin plugin) {
    }

    @Command(aliases = {"edit"}, desc = "Edits the copied sign.", usage = "<Line> <Text>", min = 1, max = 2)
    public void editSign(CommandContext context, CommandSender sender) throws CommandException {

        if(SignCopier.signs == null)
            throw new FastCommandException("SignCopier mechanic is not enabled!");

        if(!(sender instanceof Player))
            throw new FastCommandException("This command can only be performed by a player!");

        if(!sender.hasPermission("craftbook.mech.signcopy.edit"))
            throw new CommandPermissionsException();

        if(!SignCopier.signs.containsKey(sender.getName()))
            throw new FastCommandException("You haven't copied a sign!");

        int line = context.getInteger(0, 1);
        String text = context.getString(1, "");

        if (line < 1 || line > 4) {
            throw new FastCommandException("Line out of bounds. Must be between 1 and 4.");
        }

        String[] signCache = SignCopier.signs.get(sender.getName());
        signCache[line - 1] = text;
        SignCopier.signs.put(sender.getName(), signCache);

        sender.sendMessage(ChatColor.YELLOW + "Edited line " + line + ". Text is now: " + text);
    }
}