package com.sk89q.craftbook.mech;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(!plugin.getLocalConfiguration().snowSettings.placeSnow) return;
		if(event.getPlayer().getItemInHand().getType() == Material.SNOW_BALL && event.getClickedBlock().getTypeId() == 78)
		{
			if(event.getClickedBlock().getData() < 7)
				event.getClickedBlock().setData((byte) (event.getClickedBlock().getData() + 1));
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!plugin.getLocalConfiguration().snowSettings.trample) return;
		Block b = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation());
		if(b.getTypeId() == 78)
		{
			Random random = new Random();

			if(random.nextInt(20) == 3)
			{
				if(b.getData() > 1)
					b.setData((byte) (b.getData() - 1));
				else
					b.setTypeId(0);
			}
		}
		else if (b.getTypeId() == 80)
		{
			Random random = new Random();

			if(random.nextInt(20) == 3)
			{
				b.setTypeId(78);
				b.setData((byte)6);
			}
		}
	}
	
	@EventHandler
	public void onBlockForm(final BlockFormEvent event) {
		if(!plugin.getLocalConfiguration().snowSettings.enable) return;
		if (event.getNewState().getTypeId() == BlockID.SNOW) {
			Block block = event.getBlock();

			if ((block.getTypeId() != 80) && (block.getTypeId() != 78)) {
				if(block.getWorld().getBlockAt(block.getLocation().subtract(0, 1, 0)).getTypeId() == 80 || block.getWorld().getBlockAt(block.getLocation().subtract(0, 1, 0)).getTypeId() == 78) return;
				Random random = new Random();
				long delay = random.nextInt(30) + 60;
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new makeSnow(event), delay * 20L);
			}
		}
	}
	
	public class makeSnow implements Runnable {

		BlockFormEvent event;
		
		public makeSnow(BlockFormEvent event)
		{
			this.event = event;
		}
		
		@Override
		public void run() {
			if(!event.getBlock().getWorld().hasStorm()) return;
			if(event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation().subtract(0, 1, 0)).getTypeId() == 0) return;
			event.getBlock().setTypeId(78);
			event.getBlock().setData((byte) (event.getBlock().getData() + 1));
			if(event.getBlock().getData() >= (byte)7) event.getBlock().setTypeId(80);
			else if(event.getBlock().getData()<(byte)7)
			{
				Random random = new Random();
				long delay = random.nextInt(30) + 60;
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new makeSnow(event), delay * 20L);
			}
		}
	}
}
