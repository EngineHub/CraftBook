package com.sk89q.craftbook.bukkit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.mech.cauldron.ImprovedCauldronCookbook;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;

/**
 * @author Silthus
 */
public class CauldronCommands {

    public CauldronCommands(MechanismsPlugin plugin) {
    }

    @Command(
	    aliases = {"reload"},
	    desc = "Reloads the cauldron recipes from the config."
	    )
    @CommandPermissions("craftbook.mech.cauldron.reload")
    public void reload(CommandContext context, CommandSender sender) {
	ImprovedCauldronCookbook.INSTANCE.reload();
	sender.sendMessage(ChatColor.YELLOW + "Reloaded Cauldron Recipes...");
    }

}
