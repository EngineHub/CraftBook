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

package com.sk89q.craftbook.mechanics.cauldron;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * @author Silthus
 */
public class CauldronCommands {

    public CauldronCommands(CraftBookPlugin plugin) {

    }

    @Command(aliases = {"reload"}, desc = "Reloads the cauldron recipes from the config.")
    @CommandPermissions("craftbook.mech.cauldron.reload")
    public void reload(CommandContext context, CommandSender sender) {

        if(ImprovedCauldron.instance == null) return;
        CraftBookPlugin.inst().createDefaultConfiguration("cauldron-recipes.yml");
        ImprovedCauldron.instance.recipes = new ImprovedCauldronCookbook(new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "cauldron-recipes.yml"), true, YAMLFormat.EXTENDED), CraftBookPlugin.inst().getLogger());
        sender.sendMessage(ChatColor.YELLOW + "Reloaded Cauldron Recipes...");
    }
}