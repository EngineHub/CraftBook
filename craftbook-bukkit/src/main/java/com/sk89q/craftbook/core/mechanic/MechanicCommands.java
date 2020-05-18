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

package com.sk89q.craftbook.core.mechanic;

import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.exceptions.CraftbookException;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class MechanicCommands {

    private final CraftBookPlugin plugin;

    public MechanicCommands(CraftBookPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"enable"}, desc = "Enable a mechanic")
    @CommandPermissions({"craftbook.enable-mechanic"})
    public void enable(CommandContext args, final CommandSender sender) throws CraftbookException {

        if(args.argsLength() > 0) {
            String mechanic = args.getString(0);
            MechanicType<?> mechanicType = MechanicType.REGISTRY.get(mechanic);
            if (mechanicType != null) {
                plugin.getMechanicManager().enableMechanic(mechanicType);
                plugin.getConfiguration().enabledMechanics.add(mechanic);
                plugin.getConfiguration().save();

                sender.sendMessage(ChatColor.YELLOW + "Sucessfully enabled " + mechanic);
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to load " + mechanic);
            }
        }
    }

    @Command(aliases = {"disable"}, desc = "Disable a mechanic")
    @CommandPermissions({"craftbook.disable-mechanic"})
    public void disable(CommandContext args, final CommandSender sender) throws CommandPermissionsException {

        if(args.argsLength() > 0) {
            String mechanic = args.getString(0);
            MechanicType<?> mechanicType = MechanicType.REGISTRY.get(mechanic);
            Optional<?> mech = plugin.getMechanicManager().getMechanic(mechanicType);
            if(mech.isPresent() && plugin.getMechanicManager().disableMechanic((CraftBookMechanic) mech.get())) {
                plugin.getConfiguration().enabledMechanics.remove(mechanic);
                plugin.getConfiguration().save();

                sender.sendMessage(ChatColor.YELLOW + "Sucessfully disabled " + args.getString(0));
            } else
                sender.sendMessage(ChatColor.RED + "Failed to remove " + args.getString(0));
        }
    }

}
