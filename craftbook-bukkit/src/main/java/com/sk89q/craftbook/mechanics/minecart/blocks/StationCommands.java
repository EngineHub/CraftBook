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

package com.sk89q.craftbook.mechanics.minecart.blocks;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.minecart.StationManager;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;

public class StationCommands {

    public StationCommands(CraftBookPlugin plugin) {

    }

    @Command(aliases = {"station", "st"}, desc = "Commands to manage Craftbook station selection")
    public void st(CommandContext context, CommandSender sender) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only used by a player");
            return;
        }
        Player player = (Player) sender;
        if (context.argsLength() == 0) {
            String stationName = StationManager.getStation(player.getName());

            if (stationName == null)
                sender.sendMessage("You have no station selected.");
            else
                sender.sendMessage("Your currently selected station is " + stationName);
        } else {
            String stationName = context.getString(0);
            StationManager.setStation(player.getName(), stationName);
            sender.sendMessage("Station set to: " + stationName);
        }
    }
}