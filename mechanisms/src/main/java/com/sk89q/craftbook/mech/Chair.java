package com.sk89q.craftbook.mech;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.GeneralUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Me4502
 */
public class Chair implements Listener {

    public Chair (MechanismsPlugin plugin) {

        this.plugin = plugin;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new ChairChecker(), 40L, 40L);
    }

    private boolean disabled = false;
    public ConcurrentHashMap<String, Block> chairs = new ConcurrentHashMap<String, Block>();

    public void addChair (Player player, Block block) {
        if (disabled) return;
        try {
            // TODO deck chairs. Packet17EntityLocationAction packet = new Packet17EntityLocationAction(((CraftPlayer)player).getHandle(), 0,
            // block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());

            PacketContainer entitymeta = plugin.getProtocolManager().createPacket(40);
            entitymeta.getSpecificModifier(int.class).write(0, player.getEntityId());
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(0, (byte) 4);
            entitymeta.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
            // Packet40EntityMetadata packet = new Packet40EntityMetadata(player.getEntityId(), new ChairWatcher((byte) 4), false);
            for (Player play : plugin.getServer().getOnlinePlayers()) {
                if (play.getWorld().equals(player.getPlayer().getWorld())) {
                    try {
                        plugin.getProtocolManager().sendServerPacket(play, entitymeta);
                    } catch (InvocationTargetException e) {
                        Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
                    }
                    // ((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
                }
            }
        } catch (Error e) {
            Bukkit.getLogger().severe("Chairs do not work in this version of Minecraft!");
            disabled = true;
            return;
        }
        if (chairs.containsKey(player.getName())) return;
        plugin.wrap(player).print(ChatColor.YELLOW + "You are now sitting.");
        chairs.put(player.getName(), block);
    }

    public void removeChair (Player player) {
        if (disabled) return;
        PacketContainer entitymeta = plugin.getProtocolManager().createPacket(40);
        entitymeta.getSpecificModifier(int.class).write(0, player.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(0, (byte) 0);
        entitymeta.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        // Packet40EntityMetadata packet = new Packet40EntityMetadata(player.getEntityId(), new ChairWatcher((byte) 0), false);
        for (Player play : plugin.getServer().getOnlinePlayers()) {
            if (play.getWorld().equals(player.getPlayer().getWorld())) {
                try {
                    plugin.getProtocolManager().sendServerPacket(play, entitymeta);
                } catch (InvocationTargetException e) {
                    Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
                }
                // ((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
            }
        }
        plugin.wrap(player).print(ChatColor.YELLOW + "You are no longer sitting.");
        chairs.remove(player.getName());
    }

    public Block getChair (Player player) {
        if (disabled) return null;
        return chairs.get(player.getName());
    }

    public boolean hasChair (Player player) {

        return !disabled && chairs.containsKey(player.getName());
    }

    public boolean hasChair (Block player) {

        return !disabled && chairs.containsValue(player);
    }

    private MechanismsPlugin plugin;

    @EventHandler
    public void onPlayerQuit (PlayerQuitEvent event) {

        if (hasChair(event.getPlayer())) chairs.remove(event.getPlayer().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak (BlockBreakEvent event) {

        if (!plugin.getLocalConfiguration().chairSettings.enable) return;
        if (hasChair(event.getBlock())) {
            event.setCancelled(true);
            plugin.wrap(event.getPlayer()).print("This seat is in use!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick (PlayerInteractEvent event) {

        if (!plugin.getLocalConfiguration().chairSettings.enable) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || !plugin.getLocalConfiguration().chairSettings.canUseBlock(event.getClickedBlock().getTypeId()))
            return;

        BukkitPlayer player = new BukkitPlayer(plugin, event.getPlayer());

        // Now everything looks good, continue;
        if (player.getPlayer().getItemInHand() == null || !player.getPlayer().getItemInHand().getType().isBlock()
                || player.getPlayer().getItemInHand().getTypeId() == 0) {
            if (plugin.getLocalConfiguration().chairSettings.requireSneak != player.getPlayer().isSneaking()) return;
            if (!player.hasPermission("craftbook.mech.chair.use")) {
                player.printError("mech.use-permission");
                return;
            }
            if (hasChair(player.getPlayer())) { // Stand
                removeChair(player.getPlayer());
            } else { // Sit
                if (hasChair(event.getClickedBlock())) {
                    player.print("This seat is already occupied.");
                    return;
                }
                player.getPlayer().teleport(event.getClickedBlock().getLocation().add(0.5, 0, 0.5)); // Teleport to the seat
                addChair(player.getPlayer(), event.getClickedBlock());
            }
        }
    }

    public class ChairChecker implements Runnable {

        @Override
        public void run () {

            for (String pl : chairs.keySet()) {
                Player p = Bukkit.getPlayer(pl);
                if (p == null) continue;
                if (!plugin.getLocalConfiguration().chairSettings.canUseBlock(getChair(p).getTypeId())
                        || !p.getWorld().equals(getChair(p).getWorld()) || p.getLocation().distanceSquared(getChair(p).getLocation()) > 1) removeChair(p); // Remove
                                                                                                                                                           // it.
                                                                                                                                                           // It's
                                                                                                                                                           // unused.
                else {
                    addChair(p, getChair(p)); // For any new players.

                    if (plugin.getLocalConfiguration().chairSettings.healthRegen && p.getHealth() < 20) p.setHealth(p.getHealth() + 1);
                    if (p.getExhaustion() > -20f) p.setExhaustion(p.getExhaustion() - 0.1f);
                }
            }
        }
    }

    /*
     * public static class ChairWatcher extends DataWatcher {
     * 
     * private byte metadata;
     * 
     * public ChairWatcher(byte metadata) {
     * 
     * this.metadata = metadata; }
     * 
     * @Override public ArrayList<WatchableObject> b() {
     * 
     * ArrayList<WatchableObject> list = new ArrayList<WatchableObject>(); WatchableObject wo = new WatchableObject(0, 0, metadata); list.add(wo);
     * return list; } }
     */
}