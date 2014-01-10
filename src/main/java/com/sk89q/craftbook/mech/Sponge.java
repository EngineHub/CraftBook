package com.sk89q.craftbook.mech;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.LocationUtil;

public class Sponge extends AbstractCraftBookMechanic {

    int radius;

    @Override
    public boolean enable() {

        radius = CraftBookPlugin.inst().getConfiguration().spongeRadius;
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockFromTo(BlockFromToEvent event) {

        if(event.getBlock().getType() != Material.WATER && event.getBlock().getType() != Material.STATIONARY_WATER) return;

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cy = -radius; cy <= radius; cy++) {
                for (int cz = -radius; cz <= radius; cz++) {
                    Block sponge = event.getToBlock().getRelative(cx, cy, cz);
                    if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(sponge.getLocation(), event.getBlock().getLocation(), radius)) return;
                    if(CraftBookPlugin.inst().getConfiguration().spongeRedstone && !sponge.isBlockIndirectlyPowered()) return;
                    if(sponge.getType() == Material.SPONGE) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}