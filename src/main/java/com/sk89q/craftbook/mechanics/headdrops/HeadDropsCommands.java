package com.sk89q.craftbook.mechanics.headdrops;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadDropsCommands {

    public HeadDropsCommands(CraftBookPlugin plugin) {

    }

    @Command(aliases = {"give"}, desc = "Gives the player the headdrops item.", flags = "p:a:s", usage = "[-p player] <Entity Name> [-a amount] " + "[-s]", min = 1)
    public void giveItem(CommandContext context, CommandSender sender) throws CommandException {
        Player player;

        if(context.hasFlag('p'))
            player = Bukkit.getPlayer(context.getFlag('p'));
        else if(!(sender instanceof Player))
            throw new CommandException("Please provide a player! (-p flag)");
        else
            player = (Player) sender;

        if(player == null)
            throw new CommandException("Unknown Player!");

        if(HeadDrops.instance == null)
            throw new CommandException("HeadDrops are not enabled!");

        EntityType entityType = EntityType.fromName(context.getString(0));

        if (entityType == null) {
            throw new CommandException("Unknown Entity Type.");
        }

        if(!sender.hasPermission("craftbook.mech.headdrops.give" + (context.hasFlag('p') ? ".others" : "") + '.' + entityType))
            throw new CommandPermissionsException();

        HeadDrops.MobSkullType skullType = HeadDrops.MobSkullType.getFromEntityType(entityType);
        if(skullType == null)
            throw new CommandException("Invalid Skull Type!");

        String mobName = skullType.getPlayerName();

        ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta metaD = stack.getItemMeta();
        if(metaD instanceof SkullMeta) {
            SkullMeta itemMeta = (SkullMeta) metaD;
            itemMeta.setDisplayName(ChatColor.RESET + entityType.getName().toUpperCase() + " Head");
            itemMeta.setOwner(mobName);
            stack.setItemMeta(itemMeta);
        } else {
            CraftBookPlugin.logger().warning("Bukkit has failed to set a HeadDrop item to a head!");
        }

        if(context.hasFlag('a'))
            stack.setAmount(stack.getAmount() * context.getFlagInteger('a', 1));

        if(!player.getInventory().addItem(stack).isEmpty()) {
            throw new CommandException("Failed to add item to inventory!");
        }

        if(!context.hasFlag('s'))
            sender.sendMessage(ChatColor.YELLOW + "Gave HeadDrop for " + ChatColor.BLUE + entityType.getName() + ChatColor.YELLOW + " to " + player.getName());
    }
}
