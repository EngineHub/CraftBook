package com.sk89q.craftbook.mech;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;

public class Sponge extends AbstractCraftBookMechanic {

    int radius;

    @Override
    public boolean enable() {

        radius = CraftBookPlugin.inst().getConfiguration().spongeRadius;
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFromTo(BlockFromToEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.getBlock().getType() != Material.WATER && event.getBlock().getType() != Material.STATIONARY_WATER) return;

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cy = -radius; cy <= radius; cy++) {
                for (int cz = -radius; cz <= radius; cz++) {
                    Block sponge = event.getToBlock().getRelative(cx, cy, cz);
                    if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(sponge.getLocation(), event.getToBlock().getLocation(), radius)) continue;
                    if(CraftBookPlugin.inst().getConfiguration().spongeRedstone && !sponge.isBlockIndirectlyPowered()) continue;
                    if(sponge.getType() == Material.SPONGE) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.getBlock().getType() != Material.SPONGE) return;

        if(CraftBookPlugin.inst().getConfiguration().spongeRedstone && !event.getBlock().isBlockIndirectlyPowered()) return;

        removeWater(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.getBlock().getType() != Material.SPONGE) return;

        if(CraftBookPlugin.inst().getConfiguration().spongeRedstone && !event.getBlock().isBlockIndirectlyPowered()) return;

        addWater(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!CraftBookPlugin.inst().getConfiguration().spongeRedstone) return;
        if(event.getBlock().getType() == Material.SPONGE) return;

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
                    if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius)) continue;
                    if(water.getType() == Material.WATER || water.getType() == Material.STATIONARY_WATER) {
                        water.setType(Material.AIR);
                    }
                }
            }
        }
    }

    public void addWater(Block block) {

        // The negative x edge
        int cx = block.getX() - radius - 1;
        for (int cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
            for (int cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER || water.getType() == Material.STATIONARY_WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(1, 0, 0).getType()))
                        water.getRelative(1, 0, 0).setType(Material.WATER);
                }
            }
        }

        // The positive x edge
        cx = block.getX() + radius + 1;
        for (int cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
            for (int cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER || water.getType() == Material.STATIONARY_WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(-1, 0, 0).getType()))
                        water.getRelative(-1, 0, 0).setType(Material.WATER);
                }
            }
        }

        // The negative y edge
        int cy = block.getY() - radius - 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (int cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER || water.getType() == Material.STATIONARY_WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, 1, 0).getType()))
                        water.getRelative(0, 1, 0).setType(Material.WATER);
                }
            }
        }

        // The positive y edge
        cy = block.getY() + radius + 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (int cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER || water.getType() == Material.STATIONARY_WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, -1, 0).getType()))
                        water.getRelative(0, -1, 0).setType(Material.WATER);
                }
            }
        }

        // The negative z edge
        int cz = block.getZ() - radius - 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER || water.getType() == Material.STATIONARY_WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, 0, 1).getType()))
                        water.getRelative(0, 0, 1).setType(Material.WATER);
                }
            }
        }

        // The positive z edge
        cz = block.getZ() + radius + 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER || water.getType() == Material.STATIONARY_WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, 0, -1).getType()))
                        water.getRelative(0, 0, -1).setType(Material.WATER);
                }
            }
        }
    }
}