package com.sk89q.craftbook.mech;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * @author Me4502
 */
public class Chair implements Listener {

    public Chair() {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new ChairChecker(), 40L, 40L);
    }

    private boolean disabled = false;
    public ConcurrentHashMap<String, Block> chairs = new ConcurrentHashMap<String, Block>();

    public void addChair(Player player, Block block) {

        if (disabled) return;
        try {
            PacketContainer entitymeta = ProtocolLibrary.getProtocolManager().createPacket(40);
            entitymeta.getSpecificModifier(int.class).write(0, player.getEntityId());
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(0, (byte) 4);
            entitymeta.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
            for (Player play : plugin.getServer().getOnlinePlayers()) {
                if (play.getWorld().equals(player.getPlayer().getWorld())) {
                    try {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(play, entitymeta);
                    } catch (InvocationTargetException e) {
                        BukkitUtil.printStacktrace(e);
                    }
                }
            }
        } catch (Error e) {
            CraftBookPlugin.logger().warning("Chairs do not work without ProtocolLib!");
            disabled = true;
            return;
        }
        if (chairs.containsKey(player.getName())) return;
        plugin.wrapPlayer(player).print(ChatColor.YELLOW + "You are now sitting.");
        chairs.put(player.getName(), block);
    }

    public void removeChair(Player player) {

        if (disabled) return;
        PacketContainer entitymeta = ProtocolLibrary.getProtocolManager().createPacket(40);
        entitymeta.getSpecificModifier(int.class).write(0, player.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(0, (byte) 0);
        entitymeta.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        for (Player play : plugin.getServer().getOnlinePlayers()) {
            if (play.getWorld().equals(player.getPlayer().getWorld())) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(play, entitymeta);
                } catch (InvocationTargetException e) {
                    BukkitUtil.printStacktrace(e);
                }
            }
        }
        plugin.wrapPlayer(player).print(ChatColor.YELLOW + "You are no longer sitting.");
        chairs.remove(player.getName());
    }

    public Block getChair(Player player) {

        if (disabled) return null;
        return chairs.get(player.getName());
    }

    public boolean hasChair(Player player) {

        return !disabled && chairs.containsKey(player.getName());
    }

    public boolean hasChair(Block player) {

        return !disabled && chairs.containsValue(player);
    }

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!plugin.getConfiguration().chairEnabled) return;
        if (hasChair(event.getBlock())) {
            event.setCancelled(true);
            plugin.wrapPlayer(event.getPlayer()).print("This seat is in use!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getConfiguration().chairEnabled) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || !plugin.getConfiguration().chairBlocks.contains(event
                .getClickedBlock().getTypeId()))
            return;

        BukkitPlayer player = new BukkitPlayer(plugin, event.getPlayer());

        // Now everything looks good, continue;
        if (player.getPlayer().getItemInHand() == null || !player.getPlayer().getItemInHand().getType().isBlock()
                || player.getPlayer().getItemInHand().getTypeId() == 0) {
            if (plugin.getConfiguration().chairSneak != player.getPlayer().isSneaking()) return;
            if (!player.hasPermission("craftbook.mech.chair.use")) {
                player.printError("mech.use-permission");
                return;
            }
            if (hasChair(player.getPlayer())) { // Stand
                removeChair(player.getPlayer());
                event.getPlayer().teleport(event.getClickedBlock().getLocation().add(0.5, 1.5, 0.5));
            } else { // Sit
                if (hasChair(event.getClickedBlock())) {
                    player.print("This seat is already occupied.");
                    return;
                }
                if(BlockType.canPassThrough(event.getClickedBlock().getRelative(0, -1, 0).getTypeId())) {

                    player.printError("This chair has nothing below it!");
                    return;
                }
                player.getPlayer().teleport(event.getClickedBlock().getLocation().add(0.5,0,0.5)); // Teleport to the seat
                addChair(player.getPlayer(), event.getClickedBlock());
            }
        }
    }

    public class ChairChecker implements Runnable {

        @Override
        public void run() {

            for (String pl : chairs.keySet()) {
                Player p = Bukkit.getPlayer(pl);
                if (p == null) {
                    chairs.remove(pl);
                    continue;
                }

                if (!plugin.getConfiguration().chairBlocks.contains(getChair(p).getTypeId()) || !p.getWorld().equals(getChair(p).getWorld()) || LocationUtil.getDistanceSquared(p.getLocation(), getChair(p).getLocation()) > 1.5)
                    removeChair(p);
                else {
                    addChair(p, getChair(p)); // For any new players.

                    if (plugin.getConfiguration().chairHealth && p.getHealth() < 20)
                        p.setHealth(p.getHealth() + 1);
                    if (p.getExhaustion() > -20f) p.setExhaustion(p.getExhaustion() - 0.1f);
                }
            }
        }
    }
}