package com.sk89q.craftbook.bukkit.commands;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * @author Silthus
 */
public class MechanismCommands {

    private final MechanismsPlugin plugin;
    private final WorldEditPlugin worldEdit;

    public MechanismCommands(MechanismsPlugin plugin) {
        this.plugin = plugin;
        worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
    }

    @Command(
            aliases = {"area"},
            desc = "Commands to manage Craftbook Areas"
    )
    @NestedCommand(AreaCommands.class)
    public void area(CommandContext context, CommandSender sender) {
    }


    @Command(
            aliases = {"cauldron"},
            desc = "Commands to manage the Craftbook Cauldron"
    )
    @NestedCommand(CauldronCommands.class)
    public void cauldron(CommandContext context, CommandSender sender) {
    }
}
