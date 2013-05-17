package com.sk89q.craftbook.circuits.ic;

import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.CommandContext;

public interface CommandIC {

    /**
     * Called when the {@link IC} recieves a command.
     * 
     * @param args The {@link CommandContext} for this command. Context 0 is the IC ID.
     * @param sender The {@link CommandSender} of this command.
     */
    public void onICCommand(CommandContext args, CommandSender sender);

    /**
     * Check the minimum arguments this command requires to be valid.
     * 
     * @return the amount of arguments required.
     */
    public int getMinCommandArgs();
}