package com.sk89q.craftbook.mechanics;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockVector;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Snow fall mechanism. Builds up/tramples snow
 *
 * @author Me4502
 */
public class Snow extends AbstractCraftBookMechanic {

    @Override
    public boolean enable() {
        if(meltSunlight || piling) {
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

    private boolean canLandOn(Block block) {
        switch(block.getType()) {
            case CAKE:
            case DAYLIGHT_DETECTOR:
            case REPEATER:
            case COMPARATOR:
            case TORCH:
            case AIR:
                return false;
            default:
                if (Tag.STAIRS.isTagged(block.getType())) {
                    return ((Stairs) block.getBlockData()).getHalf() == Bisected.Half.TOP;
                }
                if (Tag.SLABS.isTagged(block.getType())) {
                    return ((Slab) block.getBlockData()).getType() != Slab.Type.BOTTOM;
                }
                if (Tag.CARPETS.isTagged(block.getType()) || Tag.RAILS.isTagged(block.getType()) || Tag.SAPLINGS.isTagged(block.getType())) {
                    return false;
                }
                return !(!freezeWater && (block.getType() == Material.WATER)) && !isReplacable(block);
        }
    }

    private boolean isReplacable(Block block) {
        return !(block.getType() == Material.WATER)
                && (BlockUtil.isBlockReplacable(block.getType())
                || Blocks.containsFuzzy(realisticReplacables, BukkitAdapter.adapt(block.getBlockData())));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSnowballHit(ProjectileHitEvent event) {

        if (!place) return;

        if(!EventUtil.passesFilter(event))
            return;

        if (event.getEntity() instanceof Snowball) {

            Block block = event.getEntity().getLocation().getBlock();
            if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {

                if (CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild((Player)event.getEntity().getShooter(), block.getLocation(), true)) {
                    return;
                }

                CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer((Player) event.getEntity().getShooter());
                if (!player.hasPermission("craftbook.mech.snow.place")) return;
            }
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new SnowHandler(block, 1));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (!realistic) return;
        if(event.getBlock().getType() != Material.SNOW) return;

        if(!EventUtil.passesFilter(event))
            return;

        Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new SnowHandler(event.getBlock(), 0));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!trample && !slowdown) return;

        if(!EventUtil.passesFilter(event))
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (event.getTo().getBlock().getType() == Material.SNOW) {
            org.bukkit.block.data.type.Snow levelled = (org.bukkit.block.data.type.Snow) event.getTo().getBlock().getBlockData();
            if(slowdown) {
                if(levelled.getLayers() > 4)
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 2), true);
                else if(levelled.getLayers() > levelled.getMinimumLayers())
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1), true);
            }

            if (trample) {
                if (jumpTrample && !(event.getFrom().getY() - event.getTo().getY() >= 0.1D))
                    return;

                if (CraftBookPlugin.inst().getRandom().nextInt(20) == 0) {
                    if (levelled.getLayers() == levelled.getMinimumLayers() && partialTrample)
                        return;

                    if (!player.hasPermission("craftbook.mech.snow.trample"))
                        return;

                    if (CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil
                            .canBuild(event.getPlayer(), event.getPlayer().getLocation(), false))
                        return;
                    Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new SnowHandler(event.getTo().getBlock(), -1));
                }
            }
        }
    }

    private static final BlockFace[] UPDATE_FACES = {BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(event.getBlock().getType() == Material.SNOW) {

            if(!EventUtil.passesFilter(event))
                return;

            event.setCancelled(true);
            event.getBlock().setType(Material.AIR, false);
            for(ItemStack stack : BlockUtil.getBlockDrops(event.getBlock(), event.getPlayer().getInventory().getItemInMainHand()))
                event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), stack);

            if(realistic) {
                for(BlockFace dir : UPDATE_FACES) Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(event.getBlock().getRelative(dir), 0, event.getBlock().getLocation().toVector().toBlockVector()), animationTicks);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPhysicsUpdate(BlockPhysicsEvent event) {

        if(!realistic) return;

        if(!EventUtil.passesFilter(event))
            return;
        if((event.getBlock().getType() == Material.SNOW || event.getBlock().getType() == Material.SNOW_BLOCK) && CraftBookPlugin.inst().getRandom().nextInt(10) == 0)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(event.getBlock(), 0), animationTicks);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {

        if(!piling) return;

        if(!EventUtil.passesFilter(event))
            return;

        boolean isChunkUseful = false;

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                if(meltSunlight || event.getChunk().getBlock(x, event.getWorld().getMaxHeight()-1, z).getTemperature() < 0.15) {
                    isChunkUseful = true;
                    break;
                }
            }
        }

        if(!isChunkUseful) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowChunkHandler(event.getChunk()), getRandomDelay() * (event.getWorld().hasStorm() ? 20L : 10L));
    }

    private class SnowChunkHandler implements Runnable {

        public Chunk chunk;

        SnowChunkHandler(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public void run () {
            if(!chunk.isLoaded()) {
                return; //Abandon ship.
            }

            boolean meltMode = !chunk.getWorld().hasStorm() && meltSunlight;
            long delay = meltMode ? 5L : 20L;

            if (!LocationUtil.isBorderChunk(chunk)) {
                Block chunkBase = chunk.getBlock(0, 0, 0);
                Block highest = chunk.getWorld().getHighestBlockAt(chunkBase.getX() + CraftBookPlugin.inst().getRandom().nextInt(16),
                        chunkBase.getZ() + CraftBookPlugin.inst().getRandom().nextInt(16));

                if (highest.getType() == Material.SNOW || highest.getType() == Material.SNOW_BLOCK || highest.getType() == Material.ICE
                        || isReplacable(highest)) {
                    if (highest.getWorld().hasStorm() && highest.getType() != Material.ICE) {
                        if (highest.getTemperature() < 0.15) {
                            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(highest, 1), animationTicks);
                        }
                    } else if (meltMode) {
                        if (highest.getType() == Material.SNOW && meltPartial) {
                            if (((org.bukkit.block.data.type.Snow) highest.getBlockData()).getLayers() == 0) {
                                return;
                            }
                        }
                        if (highest.getTemperature() > 0.05) {
                            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(highest, -1), animationTicks);
                        }
                    }
                }

                delay *= getRandomDelay();
            } else {
                delay *= 50;
            }

            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), this, delay);
        }
    }

    private static long getRandomDelay() {
        return CraftBookPlugin.inst().getRandom().nextInt(4) + 1; // 5 is max possible, 1 is min possible.
    }

    private class SnowHandler implements Runnable {

        Block block;
        SnowBlock snowblock;
        int amount;

        SnowHandler(Block block, int amount) {
            snowblock = new SnowBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
            if(queue.contains(snowblock)) {
                snowblock = null;
                return;
            }
            this.block = block;
            this.amount = amount;
            queue.add(snowblock);
        }

        SnowHandler(Block block, int amount, BlockVector from) {
            this(block, amount);
            this.from = from;
        }

        BlockVector from;

        @Override
        public void run () {
            if(snowblock == null || !queue.contains(snowblock)) return; //Something went wrong here.

            queue.remove(snowblock);

            if(amount == 0) {
                if(realistic)
                    if(!disperse(block) && !canLandOn(block.getRelative(0, -1, 0)))
                        decreaseSnow(block, false);
            } else if (amount < 0) {
                if(decreaseSnow(block, true))
                    amount++;
                if(amount < 0)
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(block, amount), animationTicks);
            } else {
                if(increaseSnow(block, realistic))
                    amount--;
                if(amount > 0)
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(block, amount), animationTicks);
            }
        }

        boolean disperse(Block snow) {

            if(!realistic) return false;

            List<BlockFace> faces = new LinkedList<>(Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST));

            if(snow.getType() == Material.SNOW && canLandOn(snow.getRelative(0, -2, 0)) && isReplacable(snow.getRelative(0, -1, 0)))
                faces = new LinkedList<>(Collections.singletonList(BlockFace.DOWN));
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
                        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(block, 0, snow.getLocation().toVector().toBlockVector()),
                                animationTicks);
                    }
                    continue;
                }

                if (isReplacable(block) && snow.getType() == Material.SNOW && (snow.getData() == 0 || block.getType() == Material.SNOW && block.getData() == snow.getData() - 1) && dir != BlockFace.DOWN && dir != BlockFace.UP && canLandOn(block.getRelative(0, -1, 0)) && CraftBookPlugin.inst().getRandom().nextInt(10) != 0)
                    continue;

                int diff;
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

        boolean increaseSnow(Block snow, boolean disperse) {
            if (!animateFalling) {
                while (snow.getRelative(0, -1, 0).getType() == Material.AIR) {
                    snow = snow.getRelative(0, -1, 0);
                }
            }
            Block below = snow.getRelative(0, -1, 0);

            if(below.getType() != Material.AIR && isReplacable(below)) {
                if(below.getType() != Material.SNOW || below.getData() < 0x7)
                    return increaseSnow(below, disperse);
            }

            if (freezeWater && (below.getType() == Material.WATER)) {
                if(below.getData() == 0) {
                    BlockState state = below.getState();
                    state.setType(Material.ICE);
                    if(ProtectionUtil.canBlockForm(state.getBlock(), state))
                        below.setType(Material.ICE);
                } else below.setType(Material.AIR);
            } else if(below.getType() == Material.WATER) {
                return true; //Still return true, pretend it's actually succeeded.
            }

            if(snow.getType() != Material.SNOW && snow.getType() != Material.SNOW_BLOCK) {
                if (below.getType() == Material.SNOW_BLOCK) {
                    boolean allowed = false;
                    if (pileHigh) {
                        for (int i = 0; i < maxPileHeight + 1; i++) {
                            if (below.getRelative(0, -i, 0).getType() != Material.SNOW_BLOCK) {
                                allowed = true;
                                break;
                            }
                        }
                    }
                    if (!allowed) {
                        return false;
                    }
                }
                if(isReplacable(snow)) {
                    snow.setType(Material.SNOW, false);
                    if(disperse)
                        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(snow, 0, from), animationTicks);
                    return true;
                } else
                    return false;
            }
            org.bukkit.block.data.type.Snow snowData = (org.bukkit.block.data.type.Snow) snow.getBlockData();
            if(snowData.getLayers() + 1 > snowData.getMaximumLayers()) {
                if(pileHigh) {
                    boolean allowed = false;
                    for(int i = 0; i < maxPileHeight+1; i++) {
                        if(below.getRelative(0,-i,0).getType() != Material.SNOW_BLOCK) {
                            allowed = true;
                            break;
                        }
                    }
                    if(allowed) {
                        snow.setType(Material.SNOW_BLOCK);
                        if(disperse)
                            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(snow, 0, from), animationTicks);
                        return true;
                    } else
                        return false;
                } else
                    return false;
            } else {
                snowData.setLayers(snowData.getLayers() + 1);
                snow.setBlockData(snowData);
            }

            if(disperse)
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(snow, 0, from), animationTicks);

            return true;
        }

        boolean decreaseSnow(Block snow, boolean disperse) {

            if(snow.getType() == Material.ICE) {
                snow.setType(Material.WATER);
                return true;
            }

            if(snow.getType() != Material.SNOW && snow.getType() != Material.SNOW_BLOCK)
                return false;

            if(snow.getRelative(0, 1, 0).getType() == Material.SNOW || snow.getRelative(0, 1, 0).getType() == Material.SNOW_BLOCK) {
                return decreaseSnow(snow.getRelative(0,1,0), disperse);
            }

            if (snow.getType() == Material.SNOW_BLOCK) {
                org.bukkit.block.data.type.Snow snowData = (org.bukkit.block.data.type.Snow) Material.SNOW.createBlockData();
                snowData.setLayers(snowData.getMaximumLayers() - 1);
                snow.setBlockData(snowData, false);
            } else {
                org.bukkit.block.data.type.Snow snowData = (org.bukkit.block.data.type.Snow) snow.getBlockData();
                if (snowData.getLayers() == snowData.getMinimumLayers()) {
                    snow.setType(Material.AIR, false);
                } else {
                    snowData.setLayers(snowData.getLayers() - 1);
                }
                snow.setBlockData(snowData);
            }
            if(disperse && realistic) {
                for(BlockFace dir : UPDATE_FACES) Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SnowHandler(snow.getRelative(dir), 0, snow.getLocation().toVector().toBlockVector()),
                        animationTicks);
            }

            return true;
        }
    }

    private Set<SnowBlock> queue = new HashSet<>();

    private static class SnowBlock {

        int x,y,z;
        char[] worldname;

        SnowBlock(String worldname, int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.worldname = worldname.toCharArray();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SnowBlock
                    && ((SnowBlock) o).x == x && ((SnowBlock) o).y == y
                    && ((SnowBlock) o).z == z && Arrays.equals(((SnowBlock) o).worldname, worldname);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(worldname, x, y, z);
        }
    }

    private boolean piling;
    private boolean trample;
    private boolean partialTrample;
    private boolean place;
    private boolean slowdown;
    private boolean realistic;
    private boolean pileHigh;
    private int maxPileHeight;
    private boolean jumpTrample;
    private List<BaseBlock> realisticReplacables;
    private int animationTicks;
    private boolean freezeWater;
    private boolean meltSunlight;
    private boolean meltPartial;
    private boolean animateFalling;

    private static List<String> getDefaultReplacables() {
        return Lists.newArrayList(
                BlockTypes.DEAD_BUSH.getId(),
                BlockTypes.GRASS.getId(),
                BlockTypes.DANDELION.getId(),
                BlockTypes.POPPY.getId(),
                BlockTypes.BROWN_MUSHROOM.getId(),
                BlockTypes.RED_MUSHROOM.getId(),
                BlockTypes.FIRE.getId(),
                BlockTypes.FERN.getId());
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "piling", "Enables the piling feature of the Snow mechanic.");
        piling = config.getBoolean(path + "piling", false);

        config.setComment(path + "trample", "Enables the trampling feature of the Snow mechanic.");
        trample = config.getBoolean(path + "trample", false);

        config.setComment(path + "partial-trample-only", "If trampling is enabled, only trample it down to the smallest snow.");
        partialTrample = config.getBoolean(path + "partial-trample-only", false);

        config.setComment(path + "jump-trample", "Require jumping to trample snow.");
        jumpTrample = config.getBoolean(path + "jump-trample", false);

        config.setComment(path + "place", "Allow snowballs to create snow when they land.");
        place = config.getBoolean(path + "place", false);

        config.setComment(path + "slowdown", "Slows down entities as they walk through thick snow.");
        slowdown = config.getBoolean(path + "slowdown", false);

        config.setComment(path + "realistic", "Realistically move snow around, creating an 'avalanche' or 'mound' effect.");
        realistic = config.getBoolean(path + "realistic", false);

        config.setComment(path + "high-piling", "Allow piling above the 1 block height.");
        pileHigh = config.getBoolean(path + "high-piling", false);

        config.setComment(path + "max-pile-height", "The maximum piling height of high piling snow.");
        maxPileHeight = config.getInt(path + "max-pile-height", 3);

        config.setComment(path + "replacable-blocks", "A list of blocks that can be replaced by realistic snow.");
        realisticReplacables = BlockSyntax.getBlocks(config.getStringList(path + "replacable-blocks", getDefaultReplacables()), true);

        config.setComment(path + "animate-falling", "Cause the snow to fall slowly (May be intensive).");
        animateFalling = config.getBoolean(path + "animate-falling", false);

        config.setComment(path + "falldown-animation-speed", "The animation delay of all snow interactions in ticks.");
        animationTicks = config.getInt(path + "falldown-animation-speed", 5);

        config.setComment(path + "freeze-water", "Should snow freeze water?");
        freezeWater = config.getBoolean(path + "freeze-water", false);

        config.setComment(path + "melt-in-sunlight", "Enables snow to melt in sunlight.");
        meltSunlight = config.getBoolean(path + "melt-in-sunlight", false);

        config.setComment(path + "partial-melt-only", "If melt in sunlight is enabled, only melt it down to the smallest snow.");
        meltPartial = config.getBoolean(path + "partial-melt-only", false);
    }
}