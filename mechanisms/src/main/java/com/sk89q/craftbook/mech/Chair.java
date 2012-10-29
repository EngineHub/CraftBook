package com.sk89q.craftbook.mech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.Packet40EntityMetadata;
import net.minecraft.server.WatchableObject;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.LocationUtil;


/**
 * @author Me4502
 */
public class Chair implements Listener {

    public Chair(MechanismsPlugin plugin) {

        this.plugin = plugin;
        chairs = plugin.getLocalConfiguration().chairSettings.chairs;
    }

    private MechanismsPlugin plugin;
    private Map<String, Block> chairs = new HashMap<String, Block>();

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!plugin.getLocalConfiguration().chairSettings.enable) return;
        if (chairs.containsValue(event.getBlock())) {
            for (Entry<String, Block> e : chairs.entrySet())
                if (e.getValue() == event.getBlock()) {
                    Player p = plugin.getServer().getPlayer(e.getKey());
                    Packet40EntityMetadata packet = new Packet40EntityMetadata(p.getEntityId(), new ChairWatcher((byte) 0), true);
                    for (Player play : LocationUtil.getNearbyPlayers(event.getBlock(), plugin.getServer().getViewDistance() * 16)) {
                        ((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
                    }
                    chairs.remove(p.getName());
                }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {

        if (chairs.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {

        if (chairs.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {

        if (chairs.containsKey(event.getPlayer().getName())) {
            chairs.remove(event.getPlayer().getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getLocalConfiguration().chairSettings.enable) return;
        if (event.getClickedBlock() == null || !plugin.getLocalConfiguration().chairSettings.canUseBlock(event
                .getClickedBlock().getType()))
            return; //???

        BukkitPlayer player = new BukkitPlayer(plugin, event.getPlayer());

        //Now everything looks good, continue;
        if (player.getPlayer().getItemInHand() == null || !player.getPlayer().getItemInHand().getType().isBlock() ||
                player.getPlayer().getItemInHand().getTypeId() == 0) {
            if (plugin.getLocalConfiguration().chairSettings.requireSneak)
                if (!player.getPlayer().isSneaking())
                    return;
            if (!player.hasPermission("craftbook.mech.chair.use")) {
                player.printError("mech.use-permission");
                return;
            }
            if (chairs.containsKey(player.getPlayer().getName())) { //Stand
                Packet40EntityMetadata packet = new Packet40EntityMetadata(player.getPlayer().getEntityId(),
                        new ChairWatcher((byte) 0), true);
                for (Player play : LocationUtil.getNearbyPlayers(event.getClickedBlock(),
                        plugin.getServer().getViewDistance() * 16)) {
                    ((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
                }
                chairs.remove(player.getPlayer().getName());
            } else { //Sit
                if (chairs.containsValue(event.getClickedBlock()))
                    return;
                player.getPlayer().teleport(event.getClickedBlock().getLocation().add(0.5, 0,
                        0.5)); //Teleport to the seat
                Packet40EntityMetadata packet = new Packet40EntityMetadata(player.getPlayer().getEntityId(),
                        new ChairWatcher((byte) 4), true);
                for (Player play : LocationUtil.getNearbyPlayers(event.getClickedBlock(),
                        plugin.getServer().getViewDistance() * 16)) {
                    ((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
                }
                chairs.put(player.getPlayer().getName(), event.getClickedBlock());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) { //Stop players leaving there chair.
        if (chairs.containsKey(event.getPlayer().getName())) {
            if (chairs.get(event.getPlayer().getName()).getLocation().getWorld() != event.getPlayer().getWorld()) {
                chairs.remove(event.getPlayer().getName());
                return;
            }
            if (chairs.get(event.getPlayer().getName()).getLocation().add(0.5, 0, 0.5).distance(event.getTo()) > 1.0D) {
                Location loc = chairs.get(event.getPlayer().getName()).getLocation().add(0.5, 0, 0.5);
                loc.setPitch(event.getPlayer().getLocation().getPitch());
                loc.setYaw(event.getPlayer().getLocation().getYaw());
                event.getPlayer().teleport(loc);
                event.setCancelled(true);
            }
        }
    }

    public static class ChairWatcher extends DataWatcher {

        private byte metadata;

        public ChairWatcher(byte metadata) {

            this.metadata = metadata;
        }

        @Override
        public ArrayList<WatchableObject> b() {

            ArrayList<WatchableObject> list = new ArrayList<WatchableObject>();
            WatchableObject wo = new WatchableObject(0, 0, metadata);
            list.add(wo);
            return list;
        }
    }
}