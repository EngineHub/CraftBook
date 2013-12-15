package com.sk89q.craftbook.mech;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Snow fall mechanism. Builds up/tramples snow
 *
 * @author Me4502
 */
public class Snow extends AbstractCraftBookMechanic {

    private Map<Location, BukkitTask> tasks = new HashMap<Location, BukkitTask>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSnowballHit(ProjectileHitEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowPlace) return;
        if (event.getEntity() instanceof Snowball) {

            Block block = event.getEntity().getLocation().getBlock();
            if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {

                if (CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild((Player)event.getEntity().getShooter(), block.getLocation(), true)) {
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

        if(EventUtil.shouldIgnoreEvent(event))
            return;
        if(event.getBlock().getType() == Material.SNOW && ItemUtil.isStackValid(event.getPlayer().getItemInHand())) {

            if(event.getPlayer().getItemInHand().getType() == Material.WOOD_SPADE
                    || event.getPlayer().getItemInHand().getType() == Material.STONE_SPADE
                    || event.getPlayer().getItemInHand().getType() == Material.IRON_SPADE
                    || event.getPlayer().getItemInHand().getType() == Material.GOLD_SPADE
                    || event.getPlayer().getItemInHand().getType() == Material.DIAMOND_SPADE) {
                event.setCancelled(true);
                int amount = event.getBlock().getData()+1;
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.SNOW_BALL, amount));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowTrample) return;

        if (!event.getFrom().getWorld().getName().equalsIgnoreCase(event.getTo().getWorld().getName())) return;
        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.snow.trample")) return;

        if(CraftBookPlugin.inst().getConfiguration().snowSlowdown && event.getTo().getBlock().getType() == Material.SNOW) {

            if(event.getTo().getBlock().getData() > 0x5)
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2), true);
            else if(event.getTo().getBlock().getData() > 0x2)
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1), true);
        }

        if (CraftBookPlugin.inst().getConfiguration().snowJumpTrample && event.getPlayer().getVelocity().getY() >= 0D)
            return;
        if (CraftBookPlugin.inst().getRandom().nextInt(30) == 0) {
            Block b = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation());
            if (b.getType() == Material.SNOW) {
                if (CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(event.getPlayer(), event.getPlayer().getLocation(), false)) return;
                if(b.getData() == 0x0 && CraftBookPlugin.inst().getConfiguration().snowPartialTrample) return;
                lowerData(b);
            }

            b = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation().subtract(0, 1, 0));
            if (b.getType() == Material.SNOW) {
                if (CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(event.getPlayer(), event.getPlayer().getLocation(), false)) return;
                if(b.getData() == 0x0 && CraftBookPlugin.inst().getConfiguration().snowPartialTrample) return;
                lowerData(b);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockForm(final BlockFormEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowPiling) return;
        if (event.getNewState().getType() == Material.SNOW) {
            Block block = event.getBlock();
            pile(block);
        }
    }

    public void pile(Block block) {

        if (block.getType() != Material.SNOW_BLOCK && block.getType() != Material.SNOW) {
            Location blockLoc = block.getLocation().subtract(0, 1, 0);
            if (block.getWorld().getBlockAt(blockLoc).getType() == Material.SNOW_BLOCK && !CraftBookPlugin.inst().getConfiguration().snowHighPiles || block.getWorld().getBlockAt(blockLoc).getType() == Material.SNOW)
                return;
            schedule(block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(final BlockPhysicsEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowPiling) return;
        if (event.getBlock().getType() == Material.SNOW) {
            Block block = event.getBlock();

            if (CraftBookPlugin.inst().getConfiguration().snowRealistic && event.getBlock().getData() > 0x0) disperse(event.getBlock(), true, 0);

            if (event.getBlock().getWorld().hasStorm()) pile(block);
        }
    }

    public void schedule(Location loc) {

        if (tasks.containsKey(loc)) return;
        long delay = CraftBookPlugin.inst().getRandom().nextInt(60) + 40; // 100 is max possible
        BukkitTask taskID = CraftBookPlugin.inst().getServer().getScheduler().runTaskTimer(CraftBookPlugin.inst(), new MakeSnow(loc), delay * 20L, delay * 20L);
        if (taskID.getTaskId() == -1)
            CraftBookPlugin.logger().log(Level.SEVERE, "Snow Mechanic failed to schedule!");
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
                if (event.subtract(0, 1, 0).getBlock().getType() == Material.AIR) return;

                event.add(0, 1, 0);
                if (!(event.getBlock().getType() == Material.SNOW) && !(event.getBlock().getType() == Material.SNOW_BLOCK)) return;
                incrementData(event.getBlock(), 0);
            } else {
                BukkitTask taskID = tasks.get(event);
                if (taskID == null) return;
                taskID.cancel();
                tasks.remove(event);
            }
        }
    }

    public void lowerData(Block block) {

        if (block.getRelative(0, -1, 0).getType() == Material.WATER || block.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER) {
            BlockState state = block.getRelative(0, -1, 0).getState();
            state.setType(Material.ICE);
            if(ProtectionUtil.canBlockForm(state.getBlock(), state))
                block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
        }

        byte newData = (byte) (block.getData() - 1);
        if (newData < 1) {
            block.setTypeId(0, false);
            newData = 0;
        }
        if (block.getType() == Material.SNOW_BLOCK) {
            block.setTypeId(BlockID.SNOW, false);
            newData = (byte) 7;
        }
        newData = (byte) Math.min(newData, 7);

        setBlockDataWithNotify(block, newData);
    }

    public boolean disperse(Block block, boolean remove, int depth) {

        if (block.getRelative(0, -1, 0).getType() == Material.WATER || block.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER) {
            BlockState state = block.getRelative(0, -1, 0).getState();
            state.setType(Material.ICE);
            if(ProtectionUtil.canBlockForm(state.getBlock(), state))
                block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
        }

        if (isValidBlock(block.getRelative(0, -1, 0)) && block.getRelative(0, -1, 0).getData() < 0x7) {
            if (block.getRelative(0, -1, 0).getData() < block.getData() || block.getRelative(0, -1, 0).getType() != Material.SNOW) {
                if(!canLandOn(block.getRelative(0, -2, 0)))
                    return false;
                incrementData(block.getRelative(0, -1, 0), depth+1);
                if (remove) lowerData(block);
                return true;
            }
        }

        if (isValidBlock(block.getRelative(1, 0, 0)) && block.getRelative(1, 0, 0).getData() < 0x7) {
            if (block.getRelative(1, 0, 0).getData() < block.getData() || block.getRelative(1, 0,0).getType() != Material.SNOW) {
                if(!canLandOn(block.getRelative(1, -1, 0)))
                    return false;
                incrementData(block.getRelative(1, 0, 0), depth+1);
                if (remove) lowerData(block);
                return true;
            }
        }

        if (isValidBlock(block.getRelative(-1, 0, 0)) && block.getRelative(-1, 0, 0).getData() < 0x7) {
            if (block.getRelative(-1, 0, 0).getData() < block.getData() || block.getRelative(-1, 0, 0).getType() != Material.SNOW) {
                if(!canLandOn(block.getRelative(-1, -1, 0)))
                    return false;
                incrementData(block.getRelative(-1, 0, 0), depth+1);
                if (remove) lowerData(block);
                return true;
            }
        }

        if (isValidBlock(block.getRelative(0, 0, 1)) && block.getRelative(0, 0, 1).getData() < 0x7) {
            if (block.getRelative(0, 0, 1).getData() < block.getData() || block.getRelative(0, 0, 1).getType() != Material.SNOW) {
                if(!canLandOn(block.getRelative(0, -1, 1)))
                    return false;
                incrementData(block.getRelative(0, 0, 1), depth+1);
                if (remove) lowerData(block);
                return true;
            }
        }

        if (isValidBlock(block.getRelative(0, 0, -1)) && block.getRelative(0, 0, -1).getData() < 0x7) {
            if (block.getRelative(0, 0, -1).getData() < block.getData() || block.getRelative(0, 0, -1).getType() != Material.SNOW) {
                if(!canLandOn(block.getRelative(0, -1, -1)))
                    return false;
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

        if (block.getRelative(0, -1, 0).getType() == Material.WATER || block.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER) {
            BlockState state = block.getRelative(0, -1, 0).getState();
            state.setType(Material.ICE);
            if(ProtectionUtil.canBlockForm(state.getBlock(), state))
                block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
        }

        if(!canLandOn(block.getRelative(0, -1, 0)))
            return;

        if (!isValidBlock(block) && isValidBlock(block.getRelative(0, 1, 0))) {
            incrementData(block.getRelative(0, 1, 0), depth+1);
            return;
        } else if (!isValidBlock(block)) return;

        if (depth < 1 && canPassThrough(block) && canPassThrough(block.getRelative(0, -1, 0))) {
            incrementData(block.getRelative(0, -1, 0), depth+1);
            return;
        } else if (depth >= 1 && canPassThrough(block) && canPassThrough(block.getRelative(0, -1, 0))) {

            boolean remove = false;
            if(block.getType() != Material.SNOW && canPassThrough(block)) {
                BlockState state = block.getState();
                state.setType(Material.SNOW);
                if(ProtectionUtil.canBlockForm(state.getBlock(), state)) {
                    block.setTypeId(BlockID.SNOW, false);
                    remove = true;
                }
            } else
                remove = false;
            final boolean fremove = remove;
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run () {
                    if(fremove)
                        block.setTypeId(0, false);
                    incrementData(block.getRelative(0, -1, 0), depth+1);
                }
            }, CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
            return;
        }

        if (canPassThrough(block) && block.getRelative(0, -1, 0).getType() == Material.SNOW && block.getRelative(0, -1, 0).getData() < 0x7) {
            incrementData(block.getRelative(0, -1, 0), depth+1);
            return;
        }

        if (block.getType() == Material.SNOW_BLOCK && isValidBlock(block.getRelative(0, 1, 0))) {
            incrementData(block.getRelative(0, 1, 0), depth+1);
            return;
        }

        if (CraftBookPlugin.inst().getConfiguration().snowRealistic) {
            if (block.getType() == Material.SNOW && disperse(block, false, depth+1)) return;
        }

        byte newData = 0;
        if (block.getType() != Material.SNOW && canPassThrough(block)) {
            BlockState state = block.getState();
            state.setType(Material.SNOW);
            if(ProtectionUtil.canBlockForm(state.getBlock(), state)) {
                block.setTypeId(BlockID.SNOW, false);
            }
        } else newData = (byte) (block.getData() + 1);
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
            if (p.getWorld() != block.getWorld()) continue;
            if (LocationUtil.getDistanceSquared(p.getLocation(), block.getLocation()) < CraftBookPlugin.inst().getServer().getViewDistance() * 16 * CraftBookPlugin.inst().getServer().getViewDistance() * 16)
                p.sendBlockChange(block.getLocation(), block.getType(), data);
        }
    }

    public boolean canLandOn(Block id) {


        switch(id.getType()) {

            case STEP:
            case WOOD_STEP:
            case ACTIVATOR_RAIL:
            case CAKE_BLOCK:
            case DAYLIGHT_DETECTOR:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
            case RAILS:
            case CARPET:
            case DETECTOR_RAIL:
            case POWERED_RAIL:
                return false;
            default:
                return true;
        }
    }

    public boolean canPassThrough(Block id) {

        return id.getType() == Material.AIR || CraftBookPlugin.inst().getConfiguration().snowRealisticReplacables.contains(new ItemInfo(id));
    }

    public boolean isSnowBlock(Block id) {

        return id.getType() == Material.SNOW;// || id == BlockID.SNOW_BLOCK;
    }

    public boolean isValidBlock(Block id) {

        return canPassThrough(id) || isSnowBlock(id);
    }

    @Override
    public void disable () {
        for(BukkitTask task : tasks.values())
            task.cancel();
        tasks.clear();
    }
}