package com.sk89q.craftbook.mech;

import java.util.ArrayList;
import java.util.Map.Entry;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.Packet40EntityMetadata;
import net.minecraft.server.WatchableObject;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class Chair extends AbstractMechanic implements Listener {

    @Override
    public void unload() {
    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {
    }

    @Override
    public boolean isActive() {
	return false; //Not peristsent... <- Pro spelling.
    }

    private Chair(Block trigger, MechanismsPlugin plugin) throws InvalidMechanismException {

	super();
	this.trigger = trigger;
	this.plugin = plugin;
    }

    public Chair(MechanismsPlugin plugin) {

	super();
	this.plugin = plugin;
    }


    private MechanismsPlugin plugin;
    private Block trigger;

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
	if (!plugin.getLocalConfiguration().chairSettings.enable) return;
	if (!BukkitUtil.toWorldVector(event.getBlock()).equals(BukkitUtil.toWorldVector(trigger)))
	    return; //wth? our manager is insane
	if(plugin.getLocalConfiguration().chairSettings.chairs.containsValue(event.getBlock())) { //Stand
	    for (Entry<String, Block> e : plugin.getLocalConfiguration().chairSettings.chairs.entrySet())
		if (e.getValue() == event.getBlock()) {
		    Player p = plugin.getServer().getPlayer(e.getKey());
		    if(p!=null) {
			Packet40EntityMetadata packet = new Packet40EntityMetadata(p.getEntityId(), new ChairWatcher((byte)0));
			for(Chunk c : LocationUtil.getSurroundingChunks(event.getBlock(), plugin.getServer().getViewDistance() * 16)) {
			    for(Entity ent : c.getEntities())
				if(ent instanceof Player)
				    ((CraftPlayer)ent).getHandle().netServerHandler.sendPacket(packet);
			}
			plugin.getLocalConfiguration().chairSettings.chairs.remove(p.getName());
		    }
		}
	}
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
	if(plugin.getLocalConfiguration().chairSettings.chairs.containsKey(event.getPlayer().getName()))
	    event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
	if(plugin.getLocalConfiguration().chairSettings.chairs.containsKey(event.getPlayer().getName()))
	    event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
	if(plugin.getLocalConfiguration().chairSettings.chairs.containsKey(event.getPlayer().getName()))
	    event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
	if(plugin.getLocalConfiguration().chairSettings.chairs.containsKey(event.getPlayer().getName()))
	    plugin.getLocalConfiguration().chairSettings.chairs.remove(event.getPlayer().getName());
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

	if (!plugin.getLocalConfiguration().chairSettings.enable) return;
	if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger)))
	    return; //wth? our manager is insane
	if(!plugin.getLocalConfiguration().chairSettings.canUseBlock(event.getClickedBlock().getType()))
	    return; //???

	BukkitPlayer player = new BukkitPlayer(plugin, event.getPlayer());
	if (!player.hasPermission("craftbook.mech.chair.use")) {
	    player.printError("mech.use-permission");
	    return;
	}

	//Now everything looks good, continue;
	if(player.getPlayer().getItemInHand() == null || player.getPlayer().getItemInHand().getType().isBlock() == false || player.getPlayer().getItemInHand().getTypeId() == 0) {
	    if(plugin.getLocalConfiguration().chairSettings.requireSneak == true)
		if(!player.getPlayer().isSneaking())
		    return;
	    if(plugin.getLocalConfiguration().chairSettings.chairs.containsKey(player.getName())) { //Stand
		Packet40EntityMetadata packet = new Packet40EntityMetadata(player.getPlayer().getEntityId(), new ChairWatcher((byte)0));
		for(Chunk c : LocationUtil.getSurroundingChunks(event.getClickedBlock(), plugin.getServer().getViewDistance() * 16)) {
		    for(Entity e : c.getEntities())
			if(e instanceof Player)
			    ((CraftPlayer)e).getHandle().netServerHandler.sendPacket(packet);
		}
		plugin.getLocalConfiguration().chairSettings.chairs.remove(player.getName());
	    }
	    else { //Sit
		if(plugin.getLocalConfiguration().chairSettings.chairs.containsValue(event.getClickedBlock()))
		    return;
		player.getPlayer().teleport(event.getClickedBlock().getLocation().add(0.5, 0, 0.5)); //Teleport to the seat
		Packet40EntityMetadata packet = new Packet40EntityMetadata(player.getPlayer().getEntityId(), new ChairWatcher((byte)4));
		for(Chunk c : LocationUtil.getSurroundingChunks(event.getClickedBlock(), plugin.getServer().getViewDistance() * 16)) {
		    for(Entity e : c.getEntities())
			if(e instanceof Player)
			    ((CraftPlayer)e).getHandle().netServerHandler.sendPacket(packet);
		}
		plugin.getLocalConfiguration().chairSettings.chairs.put(player.getName(), event.getClickedBlock());
	    }
	}
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) { //Stop players leaving there chair.
	if(event.isCancelled()) return;
	if(plugin.getLocalConfiguration().chairSettings.chairs.containsKey(event.getPlayer().getName()))
	    event.getPlayer().teleport((Location) plugin.getLocalConfiguration().chairSettings.chairs.get(event.getPlayer().getName()));
    }

    public static class ChairWatcher extends DataWatcher {
	private byte metadata;

	public ChairWatcher(byte metadata)
	{
	    this.metadata = metadata;
	}

	@Override
	public ArrayList<WatchableObject> b()
	{
	    ArrayList<WatchableObject> list = new ArrayList<WatchableObject>();
	    WatchableObject wo = new WatchableObject(0, 0, Byte.valueOf(metadata));
	    list.add(wo);
	    return list;
	}
    }

    public static class Factory extends AbstractMechanicFactory<Chair> {

	public Factory(MechanismsPlugin plugin) {

	    this.plugin = plugin;
	}

	private final MechanismsPlugin plugin;

	/**
	 * Find a chair.
	 *
	 * @param pt, the chair.
	 *
	 * @return A chair.
	 *
	 * @throws InvalidMechanismException never
	 */
	@Override
	public Chair detect(BlockWorldVector pt) throws InvalidMechanismException {

	    Block block = BukkitUtil.toBlock(pt);
	    if(plugin.getLocalConfiguration().chairSettings.canUseBlock(block.getType()))
		return new Chair(block, plugin);
	    else {
		return null;
	    }
	}
    }
}