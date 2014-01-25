package com.sk89q.craftbook.mech;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.util.BlockVector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemInfo;

/**
 * Snow fall mechanism. Builds up/tramples snow
 *
 * @author Me4502
 */
public class Snow extends AbstractCraftBookMechanic {

    /*private Map<Location, BukkitTask> tasks;

    @Override
    public boolean enable() {
        tasks = new HashMap<Location, BukkitTask>();

        return true;
    }

    @Override
    public void disable() {
        for(BukkitTask task : tasks.values())
            task.cancel();
        tasks = null;
    }

    @EventHandler(priority = EventPriority.HIGH)
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

    @EventHandler(priority = EventPriority.HIGH)
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

    @EventHandler(priority = EventPriority.HIGH)
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

    @EventHandler(priority = EventPriority.HIGH)
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
        long delay = CraftBookPlugin.inst().getRandom().nextInt(80) + 60; // 140 is max possible
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

        if (CraftBookPlugin.inst().getConfiguration().snowFreezeWater && (block.getRelative(0, -1, 0).getType() == Material.WATER || block.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER)) {
            if(block.getRelative(0, -1, 0).getData() == 0) {
                BlockState state = block.getRelative(0, -1, 0).getState();
                state.setType(Material.ICE);
                if(ProtectionUtil.canBlockForm(state.getBlock(), state))
                    block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
            } else block.getRelative(0, -1, 0).setTypeId(BlockID.AIR, false);
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

        if (CraftBookPlugin.inst().getConfiguration().snowFreezeWater && (block.getRelative(0, -1, 0).getType() == Material.WATER || block.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER)) {
            if(block.getRelative(0, -1, 0).getData() == 0) {
                BlockState state = block.getRelative(0, -1, 0).getState();
                state.setType(Material.ICE);
                if(ProtectionUtil.canBlockForm(state.getBlock(), state))
                    block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
            } else block.getRelative(0, -1, 0).setTypeId(BlockID.AIR, false);
        } else if(block.getRelative(0, -1, 0).getType() == Material.WATER || block.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER) {
            if (remove) lowerData(block);
            return false;
        }

        if (isValidBlock(block.getRelative(0, -1, 0)) && block.getRelative(0, -1, 0).getData() < 0x7) {
            if (block.getRelative(0, -1, 0).getData() < block.getData() || block.getRelative(0, -1, 0).getType() != Material.SNOW) {
                if(!canLandOn(block.getRelative(0, -2, 0)))
                    return false;
                if(incrementData(block.getRelative(0, -1, 0), depth+1)) {
                    if (remove) lowerData(block);
                    return true;
                }
            }
        }

        if (isValidBlock(block.getRelative(1, 0, 0)) && block.getRelative(1, 0, 0).getData() < 0x7) {
            if (block.getRelative(1, 0, 0).getData() < block.getData() || block.getRelative(1, 0,0).getType() != Material.SNOW) {
                if(!canLandOn(block.getRelative(1, -1, 0)))
                    return false;
                if(incrementData(block.getRelative(1, 0, 0), depth+1)) {
                    if (remove) lowerData(block);
                    return true;
                }
            }
        }

        if (isValidBlock(block.getRelative(-1, 0, 0)) && block.getRelative(-1, 0, 0).getData() < 0x7) {
            if (block.getRelative(-1, 0, 0).getData() < block.getData() || block.getRelative(-1, 0, 0).getType() != Material.SNOW) {
                if(!canLandOn(block.getRelative(-1, -1, 0)))
                    return false;
                if(incrementData(block.getRelative(-1, 0, 0), depth+1)) {
                    if (remove) lowerData(block);
                    return true;
                }
            }
        }

        if (isValidBlock(block.getRelative(0, 0, 1)) && block.getRelative(0, 0, 1).getData() < 0x7) {
            if (block.getRelative(0, 0, 1).getData() < block.getData() || block.getRelative(0, 0, 1).getType() != Material.SNOW) {
                if(!canLandOn(block.getRelative(0, -1, 1)))
                    return false;
                if(incrementData(block.getRelative(0, 0, 1), depth+1)) {
                    if (remove) lowerData(block);
                    return true;
                }
            }
        }

        if (isValidBlock(block.getRelative(0, 0, -1)) && block.getRelative(0, 0, -1).getData() < 0x7) {
            if (block.getRelative(0, 0, -1).getData() < block.getData() || block.getRelative(0, 0, -1).getType() != Material.SNOW) {
                if(!canLandOn(block.getRelative(0, -1, -1)))
                    return false;
                if(incrementData(block.getRelative(0, 0, -1), depth+1)) {
                    if (remove) lowerData(block);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean incrementData(final Block block, final int depth) {

        if(block.getLocation().getY() == 0)
            return false;

        if (CraftBookPlugin.inst().getConfiguration().snowFreezeWater && (block.getRelative(0, -1, 0).getType() == Material.WATER || block.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER)) {
            if(block.getRelative(0, -1, 0).getData() == 0) {
                BlockState state = block.getRelative(0, -1, 0).getState();
                state.setType(Material.ICE);
                if(ProtectionUtil.canBlockForm(state.getBlock(), state))
                    block.getRelative(0, -1, 0).setTypeId(BlockID.ICE, false);
            } else block.getRelative(0, -1, 0).setTypeId(BlockID.AIR, false);
        } else if(block.getRelative(0, -1, 0).getType() == Material.WATER || block.getRelative(0, -1, 0).getType() == Material.STATIONARY_WATER) {
            return true; //Make it still erase the snow, so it doesn't pile on the edged.
        }

        if(!canLandOn(block.getRelative(0, -1, 0)))
            return false;

        if (!isValidBlock(block) && isValidBlock(block.getRelative(0, 1, 0))) {
            return incrementData(block.getRelative(0, 1, 0), depth+1);
        } else if (!isValidBlock(block)) return false;

        if (depth < 1 && canPassThrough(block) && canPassThrough(block.getRelative(0, -1, 0))) {
            return incrementData(block.getRelative(0, -1, 0), depth+1);
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
            return true;
        }

        if (canPassThrough(block) && block.getRelative(0, -1, 0).getType() == Material.SNOW && block.getRelative(0, -1, 0).getData() < 0x7) {
            return incrementData(block.getRelative(0, -1, 0), depth+1);
        }

        if (block.getType() == Material.SNOW_BLOCK && isValidBlock(block.getRelative(0, 1, 0))) {
            return incrementData(block.getRelative(0, 1, 0), depth+1);
        }

        if (CraftBookPlugin.inst().getConfiguration().snowRealistic) {
            if (block.getType() == Material.SNOW && disperse(block, false, depth+1)) return true;
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
            Block test = block;
            int piled = 0;
            while(test.getRelative(BlockFace.DOWN).getType() == Material.SNOW_BLOCK) {

                piled++;
                if(piled > CraftBookPlugin.inst().getConfiguration().snowMaxPileHeight) break;
            }
            if(piled <= CraftBookPlugin.inst().getConfiguration().snowMaxPileHeight) {
                block.setTypeId(BlockID.SNOW_BLOCK, false);
                newData = (byte) 0;
            }
        } else if (newData > 7) {
            newData = 7;
        }
        setBlockDataWithNotify(block, newData);

        return true;
    }

    public void setBlockDataWithNotify(Block block, byte data) {

        block.setTypeIdAndData(block.getTypeId(), data, false);
        for (Player p : block.getWorld().getPlayers()) {
            if (p.getWorld() != block.getWorld()) continue;
            if (LocationUtil.getDistanceSquared(p.getLocation(), block.getLocation()) < CraftBookPlugin.inst().getServer().getViewDistance() * 16 * CraftBookPlugin.inst().getServer().getViewDistance() * 16)
                p.sendBlockChange(block.getLocation(), block.getType(), data);
        }
    }*/

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
                return false;
            default:
                if(!CraftBookPlugin.inst().getConfiguration().snowFreezeWater && (id.getType() == Material.WATER || id.getType() == Material.STATIONARY_WATER)) return false;
                return true;
        }
    }

    public boolean isReplacable(Block block) {

        if(BlockUtil.isBlockReplacable(block.getType())) return true;
        return CraftBookPlugin.inst().getConfiguration().snowRealisticReplacables.contains(new ItemInfo(block));
    }

    @Override
    public boolean enable() {

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

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event))
            return;
        if(event.getBlock().getType() == Material.SNOW) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            for(ItemStack stack : BlockUtil.getBlockDrops(event.getBlock(), event.getPlayer().getItemInHand()))
                event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), stack);

            if(CraftBookPlugin.inst().getConfiguration().snowRealistic) {
                BlockFace[] faces = new BlockFace[]{BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
                for(BlockFace dir : faces) Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(event.getBlock().getRelative(dir), 0, event.getBlock().getLocation().toVector().toBlockVector()), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
            }
        }
    }

    @EventHandler
    public void onPhysicsUpdate(BlockPhysicsEvent event) {

        if(!EventUtil.passesFilter(event))
            return;
        if(event.getBlock().getType() == Material.SNOW || event.getBlock().getType() == Material.SNOW_BLOCK && CraftBookPlugin.inst().getRandom().nextInt(10) == 0)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(event.getBlock(), 0), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        if(!EventUtil.passesFilter(event))
            return;
        if(!CraftBookPlugin.inst().getConfiguration().snowPiling) return;

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
            if(Bukkit.getScheduler().getPendingTasks().size() > 10000)
                skip = true;

            boolean meltMode = false;
            if(!chunk.getWorld().hasStorm() && CraftBookPlugin.inst().getConfiguration().snowMeltSunlight)
                meltMode = true;

            if(!skip) {
                Block highest = chunk.getWorld().getHighestBlockAt(chunk.getBlock(0, 0, 0).getX() + CraftBookPlugin.inst().getRandom().nextInt(16), chunk.getBlock(0, 0, 0).getZ() + CraftBookPlugin.inst().getRandom().nextInt(16));

                if(highest.getType() == Material.SNOW || highest.getType() == Material.SNOW_BLOCK || isReplacable(highest)) {

                    if(highest.getWorld().hasStorm()) {
                        if(highest.getTemperature() < 0.15) {
                            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(highest, 1), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                        }
                    } else if(meltMode) {
                        if(highest.getTemperature() > 0.10)
                            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(highest, -1), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                    }
                }
            }

            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), this, getRandomDelay() * (skip ? 100L : meltMode ? 20L : 5L));
        }
    }

    public long getRandomDelay() {
        return CraftBookPlugin.inst().getRandom().nextInt(4) + 1; // 5 is max possible, 1 is min possible.
    }

    public class SnowHandler implements Runnable {

        Block block;
        int amount;

        public SnowHandler(Block block, int amount) {
            this.block = block;
            this.amount = amount;
        }

        public SnowHandler(Block block, int amount, BlockVector from) {
            this.block = block;
            this.amount = amount;
            this.from = from;
        }

        BlockVector from;

        @Override
        public void run () {

            if(amount == 0) {
                if(CraftBookPlugin.inst().getConfiguration().snowRealistic)
                    if(!disperse(block, from) && !canLandOn(block.getRelative(0, -1, 0)))
                        decreaseSnow(block, false);
            } else if (amount < 0) { // Odd edge case.
                decreaseSnow(block, true);
                amount++;
                if(amount < 0)
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(block, amount), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                return;
            } else {
                if(!CraftBookPlugin.inst().getConfiguration().snowRealistic) {
                    increaseSnow(block, false);
                    amount--;
                } else {
                    if(disperse(block, from))
                        amount--;
                    else {
                        increaseSnow(block, true);
                        amount--;
                    }
                }
                if(amount > 0)
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(block, amount), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                return;
            }
        }

        public boolean disperse(Block snow, BlockVector from) {

            if(!CraftBookPlugin.inst().getConfiguration().snowRealistic) return false;

            List<BlockFace> faces = new LinkedList<BlockFace>(Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST));

            if(snow.getType() == Material.SNOW && snow.getData() == 0x0 && canLandOn(snow.getRelative(0, -1, 0)))
                faces = new LinkedList<BlockFace>(Arrays.asList(BlockFace.DOWN));
            else {
                for(int i = 0; i < 2; i++)
                    Collections.shuffle(faces, CraftBookPlugin.inst().getRandom());
                faces.add(0, BlockFace.DOWN);
            }

            BlockFace best = null;
            int bestDiff = 0;

            for(BlockFace dir : faces) {

                Block block = snow.getRelative(dir);
                if(from != null && block.getLocation().toVector().toBlockVector().equals(from)) continue;
                if(!isReplacable(snow)) continue;
                if(block.getType() == Material.SNOW && block.getData() >= snow.getData() && dir != BlockFace.DOWN && dir != BlockFace.UP) {
                    if(block.getData() > snow.getData()+1) {
                        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(block, 0, snow.getLocation().toVector().toBlockVector()), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                    }
                    continue;
                }

                int diff = 0;
                if(block.getType() == Material.SNOW) {
                    if(snow.getData() == 2 && block.getData() == 1 && dir != BlockFace.DOWN)
                        continue;
                    diff = snow.getData() - block.getData();
                } else
                    diff = 100;
                if(diff > bestDiff || dir == BlockFace.DOWN) {
                    best = dir;
                    bestDiff = diff;
                    if(dir == BlockFace.DOWN) break;
                }
            }

            if(best != null) {
                Block block = snow.getRelative(best);
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

            if(isReplacable(snow.getRelative(0, -1, 0))) {
                if(snow.getRelative(0, -1, 0).getType() != Material.SNOW || snow.getRelative(0,-1,0).getData() < 0x7)
                    return increaseSnow(snow.getRelative(0,-1,0), disperse);
            }

            if(snow.getType() != Material.SNOW && snow.getType() != Material.SNOW_BLOCK) {
                if(isReplacable(snow)) {
                    snow.setTypeId(Material.SNOW.getId(), false);
                    if(disperse)
                        disperse(snow, null);
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
                        snow.setTypeId(Material.SNOW_BLOCK.getId(), false);
                        if(disperse)
                            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(snow, 0), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
                        return true;
                    } else
                        return false;
                } else
                    return false;
            } else
                snow.setData(data, false);

            if(disperse)
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(snow, 0), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);

            return true;
        }

        public boolean decreaseSnow(Block snow, boolean disperse) {
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
                for(BlockFace dir : faces) Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(block.getRelative(dir), 0, block.getLocation().toVector().toBlockVector()), CraftBookPlugin.inst().getConfiguration().snowFallAnimationSpeed);
            }

            return true;
        }
    }
}