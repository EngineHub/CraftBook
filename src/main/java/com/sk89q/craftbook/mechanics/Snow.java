package com.sk89q.craftbook.mechanics;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockVector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ProtectionUtil;

/**
 * Snow fall mechanism. Builds up/tramples snow
 *
 * @author Me4502
 */
public class Snow extends AbstractCraftBookMechanic {

    @Override
    public boolean enable() {

        if(CraftBookPlugin.inst().getConfiguration().snowMeltSunlight || CraftBookPlugin.inst().getConfiguration().snowPiling || CraftBookPlugin.inst().getConfiguration().snowRealistic) {
            for(World world : Bukkit.getWorlds()) {
                for(Chunk chunk : world.getLoadedChunks()) {
                    boolean isChunkUseful = false;
                    for(int x = 0; x < 16; x++) {
                        for(int z = 0; z < 16; z++) {
                            if(chunk.getBlock(x, world.getMaxHeight()-1, z).getTemperature() < 0.15) {
                                isChunkUseful = true;
                                break;
                            }
                        }
                    }

                    if(!isChunkUseful) continue;

                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowChunkHandler(chunk), getRandomDelay() * 20L);
                }
            }
        }

        return true;
    }

    public boolean canLandOn(Block id) {

        switch(id.getType()) {

            case WOOD_STAIRS:
            case BRICK_STAIRS:
            case SMOOTH_STAIRS:
            case SANDSTONE_STAIRS:
            case QUARTZ_STAIRS:
            case COBBLESTONE_STAIRS:
                return new Stairs(id.getType(), id.getData()).isInverted();
            case STEP:
            case WOOD_STEP:
                return new Step(id.getType(), id.getData()).isInverted();
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
            case SAPLING:
            case TORCH:
            case AIR:
                return false;
            default:
                if(!CraftBookPlugin.inst().getConfiguration().snowFreezeWater && (id.getType() == Material.WATER || id.getType() == Material.STATIONARY_WATER)) return false;
                return !isReplacable(id);
        }
    }

    public boolean isReplacable(Block block) {

        if(block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) return false;
        if(BlockUtil.isBlockReplacable(block.getType())) return true;
        return CraftBookPlugin.inst().getConfiguration().snowRealisticReplacables.contains(new ItemInfo(block));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSnowballHit(ProjectileHitEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowPlace) return;

        if(!EventUtil.passesFilter(event))
            return;

        if (event.getEntity() instanceof Snowball) {

            Block block = event.getEntity().getLocation().getBlock();
            if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {

                if (CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild((Player)event.getEntity().getShooter(), block.getLocation(), true)) {
                    return;
                }

                LocalPlayer player = CraftBookPlugin.inst().wrapPlayer((Player) event.getEntity().getShooter());
                if (!player.hasPermission("craftbook.mech.snow.place")) return;
            }
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new SnowHandler(block, 1));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowRealistic) return;
        if(event.getBlock().getType() != Material.SNOW) return;

        if(!EventUtil.passesFilter(event))
            return;

        Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new SnowHandler(event.getBlock(), 0));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().snowTrample) return;

        if(!EventUtil.passesFilter(event))
            return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (event.getTo().getBlock().getType() == Material.SNOW) {

            if(CraftBookPlugin.inst().getConfiguration().snowSlowdown) {
                if(event.getTo().getBlock().getData() > 4)
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 2), true);
                else if(event.getTo().getBlock().getData() > 0)
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1), true);
            }

            if (!player.hasPermission("craftbook.mech.snow.trample")) return;

            if (CraftBookPlugin.inst().getConfiguration().snowJumpTrample && !(event.getFrom().getY() - event.getTo().getY() >= 0.1D))
                return;

            if (CraftBookPlugin.inst().getRandom().nextInt(20) == 0) {
                if(event.getTo().getBlock().getData() == 0x0 && CraftBookPlugin.inst().getConfiguration().snowPartialTrample) return;
                if (CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(event.getPlayer(), event.getPlayer().getLocation(), false)) return;
                Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new SnowHandler(event.getTo().getBlock(), -1));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(event.getBlock().getType() == Material.SNOW) {

            if(!EventUtil.passesFilter(event))
                return;

            event.setCancelled(true);
            event.getBlock().setTypeId(Material.AIR.getId(), false);
            for(ItemStack stack : BlockUtil.getBlockDrops(event.getBlock(), event.getPlayer().getItemInHand()))
                event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), stack);

            if(CraftBookPlugin.inst().getConfiguration().snowRealistic) {
                BlockFace[] faces = new BlockFace[]{BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
                for(BlockFace dir : faces) Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(event.getBlock().getRelative(dir), 0, event.getBlock().getLocation().toVector().toBlockVector()), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPhysicsUpdate(BlockPhysicsEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().snowRealistic) return;

        if(!EventUtil.passesFilter(event))
            return;
        if((event.getBlock().getType() == Material.SNOW || event.getBlock().getType() == Material.SNOW_BLOCK) && CraftBookPlugin.inst().getRandom().nextInt(10) == 0)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(event.getBlock(), 0), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().snowPiling) return;

        if(!EventUtil.passesFilter(event))
            return;

        boolean isChunkUseful = false;

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                if(event.getChunk().getBlock(x, event.getWorld().getMaxHeight()-1, z).getTemperature() < 0.15) {
                    isChunkUseful = true;
                    break;
                }
            }
        }

        if(!isChunkUseful) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowChunkHandler(event.getChunk()), getRandomDelay() * (event.getWorld().hasStorm() ? 20L : 10L));
    }

    public class SnowChunkHandler implements Runnable {

        public Chunk chunk;

        public SnowChunkHandler(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public void run () {

            if(!chunk.isLoaded()) return; //Abandon ship.

            boolean skip = false; //Skip on some circumstances, to alleviate lag.
            //if(Bukkit.getScheduler().getPendingTasks().size() > 10000)
            //    skip = true;

            boolean meltMode = false;
            if(!chunk.getWorld().hasStorm() && CraftBookPlugin.inst().getConfiguration().snowMeltSunlight)
                meltMode = true;

            if(!skip) {
                Block highest = chunk.getWorld().getHighestBlockAt(chunk.getBlock(0, 0, 0).getX() + CraftBookPlugin.inst().getRandom().nextInt(16), chunk.getBlock(0, 0, 0).getZ() + CraftBookPlugin.inst().getRandom().nextInt(16));

                if(highest.getType() == Material.SNOW || highest.getType() == Material.SNOW_BLOCK || highest.getType() == Material.ICE || isReplacable(highest)) {

                    if(highest.getWorld().hasStorm() && highest.getType() != Material.ICE) {
                        if(highest.getTemperature() < 0.15) {
                            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(highest, 1), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                        }
                    } else if(meltMode) {
                        if(highest.getType() == Material.SNOW && CraftBookPlugin.inst().getConfiguration().snowMeltPartial)
                            if(highest.getData() == 0) return;
                        if(highest.getTemperature() > 0.05)
                            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(highest, -1), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                    }
                }
            }

            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), this, getRandomDelay() * (skip ? 100L : meltMode ? 5L : 20L));
        }
    }

    public long getRandomDelay() {
        return CraftBookPlugin.inst().getRandom().nextInt(4) + 1; // 5 is max possible, 1 is min possible.
    }

    public class SnowHandler implements Runnable {

        Block block;
        SnowBlock snowblock;
        int amount;

        public SnowHandler(Block block, int amount) {
            snowblock = new SnowBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
            if(queue.contains(snowblock)) {
                snowblock = null;
                return;
            }
            this.block = block;
            this.amount = amount;
            queue.add(snowblock);
        }

        public SnowHandler(Block block, int amount, BlockVector from) {
            this(block, amount);
            this.from = from;
        }

        BlockVector from;

        @Override
        public void run () {

            if(snowblock == null || !queue.contains(snowblock)) return; //Something went wrong here.

            queue.remove(snowblock);

            if(amount == 0) {
                if(CraftBookPlugin.inst().getConfiguration().snowRealistic)
                    if(!disperse(block) && !canLandOn(block.getRelative(0, -1, 0)))
                        decreaseSnow(block, false);
            } else if (amount < 0) { // Odd edge case.
                if(decreaseSnow(block, true))
                    amount++;
                if(amount < 0)
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(block, amount), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                return;
            } else {
                if(increaseSnow(block, CraftBookPlugin.inst().getConfiguration().snowRealistic))
                    amount--;
                if(amount > 0)
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(block, amount), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                return;
            }
        }

        public boolean disperse(Block snow) {

            if(!CraftBookPlugin.inst().getConfiguration().snowRealistic) return false;

            List<BlockFace> faces = new LinkedList<BlockFace>(Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST));

            if(snow.getType() == Material.SNOW && canLandOn(snow.getRelative(0, -2, 0)) && isReplacable(snow.getRelative(0, -1, 0)))
                faces = new LinkedList<BlockFace>(Arrays.asList(BlockFace.DOWN));
            else {
                Collections.shuffle(faces, CraftBookPlugin.inst().getRandom());
                faces.add(0, BlockFace.DOWN);
            }

            BlockFace best = null;
            int bestDiff = 0;

            for(BlockFace dir : faces) {

                Block block = snow.getRelative(dir);
                if(from != null && block.getLocation().getBlockX() == from.getBlockX() && block.getLocation().getBlockY() == from.getBlockY() && block.getLocation().getBlockZ() == from.getBlockZ()) continue;
                if(!isReplacable(block)) continue;
                if(queue.contains(new SnowBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()))) continue;
                if(block.getType() == Material.SNOW && snow.getType() == Material.SNOW && block.getData() >= snow.getData() && dir != BlockFace.DOWN && dir != BlockFace.UP) {
                    if(block.getData() > snow.getData()+1) {
                        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(block, 0, snow.getLocation().toVector().toBlockVector()), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                    }
                    continue;
                }

                if (isReplacable(block) && snow.getType() == Material.SNOW && (snow.getData() == 0 || block.getType() == Material.SNOW && block.getData() == snow.getData() - 1) && dir != BlockFace.DOWN && dir != BlockFace.UP && canLandOn(block.getRelative(0, -1, 0)) && CraftBookPlugin.inst().getRandom().nextInt(10) != 0)
                    continue;

                int diff = 0;
                if(block.getType() == Material.SNOW) {
                    if(snow.getData() == 2 && block.getData() == 1 && dir != BlockFace.DOWN)
                        continue;
                    diff = snow.getData() - block.getData();
                } else
                    diff = block.getType() == Material.AIR ? 100 : 99;
                if(diff > bestDiff || dir == BlockFace.DOWN) {
                    best = dir;
                    bestDiff = diff;
                    if(dir == BlockFace.DOWN) break;
                }
            }

            if(best != null) {
                Block block = snow.getRelative(best);
                if(snow.getLocation().equals(block.getLocation())) return false;
                boolean success = false;
                if(amount < 0) {
                    if(decreaseSnow(block, false))
                        success = increaseSnow(snow, true);
                } else {
                    if(decreaseSnow(snow, false))
                        success = increaseSnow(block, true);
                }

                return success;
            }

            return false;
        }

        public boolean increaseSnow(Block snow, boolean disperse) {

            if(snow.getRelative(0, -1, 0).getType() != Material.AIR && isReplacable(snow.getRelative(0, -1, 0))) {
                if(snow.getRelative(0, -1, 0).getType() != Material.SNOW || snow.getRelative(0,-1,0).getData() < 0x7)
                    return increaseSnow(snow.getRelative(0,-1,0), disperse);
            }

            if (CraftBookPlugin.inst().getConfiguration().snowFreezeWater && (snow.getRelative(0, -1, 0).getType() == Material.WATER || snow.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER)) {
                if(snow.getRelative(0, -1, 0).getData() == 0) {
                    BlockState state = snow.getRelative(0, -1, 0).getState();
                    state.setType(Material.ICE);
                    if(ProtectionUtil.canBlockForm(state.getBlock(), state))
                        snow.getRelative(0, -1, 0).setType(Material.ICE);
                } else snow.getRelative(0, -1, 0).setType(Material.AIR);
            } else if(snow.getRelative(0, -1, 0).getType() == Material.WATER || snow.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER) {
                return true; //Still return true, pretend it's actually succeeded.
            }

            if(snow.getType() != Material.SNOW && snow.getType() != Material.SNOW_BLOCK) {
                if(isReplacable(snow)) {
                    snow.setTypeIdAndData(Material.SNOW.getId(), (byte) 0, false);
                    if(disperse)
                        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(snow, 0, from), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                    return true;
                } else
                    return false;
            }
            byte data = (byte) (snow.getData()+1);
            if(data > 0x6) {
                if(CraftBookPlugin.inst().getConfiguration().snowHighPiles) {
                    boolean allowed = false;
                    for(int i = 0; i < CraftBookPlugin.inst().getConfiguration().snowMaxPileHeight+1; i++) {
                        if(snow.getRelative(0,-i,0).getType() != Material.SNOW_BLOCK) {
                            allowed = true;
                            break;
                        }
                    }
                    if(allowed) {
                        snow.setType(Material.SNOW_BLOCK);
                        if(disperse)
                            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(snow, 0, from), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                        return true;
                    } else
                        return false;
                } else
                    return false;
            } else
                snow.setData(data, false);

            if(disperse)
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(snow, 0, from), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);

            return true;
        }

        public boolean decreaseSnow(Block snow, boolean disperse) {

            if(snow.getType() == Material.ICE) {
                snow.setType(Material.WATER);
                return true;
            }

            if(snow.getType() != Material.SNOW && snow.getType() != Material.SNOW_BLOCK)
                return false;

            if(snow.getRelative(0, 1, 0).getType() == Material.SNOW || snow.getRelative(0, 1, 0).getType() == Material.SNOW_BLOCK) {
                return decreaseSnow(snow.getRelative(0,1,0), disperse);
            }

            byte data = (byte) (snow.getData()-1);
            if(snow.getType() == Material.SNOW && snow.getData() == 0x0)
                snow.setTypeId(Material.AIR.getId(), false);
            else if(snow.getType() == Material.SNOW_BLOCK)
                snow.setTypeIdAndData(Material.SNOW.getId(), (byte)6, false);
            else
                snow.setData(data, false);

            if(disperse && CraftBookPlugin.inst().getConfiguration().snowRealistic) {
                BlockFace[] faces = new BlockFace[]{BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
                for(BlockFace dir : faces) Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(snow.getRelative(dir), 0, snow.getLocation().toVector().toBlockVector()), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
            }

            return true;
        }
    }

    private Set<SnowBlock> queue = new HashSet<SnowBlock>();

    private class SnowBlock {

        int x,y,z;
        char[] worldname;

        public SnowBlock(String worldname, int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.worldname = worldname.toCharArray();
        }

        @Override
        public boolean equals(Object o) {
            if(!(o instanceof SnowBlock)) return false;
            if(((SnowBlock)o).x == x && ((SnowBlock)o).y == y && ((SnowBlock)o).z == z) return ((SnowBlock)o).worldname.equals(worldname);
            return false;
        }

        @Override
        public int hashCode() {
            // Constants correspond to glibc's lcg algorithm parameters
            return (worldname.hashCode() * 1103515245 + 12345 ^ x * 1103515245 + 12345 ^ y * 1103515245 + 12345 ^ z * 1103515245 + 12345) * 1103515245 + 12345;
        }
    }
}