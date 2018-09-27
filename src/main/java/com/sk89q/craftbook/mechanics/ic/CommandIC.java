package com.sk89q.craftbook.mechanics.ic;

import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.CommandContext;

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