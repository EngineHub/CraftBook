package com.sk89q.craftbook.bukkit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mech.SignCopier;
import com.sk89q.craftbook.util.exceptions.FastCommandException;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;

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

        int line = context.getInteger(0, 0);
        String text = context.getString(1, "");

        String[] signCache = SignCopier.signs.get(sender.getName());
        signCache[line - 1] = text;
        SignCopier.signs.put(sender.getName(), signCache);

        sender.sendMessage(ChatColor.YELLOW + "Edited line " + line + ". Text is now: " + text);
    }
}