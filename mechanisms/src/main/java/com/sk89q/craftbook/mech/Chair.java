package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.LocationUtil;
import net.minecraft.server.DataWatcher;
import net.minecraft.server.Packet40EntityMetadata;
import net.minecraft.server.WatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;


/**
 * @author Me4502
 */
public class Chair implements Listener {

    public Chair(MechanismsPlugin plugin) {

        this.plugin = plugin;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new ChairChecker(), 40L, 40L);
    }

    public void addChair(Player player, Block block) {
        plugin.getLocalConfiguration().chairSettings.chairs.put(player.getName(), block);
    }

    public void removeChair(Player player) {
        plugin.getLocalConfiguration().chairSettings.chairs.remove(player.getName());
    }

    public Block getChair(Player player) {
        return plugin.getLocalConfiguration().chairSettings.chairs.get(player.getName());
    }

    public Player getChair(Block player) {
        return Bukkit.getPlayer(plugin.getLocalConfiguration().chairSettings.chairs.inverse().get(player));
    }

    public boolean hasChair(Player player) {
        return plugin.getLocalConfiguration().chairSettings.chairs.containsKey(player.getName());
    }

    public boolean hasChair(Block player) {
        return plugin.getLocalConfiguration().chairSettings.chairs.containsValue(player);
    }

    private MechanismsPlugin plugin;

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!plugin.getLocalConfiguration().chairSettings.enable) return;
        if (hasChair(event.getBlock())) {
            Player p = getChair(event.getBlock());
            if(!p.isOnline() || !p.getWorld().equals(event.getBlock().getWorld())
                    || p.getLocation().distanceSquared(event.getBlock().getLocation()) > 3*3) {
                removeChair(p);
            }
            Packet40EntityMetadata packet = new Packet40EntityMetadata(p.getEntityId(), new ChairWatcher((byte) 0), true);
            for (Player play : LocationUtil.getNearbyPlayers(event.getBlock(), plugin.getServer().getViewDistance() * 16)) {
                ((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
            }
            removeChair(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getLocalConfiguration().chairSettings.enable) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || !plugin.getLocalConfiguration().chairSettings.canUseBlock(event.getClickedBlock().getTypeId())) return;

        BukkitPlayer player = new BukkitPlayer(plugin, event.getPlayer());

        //Now everything looks good, continue;
        if (player.getPlayer().getItemInHand() == null || !player.getPlayer().getItemInHand().getType().isBlock() ||
                player.getPlayer().getItemInHand().getTypeId() == 0) {
            if (plugin.getLocalConfiguration().chairSettings.requireSneak && !player.getPlayer().isSneaking())
                return;
            if (!player.hasPermission("craftbook.mech.chair.use")) {
                player.printError("mech.use-permission");
                return;
            }
            if (hasChair(player.getPlayer())) { //Stand
                Packet40EntityMetadata packet = new Packet40EntityMetadata(player.getPlayer().getEntityId(),
                        new ChairWatcher((byte) 0), true);
                for (Player play : LocationUtil.getNearbyPlayers(event.getClickedBlock(),
                        plugin.getServer().getViewDistance() * 16)) {
                    ((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
                }
                removeChair(player.getPlayer());
            } else { //Sit
                if (hasChair(event.getClickedBlock())) {
                    Player p = getChair(event.getClickedBlock());
                    if(!p.isOnline() || !p.getWorld().equals(event.getClickedBlock().getWorld())
                            || p.getLocation().distanceSquared(event.getClickedBlock().getLocation()) > 3*3) {
                        removeChair(p);
                    }
                    else
                        return;
                }
                player.getPlayer().teleport(event.getClickedBlock().getLocation().add(0.5, 0, 0.5)); //Teleport to the seat
                Packet40EntityMetadata packet = new Packet40EntityMetadata(player.getPlayer().getEntityId(),
                        new ChairWatcher((byte) 4), true);
                for (Player play : LocationUtil.getNearbyPlayers(event.getClickedBlock(),
                        plugin.getServer().getViewDistance() * 16)) {
                    ((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
                }
                addChair(player.getPlayer(), event.getClickedBlock());
            }
        }
    }

    public class ChairChecker implements Runnable {

        @Override
        public void run () {
            for(String pl : plugin.getLocalConfiguration().chairSettings.chairs.keySet()) {
                Player p = Bukkit.getPlayer(pl);
                if(!p.isOnline() || !p.getWorld().equals(getChair(p).getWorld())
                        || p.getLocation().distanceSquared(getChair(p).getLocation()) > 3*3) {
                    Packet40EntityMetadata packet = new Packet40EntityMetadata(p.getEntityId(),
                            new ChairWatcher((byte) 0), true);
                    for (Player play : LocationUtil.getNearbyPlayers(getChair(p), plugin.getServer().getViewDistance() * 16)) {
                        ((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
                    }
                    removeChair(p);
                }
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