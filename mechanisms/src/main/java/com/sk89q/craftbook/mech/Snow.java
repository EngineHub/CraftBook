package com.sk89q.craftbook.mech;

import java.util.Random;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.blocks.BlockID;

public class Snow implements Listener {

	MechanismsPlugin plugin;
	
	public Snow(MechanismsPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!plugin.getLocalConfiguration().snowSettings.trample) return;
		Block b = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation().subtract(0, 1, 0));
		if(b.getTypeId() == 78)
		{
			Random random = new Random();

			if(random.nextInt(10) == 3)
			{
				if(b.getData() > 1)
					b.setData((byte) (b.getData() - 1));
				else
					b.setTypeId(0);
			}
		}
	}
	
	@EventHandler
	public void onBlockForm(final BlockFormEvent event) {
		if(!plugin.getLocalConfiguration().snowSettings.enable) return;
		if (event.getNewState().getTypeId() == BlockID.SNOW) {
			Block block = event.getBlock();

			if ((block.getTypeId() != 80) && (block.getTypeId() != 78)) {
				Random random = new Random();

				long delay = random.nextInt(10) + 60;

				plugin.getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								event.getBlock().setTypeId(78);
								event.getBlock().setData((byte) 2);
							}
						}, delay * 20L);

				delay = random.nextInt(10) + 60;

				plugin.getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								event.getBlock().setTypeId(78);
								event.getBlock().setData((byte) 3);
							}
						}, delay * 20L);

				delay = random.nextInt(10) + 60;

				plugin.getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								event.getBlock().setTypeId(78);
								event.getBlock().setData((byte) 4);
							}
						}, delay * 20L);

				delay = random.nextInt(10) + 60;

				plugin.getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								event.getBlock().setTypeId(78);
								event.getBlock().setData((byte) 5);
							}
						}, delay * 20L);

				delay = random.nextInt(10) + 60;

				plugin.getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								event.getBlock().setTypeId(78);
								event.getBlock().setData((byte) 6);
							}
						}, delay * 20L);

				delay = random.nextInt(10) + 60;

				plugin.getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								event.getBlock().setTypeId(80);
							}
						}, delay * 20L);
			}
		}
	}
}
