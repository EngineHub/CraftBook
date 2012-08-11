package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Random;
import java.util.logging.Level;

/**
 * Snow fall mechanism. Builds up/tramples snow
 *
 * @author Me4502
 */
public class Snow implements Listener {

    final MechanismsPlugin plugin;

    public Snow(MechanismsPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (!plugin.getLocalConfiguration().snowSettings.placeSnow) return;

        LocalPlayer player = plugin.wrap(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.snow.place")) return;
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        try {
            if (event.getPlayer().getItemInHand().getTypeId() == ItemID.SNOWBALL
                    && event.getClickedBlock().getTypeId() == 78) {
                if (event.getClickedBlock().getData() < (byte) 7) {
                    incrementData(event.getClickedBlock());
                }
            } else if (event.getPlayer().getItemInHand().getTypeId() == ItemID.SNOWBALL
                    && event.getPlayer().getWorld().getBlockAt(event.getClickedBlock().getLocation().add(0, 1, 0))
                    .getTypeId() == 0) {
                event.getPlayer().getWorld().getBlockAt(event.getClickedBlock().getLocation().add(0, 1, 0))
                        .setTypeId(78);
                incrementData(event.getPlayer().getWorld().getBlockAt(event.getClickedBlock().getLocation()
                        .add(0, 1, 0)));
            }
        } catch (Exception ignored) {
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!plugin.getLocalConfiguration().snowSettings.trample) return;

        LocalPlayer player = plugin.wrap(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.snow.trample")) return;

        Random random = new Random();
        if (plugin.getLocalConfiguration().snowSettings.jumpTrample && event.getPlayer().getVelocity().getY() >= 0D)
            return;
        if (random.nextInt(10) == 6) {
            Block b = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation());
            if (b.getTypeId() == 78 || b.getTypeId() == 80) {
                lowerData(b);
            }

            b = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation().subtract(0, 1, 0));
            if (b.getTypeId() == 78 || b.getTypeId() == 80) {
                lowerData(b);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(final BlockFormEvent event) {

        if (!plugin.getLocalConfiguration().snowSettings.enable) return;
        if (event.getNewState().getTypeId() == BlockID.SNOW) {
            Block block = event.getBlock();

            if (block.getTypeId() != BlockID.SNOW_BLOCK && block.getTypeId() != BlockID.SNOW) {
                Location blockLoc = block.getLocation().subtract(0, 1, 0);
                if (block.getWorld().getBlockAt(blockLoc).getTypeId() == BlockID.SNOW_BLOCK
                        && !plugin.getLocalConfiguration().snowSettings.piling
                        || block.getWorld().getBlockAt(blockLoc).getTypeId() == BlockID.SNOW)
                    return;
                Random random = new Random();
                long delay = random.nextInt(100) + 60;
                if (plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                        new makeSnow(block.getLocation()), delay * 20L) == -1)
                    plugin.getLogger().log(Level.SEVERE, "[CraftBookMechanisms] Snow Mechanic failed to schedule!");
            }
        }
    }

    public class makeSnow implements Runnable {

        final Location event;

        public makeSnow(Location event) {

            this.event = event;
        }

        @Override
        public void run() {

            if (event.getWorld().hasStorm()) {
                if (event.getBlock().getData() > (byte) 7) return;
                if (event.subtract(0, 1, 0).getBlock().getTypeId() == 0) return;

                event.add(0, 1, 0);
                if (!(event.getBlock().getTypeId() == 78) && !(event.getBlock().getTypeId() == 80)) return;
                incrementData(event.getBlock());
                Random random = new Random();
                long delay = random.nextInt(100) + 60;
                if (plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new makeSnow(event),
                        delay * 20L) == -1)
                    plugin.getLogger().log(Level.SEVERE, "[CraftBookMechanisms] Snow Mechanic failed to schedule!");
            } else {
                Random random = new Random();
                long delay = random.nextInt(100) + 600;
                if (plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new makeSnow(event),
                        delay * 20L) == -1)
                    plugin.getLogger().log(Level.SEVERE, "[CraftBookMechanisms] Snow Mechanic failed to schedule!");
            }
        }
    }

    public void lowerData(Block block) {

        byte newData = (byte) (block.getData() - 1);
        if (newData < 1) {
            block.setTypeId(0);
            newData = 0;
        }
        if (block.getTypeId() == BlockID.SNOW_BLOCK) {
            block.setTypeId(BlockID.SNOW);
            newData = (byte) 7;
        }
        if (newData > (byte) 7)
            newData = (byte) 7;
        setBlockDataWithNotify(block, newData);
    }

    public void incrementData(Block block) {

        byte newData = (byte) (block.getData() + 1);
        if (newData > (byte) 7 && plugin.getLocalConfiguration().snowSettings.piling) {
            block.setTypeId(BlockID.SNOW_BLOCK);
            newData = (byte) 0;
        }
        setBlockDataWithNotify(block, newData);
    }

    public void setBlockDataWithNotify(Block block, byte data) {

        block.setData(data);
        for (Player p : block.getWorld().getPlayers()) {
            if (p.getLocation().distance(block.getLocation()) < plugin.getServer().getViewDistance() * 16)
                p.sendBlockChange(block.getLocation(), block.getTypeId(), data);
        }
        // This notifies a block update, there is a bug in bukkit that doesn't
        // notify the client when Snow's data gets changed. Maybe i should submit a thingy
    }
}