package com.sk89q.craftbook.mech;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

public class CustomDrops extends MechanismsPlugin implements Listener{

    MechanismsPlugin plugin;
    
    public CustomDrops(MechanismsPlugin plugin) {
	this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
	if(!event.getPlayer().hasPermission("craftbook.mech.drops")) return;
	if(event.isCancelled()) return;
	int oldId = event.getBlock().getTypeId();
	byte oldData = event.getBlock().getData();
	boolean didDropCustom = false;
	for(String s : plugin.getLocalConfiguration().customDropSettings.blockData) {
	    int newId = Integer.parseInt(s.split("->")[0].split(":")[0]);
	    if(oldId!=newId) continue; //Wrong ID
	    byte newData = 0;
	    if(s.split("->")[0].split(":").length>1) {
		newData = Byte.parseByte(s.split("->")[0].split(":")[1]);
		if(newData!=oldData) continue; //Wrong data
	    }
	    
	    //We have the correct block, now we just need to work out what it should drop.
	    String[] drops = s.split("->")[1].split(",");
	    for(String drop : drops) {
		String[] dropInfo = drop.split(":");
		int dropID = Integer.parseInt(dropInfo[0]);
		byte dropData = 0;
		int dropCount = 1;
		if(dropInfo.length>1)
		    dropData = Byte.parseByte(dropInfo[1]);
		if(dropInfo.length>2) {
		    if(dropInfo[2].contains("-")) {
			String[] ranges = dropInfo[2].split("-");
			int min = Integer.parseInt(ranges[0]);
			int max = Integer.parseInt(ranges[1]);
			dropCount = min + (int)(Math.random() * ((max - min) + 1));
		    }
		    else
			dropCount = Integer.parseInt(dropInfo[2]);
		}
				
		if(dropCount < 1) continue;
		//Add the new drops :)
		didDropCustom = true;
		event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(dropID,dropCount,dropData));
	    }
	}
	if(didDropCustom) {
            event.getBlock().setTypeId(0);
            event.setCancelled(true);
	}
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
	for(String s : plugin.getLocalConfiguration().customDropSettings.mobData) {
	    String newEntity = s.split("->")[0];
	    if(!event.getEntityType().getName().equalsIgnoreCase(newEntity)) continue; //Wrong ID
	    
            event.getDrops().clear();
	    
	    //We have the correct block, now we just need to work out what it should drop.
	    String[] drops = s.split("->")[1].split(",");
	    for(String drop : drops) {
		String[] dropInfo = drop.split(":");
		int dropID = Integer.parseInt(dropInfo[0]);
		byte dropData = 0;
		int dropCount = 1;
		if(dropInfo.length>1)
		    dropData = Byte.parseByte(dropInfo[1]);
		if(dropInfo.length>2) {
		    if(dropInfo[2].contains("-")) {
			String[] ranges = dropInfo[2].split("-");
			int min = Integer.parseInt(ranges[0]);
			int max = Integer.parseInt(ranges[1]);
			dropCount = min + (int)(Math.random() * ((max - min) + 1));
		    }
		    else
			dropCount = Integer.parseInt(dropInfo[2]);
		}
				
		if(dropCount < 1) continue;
		//Add the new drops :)
		event.getDrops().add(new ItemStack(dropID,dropCount,dropData));
	    }
	}
    }
}
