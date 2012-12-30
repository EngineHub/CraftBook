package com.sk89q.craftbook.bukkit.commands;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.VehicleCore;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VehicleCommands {

    VehicleCore vc = VehicleCore.inst();

    public VehicleCommands(CraftBookPlugin plugin) {

    }

    @Command(aliases = {"station", "st"}, desc = "Commands to manage Craftbook station selection")
    public void st(CommandContext context, CommandSender sender) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only used by a player");
            return;
        }
        Player player = (Player) sender;
        if (context.argsLength() == 0) {
            String stationName = vc.getStation(player.getName());

            if (stationName == null) {
                sender.sendMessage("You have no station selected.");
            } else {
                sender.sendMessage("Your currently selected station is " + stationName);
            }
        } else {
            String stationName = context.getString(0);
            vc.setStation(player.getName(), stationName);
            sender.sendMessage("Station set to: " + stationName);
        }

    }
}
