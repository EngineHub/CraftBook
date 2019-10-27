package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;

public class Sponge extends AbstractCraftBookMechanic {

    private boolean isValidSponge(Block block) {
        return block.getType() == Material.SPONGE || (includeWet && block.getType() == Material.WET_SPONGE);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFromTo(BlockFromToEvent event) {

        if(event.getBlock().getType() != Material.WATER) return;

        if(!BlockUtil.isBlockReplacable(event.getToBlock().getType())) return;

        if(!EventUtil.passesFilter(event)) return;

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cy = -radius; cy <= radius; cy++) {
                for (int cz = -radius; cz <= radius; cz++) {
                    Block sponge = event.getToBlock().getRelative(cx, cy, cz);
                    if(circularRadius && !LocationUtil.isWithinSphericalRadius(sponge.getLocation(), event.getToBlock().getLocation(), radius)) continue;
                    if(redstone && !sponge.isBlockIndirectlyPowered()) continue;
                    if(isValidSponge(sponge)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {

        if(!isValidSponge(event.getBlock())) return;

        if(redstone && !event.getBlock().isBlockIndirectlyPowered()) return;

        if(!EventUtil.passesFilter(event)) return;

        removeWater(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!isValidSponge(event.getBlock())) return;

        if(redstone && !event.getBlock().isBlockIndirectlyPowered()) return;

        if(!EventUtil.passesFilter(event)) return;

        Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> addWater(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!redstone) return;
        if(!isValidSponge(event.getBlock())) return;

        if(event.isMinor()) return;

        if(!EventUtil.passesFilter(event)) return;

        if(!event.isOn())
            addWater(event.getBlock());
        else
            removeWater(event.getBlock());
    }

    public void removeWater(Block block) {
        for (int cx = -radius; cx <= radius; cx++) {
            for (int cy = -radius; cy <= radius; cy++) {
                for (int cz = -radius; cz <= radius; cz++) {
                    Block water = block.getRelative(cx, cy, cz);
                    if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius)) continue;
                    if(water.getType() == Material.WATER) {
                        water.setType(Material.AIR);
                    }
                }
            }
        }
    }

    public void addWater(Block block) {

        int cx,cy,cz;

        // The negative x edge
        cx = block.getX() - radius - 1;
        for (cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
            for (cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(1, 0, 0).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(1, 0, 0));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(1, 0, 0).setType(Material.WATER);
                    }
                }
            }
        }

        // The positive x edge
        cx = block.getX() + radius + 1;
        for (cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
            for (cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(-1, 0, 0).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(-1, 0, 0));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(-1, 0, 0).setType(Material.WATER);
                    }
                }
            }
        }

        // The negative y edge
        /*cy = block.getY() - radius - 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (int cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, 1, 0).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(0, 1, 0));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(0, 1, 0).setType(Material.WATER);
                    }
                }
            }
        }*/

        // The positive y edge
        cy = block.getY() + radius + 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, -1, 0).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(0, -1, 0));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(0, -1, 0).setType(Material.WATER);
                    }
                }
            }
        }

        // The negative z edge
        cz = block.getZ() - radius - 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, 0, 1).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(0, 0, 1));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(0, 0, 1).setType(Material.WATER);
                    }
                }
            }
        }

        // The positive z edge
        cz = block.getZ() + radius + 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, 0, -1).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(0, 0, -1));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(0, 0, -1).setType(Material.WATER);
                    }
                }
            }
        }
    }

    private int radius;
    private boolean circularRadius;
    private boolean redstone;
    private boolean includeWet;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "radius", "The maximum radius of the sponge.");
        radius = config.getInt(path + "radius", 5);

        config.setComment(path + "circular-radius", "Whether the radius should be circular or square.");
        circularRadius = config.getBoolean(path + "circular-radius", true);

        config.setComment(path + "require-redstone", "Whether to require redstone to suck up water or not.");
        redstone = config.getBoolean(path + "require-redstone", false);

        config.setComment(path + "include-wet", "Whether to include wet sponges or not.");
        includeWet = config.getBoolean(path + "include-wet", false);
    }
}
