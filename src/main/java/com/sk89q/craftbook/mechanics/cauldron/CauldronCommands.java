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
        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "cauldron-recipes.yml"), "cauldron-recipes.yml");
        ImprovedCauldron.instance.recipes = new ImprovedCauldronCookbook(new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "cauldron-recipes.yml"), true, YAMLFormat.EXTENDED), CraftBookPlugin.inst().getLogger());
        sender.sendMessage(ChatColor.YELLOW + "Reloaded Cauldron Recipes...");
    }
}