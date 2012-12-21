package com.sk89q.craftbook.mech;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.BaseBukkitPlugin;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.blocks.BlockID;

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

    private HashMap<Location, Integer> tasks = new HashMap<Location, Integer>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSnowballHit(ProjectileHitEvent event) {

        if (!plugin.getLocalConfiguration().snowSettings.placeSnow) return;
        if(event.getEntity() instanceof Snowball) {
            if(event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {
                LocalPlayer player = plugin.wrap((Player) event.getEntity().getShooter());
                if (!player.hasPermission("craftbook.mech.snow.place")) return;
            }
            Block block = event.getEntity().getLocation().getBlock();
            incrementData(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!plugin.getLocalConfiguration().snowSettings.trample) return;

        if(!event.getFrom().getWorld().getName().equalsIgnoreCase(event.getTo().getWorld().getName()))
            return;
        LocalPlayer player = plugin.wrap(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.snow.trample")) return;

        if (plugin.getLocalConfiguration().snowSettings.jumpTrample && event.getPlayer().getVelocity().getY() >= 0D)
            return;
        if (BaseBukkitPlugin.random.nextInt(30) == 0) {
            Block b = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation());
            if (b.getTypeId() == 78) {
                if (!plugin.canBuildInArea(event.getPlayer().getLocation(), event.getPlayer()))
                    return;
                lowerData(b);
            }

            b = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation().subtract(0, 1, 0));
            if (b.getTypeId() == 78) {
                if (!plugin.canBuildInArea(event.getPlayer().getLocation(), event.getPlayer()))
                    return;
                lowerData(b);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockForm(final BlockFormEvent event) {

        if (!plugin.getLocalConfiguration().snowSettings.enable) return;
        if (event.getNewState().getTypeId() == BlockID.SNOW) {
            Block block = event.getBlock();
            pile(block);
        }
    }

    public void pile(Block block) {

        if (block.getTypeId() != BlockID.SNOW_BLOCK && block.getTypeId() != BlockID.SNOW) {
            Location blockLoc = block.getLocation().subtract(0, 1, 0);
            if (block.getWorld().getBlockAt(blockLoc).getTypeId() == BlockID.SNOW_BLOCK
                    && !plugin.getLocalConfiguration().snowSettings.piling
                    || block.getWorld().getBlockAt(blockLoc).getTypeId() == BlockID.SNOW)
                return;
            schedule(block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(final BlockPhysicsEvent event) {

        if (!plugin.getLocalConfiguration().snowSettings.enable) return;
        if (event.getBlock().getTypeId() == BlockID.SNOW) {
            Block block = event.getBlock();

            if(plugin.getLocalConfiguration().snowSettings.realistic)
                disperse(event.getBlock(), true);

            if(event.getBlock().getWorld().hasStorm()) {

                pile(block);
            }
        }
    }

    public void schedule(Location loc) {
        if(tasks.containsKey(loc))
            return;
        long delay = BaseBukkitPlugin.random.nextInt(60) + 40; //100 is max possible
        int taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new MakeSnow(loc), delay * 20L, delay * 20L);
        if (taskID == -1)
            plugin.getLogger().log(Level.SEVERE, "[CraftBookMechanisms] Snow Mechanic failed to schedule!");
        else
            tasks.put(loc, taskID);
    }

    public class MakeSnow implements Runnable {

        final Location event;

        public MakeSnow(Location event) {

            this.event = event;
        }

        @Override
        public void run() {

            if(!tasks.containsKey(event))
                return;
            if (event.getWorld().hasStorm()) {
                if (event.getBlock().getData() > (byte) 7) return;
                if (event.subtract(0, 1, 0).getBlock().getTypeId() == 0) return;

                event.add(0, 1, 0);
                if (!(event.getBlock().getTypeId() == 78) && !(event.getBlock().getTypeId() == 80)) return;
                incrementData(event.getBlock());
            }
            else {
                Integer taskID = tasks.get(event);
                if(taskID == null)
                    return;
                Bukkit.getScheduler().cancelTask(taskID);
                tasks.remove(event);
            }
        }
    }

    public void lowerData(Block block) {

        if(block.getRelative(0, -1, 0).getTypeId() == BlockID.WATER || block.getRelative(0, -1, 0).getTypeId() == BlockID.STATIONARY_WATER) {
            block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
        }

        byte newData = (byte) (block.getData() - 1);
        if (newData < 1) {
            block.setTypeId(0, false);
            newData = 0;
        }
        if (block.getTypeId() == BlockID.SNOW_BLOCK) {
            block.setTypeId(BlockID.SNOW, false);
            newData = (byte) 7;
        }
        if (newData > (byte) 7) {
            newData = (byte) 0;
        }
        setBlockDataWithNotify(block, newData);
    }

    public boolean disperse(Block block, boolean remove) {

        if(block.getRelative(0, -1, 0).getTypeId() == BlockID.WATER || block.getRelative(0, -1, 0).getTypeId() == BlockID.STATIONARY_WATER) {
            block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
        }

        if(isValidBlock(block.getRelative(0, -1, 0).getTypeId()) && block.getRelative(0, -1, 0).getData() < 0x7) {
            if(block.getRelative(0, -1, 0).getData() < block.getData() || block.getRelative(0, -1, 0).getTypeId() != BlockID.SNOW) {
                incrementData(block.getRelative(0, -1, 0));
                if(remove)
                    lowerData(block);
                return true;
            }
        }

        if(isValidBlock(block.getRelative(1, 0, 0).getTypeId()) && block.getRelative(1, 0, 0).getData() < 0x7) {
            if(block.getRelative(1, 0, 0).getData() < block.getData() || block.getRelative(1, 0, 0).getTypeId() != BlockID.SNOW) {
                incrementData(block.getRelative(1, 0, 0));
                if(remove)
                    lowerData(block);
                return true;
            }
        }

        if(isValidBlock(block.getRelative(-1, 0, 0).getTypeId()) && block.getRelative(-1, 0, 0).getData() < 0x7) {
            if(block.getRelative(-1, 0, 0).getData() < block.getData() || block.getRelative(-1, 0, 0).getTypeId() != BlockID.SNOW) {
                incrementData(block.getRelative(-1, 0, 0));
                if(remove)
                    lowerData(block);
                return true;
            }
        }

        if(isValidBlock(block.getRelative(0, 0, 1).getTypeId()) && block.getRelative(0, 0, 1).getData() < 0x7) {
            if(block.getRelative(0, 0, 1).getData() < block.getData() || block.getRelative(0, 0, 1).getTypeId() != BlockID.SNOW) {
                incrementData(block.getRelative(0, 0, 1));
                if(remove)
                    lowerData(block);
                return true;
            }
        }

        if(isValidBlock(block.getRelative(0, 0, -1).getTypeId()) && block.getRelative(0, 0, -1).getData() < 0x7) {
            if(block.getRelative(0, 0, -1).getData() < block.getData() || block.getRelative(0, 0, -1).getTypeId() != BlockID.SNOW) {
                incrementData(block.getRelative(0, 0, -1));
                if(remove)
                    lowerData(block);
                return true;
            }
        }

        return false;
    }

    public void incrementData(Block block) {

        if(block.getRelative(0, -1, 0).getTypeId() == BlockID.WATER || block.getRelative(0, -1, 0).getTypeId() == BlockID.STATIONARY_WATER) {
            block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
        }

        if(!isValidBlock(block.getTypeId()) && isValidBlock(block.getRelative(0, 1, 0).getTypeId())) {
            incrementData(block.getRelative(0, 1, 0));
            return;
        }
        else if(!isValidBlock(block.getTypeId()))
            return;

        if(canPassThrough(block.getTypeId()) && canPassThrough(block.getRelative(0, -1, 0).getTypeId())) {
            incrementData(block.getRelative(0, -1, 0));
            return;
        }

        if(canPassThrough(block.getTypeId()) && block.getRelative(0, -1, 0).getTypeId() == BlockID.SNOW && block.getRelative(0, -1, 0).getData() < 0x7) {
            incrementData(block.getRelative(0, -1, 0));
            return;
        }

        if(block.getTypeId() == BlockID.SNOW_BLOCK) {
            incrementData(block.getRelative(0, 1, 0));
            return;
        }

        if(plugin.getLocalConfiguration().snowSettings.realistic) {
            if(block.getTypeId() == BlockID.SNOW && disperse(block, false))
                return;
        }

        byte newData = 0;
        if(block.getTypeId() != BlockID.SNOW && canPassThrough(block.getTypeId()))
            block.setTypeId(BlockID.SNOW, false);
        else
            newData = (byte) (block.getData() + 1);
        if (newData > (byte) 7 && plugin.getLocalConfiguration().snowSettings.piling) {
            block.setTypeId(BlockID.SNOW_BLOCK, false);
            newData = (byte) 0;
        } else if (newData > 7) {
            newData = 7;
        }
        setBlockDataWithNotify(block, newData);
    }

    public void setBlockDataWithNotify(Block block, byte data) {

        block.setTypeIdAndData(block.getTypeId(), data, false);
        for (Player p : block.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(block.getLocation()) < plugin.getServer().getViewDistance() * 16 * plugin.getServer().getViewDistance() * 16) {
                p.sendBlockChange(block.getLocation(), block.getTypeId(), data);
            }
        }
    }

    public boolean canPassThrough(int id) {

        return id == BlockID.DEAD_BUSH || id == BlockID.AIR || id == BlockID.LONG_GRASS || id == BlockID.FIRE;
    }

    public boolean isSnowBlock(int id) {

        return id == BlockID.SNOW_BLOCK || id == BlockID.SNOW;
    }

    public boolean isValidBlock(int id) {

        return canPassThrough(id) || isSnowBlock(id);
    }
}