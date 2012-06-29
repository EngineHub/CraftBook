package com.sk89q.craftbook.mech;

import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Snow fall mechanism. Builds up/tramples snow
 * 
 * @author Me4502
 * 
 */
public class Snow implements Listener {

    MechanismsPlugin plugin;

    public Snow(MechanismsPlugin plugin) {
	this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
	if (!plugin.getLocalConfiguration().snowSettings.placeSnow)
	    return;
	if(!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
	try {
	    if (event.getPlayer().getItemInHand().getType() == Material.SNOW_BALL
		    && event.getClickedBlock().getTypeId() == 78) {
		if (event.getClickedBlock().getData() < 7) {
		    setBlockDataWithNotify(event.getClickedBlock(),
			    (byte) (event.getClickedBlock().getData() + 1));
		}
	    } else if (event.getPlayer().getItemInHand().getType() == Material.SNOW_BALL
		    && event.getPlayer()
		    .getWorld()
		    .getBlockAt(
			    event.getClickedBlock().getLocation()
			    .add(0, 1, 0)).getTypeId() == 0) {
		event.getPlayer()
		.getWorld()
		.getBlockAt(
			event.getClickedBlock().getLocation()
			.add(0, 1, 0)).setTypeId(78);
		setBlockDataWithNotify(
			event.getPlayer()
			.getWorld()
			.getBlockAt(
				event.getClickedBlock().getLocation()
				.add(0, 1, 0)), (byte) 1);
	    }
	} catch (Exception e) {
	}
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
	if (!plugin.getLocalConfiguration().snowSettings.trample)
	    return;
	Random random = new Random();
	if (random.nextInt(10) == 6) {
	    Block b = event.getPlayer().getWorld()
		    .getBlockAt(event.getPlayer().getLocation());
	    if (b.getTypeId() == 78) {
		if (b.getData() > 1)
		    setBlockDataWithNotify(b,(byte) (b.getData() - 1));
		else
		    b.setTypeId(0);
	    }

	    b = event
		    .getPlayer()
		    .getWorld()
		    .getBlockAt(
			    event.getPlayer().getLocation().subtract(0, 1, 0));
	    if (b.getTypeId() == 78) {
		if (b.getData() > 1)
		    setBlockDataWithNotify(b,(byte) (b.getData() - 1));
		else
		    b.setTypeId(0);
	    }
	}
    }

    @EventHandler
    public void onBlockForm(final BlockFormEvent event) {
	if (!plugin.getLocalConfiguration().snowSettings.enable)
	    return;
	if (event.getNewState().getTypeId() == BlockID.SNOW) {
	    Block block = event.getBlock();

	    if ((block.getTypeId() != 80) && (block.getTypeId() != 78)) {
		if (block.getWorld()
			.getBlockAt(block.getLocation().subtract(0, 1, 0))
			.getTypeId() == 80
			|| block.getWorld()
			.getBlockAt(
				block.getLocation().subtract(0, 1, 0))
				.getTypeId() == 78)
		    return;
		Random random = new Random();
		long delay = random.nextInt(100) + 60;
		if (plugin
			.getServer()
			.getScheduler()
			.scheduleSyncDelayedTask(plugin,
				new makeSnow(block.getLocation()), delay * 20L) == -1)
		    plugin.getLogger()
		    .log(Level.SEVERE,
			    "[CraftBookMechanisms] Snow Mechanic failed to schedule!");
	    }
	}
    }

    public class makeSnow implements Runnable {

	Location event;

	public makeSnow(Location event) {
	    this.event = event;
	}

	@Override
	public void run() {
	    if (event.getWorld().hasStorm()) {
		if (event.getBlock().getData() > 7)
		    return;
		if (event.subtract(0, 1, 0).getBlock().getTypeId() == 0)
		    return;
		event.add(0, 1, 0);
		if (!(event.getBlock().getTypeId() == 78))
		    return;
		setBlockDataWithNotify(event.getBlock(), (byte) (event
			.getBlock().getData() + 2));
		// if(event.getBlock().getData() >= (byte)7)
		// event.getBlock().setTypeId(80);
		Random random = new Random();
		long delay = random.nextInt(100) + 60;
		if (plugin
			.getServer()
			.getScheduler()
			.scheduleSyncDelayedTask(plugin, new makeSnow(event),
				delay * 20L) == -1)
		    plugin.getLogger()
		    .log(Level.SEVERE,
			    "[CraftBookMechanisms] Snow Mechanic failed to schedule!");
	    } else {
		Random random = new Random();
		long delay = random.nextInt(100) + 600;
		if (plugin
			.getServer()
			.getScheduler()
			.scheduleSyncDelayedTask(plugin, new makeSnow(event),
				delay * 20L) == -1)
		    plugin.getLogger()
		    .log(Level.SEVERE,
			    "[CraftBookMechanisms] Snow Mechanic failed to schedule!");
	    }
	}
    }

    public void setBlockDataWithNotify(Block block, byte data) {
	block.setData(data);
	block.setType(block.getType()); 
	// This notifies a block update, there is a bug in bukkit that doesn't
	// notify the client when Snow's data gets changed
    }
}