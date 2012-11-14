package com.sk89q.craftbook.bukkit.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.VehiclesPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;

public class VehicleCommands {
    
    VehiclesPlugin plugin;

    public VehicleCommands(VehiclesPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Command(
            aliases = {"st"},
            desc = "Commands to manage Craftbook station selection"
            )
    public void st(CommandContext context, CommandSender sender) {
        if(!(sender instanceof Player)){
            sender.sendMessage("This command can only used by a player");
            return;
        }
        Player player = (Player)sender;
        if(context.argsLength() == 0){
            String stationName = plugin.getStation(player.getName());
            
            if(stationName == null){
                sender.sendMessage("You have no station selected.");
            }else{
                sender.sendMessage("Your currently selected station is " + stationName);
            }
        }
        else {
            String stationName = context.getString(0);
            plugin.setStation(player.getName(),stationName);
            sender.sendMessage("Station set to: " + stationName);
        }
        
    }
}
