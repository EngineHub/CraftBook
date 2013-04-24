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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * Snow fall mechanism. Builds up/tramples snow
 *
 * @author Me4502
 */
public class Snow implements Listener {

    public Snow() {

    }

    private HashMap<Location, Integer> tasks = new HashMap<Location, Integer>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSnowballHit(ProjectileHitEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowPlace) return;
        if (event.getEntity() instanceof Snowball) {

            Block block = event.getEntity().getLocation().getBlock();
            if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {

                if (!CraftBookPlugin.inst().canBuild((Player)event.getEntity().getShooter(), block.getLocation())) {
                    return;
                }

                LocalPlayer player = CraftBookPlugin.inst().wrapPlayer((Player) event.getEntity().getShooter());
                if (!player.hasPermission("craftbook.mech.snow.place")) return;
            }
            incrementData(block, 0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if(event.getBlock().getTypeId() == BlockID.SNOW && ItemUtil.isStackValid(event.getPlayer().getItemInHand())) {

            if(event.getPlayer().getItemInHand().getTypeId() == ItemID.WOOD_SHOVEL
                    || event.getPlayer().getItemInHand().getTypeId() == ItemID.STONE_SHOVEL
                    || event.getPlayer().getItemInHand().getTypeId() == ItemID.IRON_SHOVEL
                    || event.getPlayer().getItemInHand().getTypeId() == ItemID.GOLD_SHOVEL
                    || event.getPlayer().getItemInHand().getTypeId() == ItemID.DIAMOND_SHOVEL) {
                event.setCancelled(true);
                int amount = event.getBlock().getData()+1;
                event.getBlock().setTypeId(BlockID.AIR);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(ItemID.SNOWBALL, amount));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowTrample) return;

        if (!event.getFrom().getWorld().getName().equalsIgnoreCase(event.getTo().getWorld().getName())) return;
        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.snow.trample")) return;

        if(CraftBookPlugin.inst().getConfiguration().snowSlowdown && event.getTo().getBlock().getTypeId() == BlockID.SNOW) {

            if(event.getTo().getBlock().getData() > 0x5)
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2), true);
            else if(event.getTo().getBlock().getData() > 0x2)
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1), true);
        }

        if (CraftBookPlugin.inst().getConfiguration().snowJumpTrample && event.getPlayer().getVelocity().getY() >= 0D)
            return;
        if (CraftBookPlugin.inst().getRandom().nextInt(30) == 0) {
            Block b = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation());
            if (b.getTypeId() == 78) {
                if (!CraftBookPlugin.inst().canBuild(event.getPlayer(), event.getPlayer().getLocation())) return;
                lowerData(b);
            }

            b = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation().subtract(0, 1, 0));
            if (b.getTypeId() == 78) {
                if (!CraftBookPlugin.inst().canBuild(event.getPlayer(), event.getPlayer().getLocation())) return;
                lowerData(b);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockForm(final BlockFormEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowPiling) return;
        if (event.getNewState().getTypeId() == BlockID.SNOW) {
            Block block = event.getBlock();
            pile(block);
        }
    }

    public void pile(Block block) {

        if (block.getTypeId() != BlockID.SNOW_BLOCK && block.getTypeId() != BlockID.SNOW) {
            Location blockLoc = block.getLocation().subtract(0, 1, 0);
            if (block.getWorld().getBlockAt(blockLoc).getTypeId() == BlockID.SNOW_BLOCK && !CraftBookPlugin.inst()
                    .getConfiguration().snowHighPiles
                    || block.getWorld().getBlockAt(blockLoc).getTypeId() == BlockID.SNOW) return;
            schedule(block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(final BlockPhysicsEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowPiling) return;
        if (event.getBlock().getTypeId() == BlockID.SNOW) {
            Block block = event.getBlock();

            if (CraftBookPlugin.inst().getConfiguration().snowRealistic && event.getBlock().getData() > 0x0) disperse(event.getBlock(), true, 0);

            if (event.getBlock().getWorld().hasStorm()) pile(block);
        }
    }

    public void schedule(Location loc) {

        if (tasks.containsKey(loc)) return;
        long delay = CraftBookPlugin.inst().getRandom().nextInt(60) + 40; // 100 is max possible
        int taskID = CraftBookPlugin.inst().getServer().getScheduler().scheduleSyncRepeatingTask(CraftBookPlugin.inst
                (), new MakeSnow(loc),
                delay * 20L, delay * 20L);
        if (taskID == -1)
            Bukkit.getLogger().log(Level.SEVERE, "[CraftBookMechanisms] Snow Mechanic failed to schedule!");
        else tasks.put(loc, taskID);
    }

    public class MakeSnow implements Runnable {

        final Location event;

        public MakeSnow(Location event) {

            this.event = event;
        }

        @Override
        public void run() {

            if (!tasks.containsKey(event)) return;
            if (event.getWorld().hasStorm()) {
                if (event.getBlock().getData() > (byte) 7) return;
                if (event.subtract(0, 1, 0).getBlock().getTypeId() == 0) return;

                event.add(0, 1, 0);
                if (!(event.getBlock().getTypeId() == 78) && !(event.getBlock().getTypeId() == 80)) return;
                incrementData(event.getBlock(), 0);
            } else {
                Integer taskID = tasks.get(event);
                if (taskID == null) return;
                Bukkit.getScheduler().cancelTask(taskID);
                tasks.remove(event);
            }
        }
    }

    public void lowerData(Block block) {

        if (block.getRelative(0, -1, 0).getTypeId() == BlockID.WATER || block.getRelative(0, -1,
                0).getTypeId() == BlockID.STATIONARY_WATER) {
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

    public boolean disperse(Block block, boolean remove, int depth) {

        if (block.getRelative(0, -1, 0).getTypeId() == BlockID.WATER || block.getRelative(0, -1,
                0).getTypeId() == BlockID.STATIONARY_WATER) {
            block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
        }

        if (isValidBlock(block.getRelative(0, -1, 0).getTypeId()) && block.getRelative(0, -1, 0).getData() < 0x7) {
            if (block.getRelative(0, -1, 0).getData() < block.getData() || block.getRelative(0, -1,
                    0).getTypeId() != BlockID.SNOW) {
                incrementData(block.getRelative(0, -1, 0), depth+1);
                if (remove) lowerData(block);
                return true;
            }
        }

        if (isValidBlock(block.getRelative(1, 0, 0).getTypeId()) && block.getRelative(1, 0, 0).getData() < 0x7) {
            if (block.getRelative(1, 0, 0).getData() < block.getData() || block.getRelative(1, 0,0).getTypeId() != BlockID.SNOW) {
                incrementData(block.getRelative(1, 0, 0), depth+1);
                if (remove) lowerData(block);
                return true;
            }
        }

        if (isValidBlock(block.getRelative(-1, 0, 0).getTypeId()) && block.getRelative(-1, 0, 0).getData() < 0x7) {
            if (block.getRelative(-1, 0, 0).getData() < block.getData() || block.getRelative(-1, 0,
                    0).getTypeId() != BlockID.SNOW) {
                incrementData(block.getRelative(-1, 0, 0), depth+1);
                if (remove) lowerData(block);
                return true;
            }
        }

        if (isValidBlock(block.getRelative(0, 0, 1).getTypeId()) && block.getRelative(0, 0, 1).getData() < 0x7) {
            if (block.getRelative(0, 0, 1).getData() < block.getData() || block.getRelative(0, 0,
                    1).getTypeId() != BlockID.SNOW) {
                incrementData(block.getRelative(0, 0, 1), depth+1);
                if (remove) lowerData(block);
                return true;
            }
        }

        if (isValidBlock(block.getRelative(0, 0, -1).getTypeId()) && block.getRelative(0, 0, -1).getData() < 0x7) {
            if (block.getRelative(0, 0, -1).getData() < block.getData() || block.getRelative(0, 0,
                    -1).getTypeId() != BlockID.SNOW) {
                incrementData(block.getRelative(0, 0, -1), depth+1);
                if (remove) lowerData(block);
                return true;
            }
        }

        return false;
    }

    public void incrementData(final Block block, final int depth) {

        if(block.getLocation().getY() == 0)
            return;

        if (block.getRelative(0, -1, 0).getTypeId() == BlockID.WATER || block.getRelative(0, -1, 0).getTypeId() == BlockID.STATIONARY_WATER) {
            block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
        }

        if (!isValidBlock(block.getTypeId()) && isValidBlock(block.getRelative(0, 1, 0).getTypeId())) {
            incrementData(block.getRelative(0, 1, 0), depth+1);
            return;
        } else if (!isValidBlock(block.getTypeId())) return;

        if (depth < 1 && canPassThrough(block.getTypeId()) && canPassThrough(block.getRelative(0, -1, 0).getTypeId())) {
            incrementData(block.getRelative(0, -1, 0), depth+1);
            return;
        } else if (depth >= 1 && canPassThrough(block.getTypeId()) && canPassThrough(block.getRelative(0, -1, 0).getTypeId())) {

            final boolean remove;
            if(block.getTypeId() != BlockID.SNOW && canPassThrough(block.getTypeId())) {
                block.setTypeId(BlockID.SNOW, false);
                remove = true;
            } else {
                remove = false;
            }
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run () {
                    if(remove)
                        block.setTypeId(0, false);
                    incrementData(block.getRelative(0, -1, 0), depth+1);
                }
            }, 5L);
            return;
        }

        if (canPassThrough(block.getTypeId()) && block.getRelative(0, -1, 0).getTypeId() == BlockID.SNOW && block.getRelative(0, -1, 0).getData() < 0x7) {
            incrementData(block.getRelative(0, -1, 0), depth+1);
            return;
        }

        if (block.getTypeId() == BlockID.SNOW_BLOCK && isValidBlock(block.getRelative(0, 1, 0).getTypeId())) {
            incrementData(block.getRelative(0, 1, 0), depth+1);
            return;
        }

        if (CraftBookPlugin.inst().getConfiguration().snowRealistic) {
            if (block.getTypeId() == BlockID.SNOW && disperse(block, false, depth+1)) return;
        }

        byte newData = 0;
        if (block.getTypeId() != BlockID.SNOW && canPassThrough(block.getTypeId()))
            block.setTypeId(BlockID.SNOW, false);
        else newData = (byte) (block.getData() + 1);
        if (newData > (byte) 7 && CraftBookPlugin.inst().getConfiguration().snowHighPiles) {
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
            if (LocationUtil.getDistanceSquared(p.getLocation(), block.getLocation()) < CraftBookPlugin.inst().getServer().getViewDistance() * 16 * CraftBookPlugin.inst().getServer().getViewDistance() * 16)
                p.sendBlockChange(block.getLocation(), block.getTypeId(), data);
        }
    }

    public boolean canPassThrough(int id) {

        return id == BlockID.AIR || CraftBookPlugin.inst().getConfiguration().snowRealisticReplacables.contains(id);
    }

    public boolean isSnowBlock(int id) {

        return id == BlockID.SNOW;// || id == BlockID.SNOW_BLOCK;
    }

    public boolean isValidBlock(int id) {

        return canPassThrough(id) || isSnowBlock(id);
    }
}