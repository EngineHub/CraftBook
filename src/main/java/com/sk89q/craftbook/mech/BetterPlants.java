package com.sk89q.craftbook.mech;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockUtil;

public class BetterPlants extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPhysicsUpdate(BlockPhysicsEvent event) {

        if(CraftBookPlugin.inst().getConfiguration().betterPlantsFernFarming && event.getBlock().getType() == Material.LONG_GRASS && event.getBlock().getData() == 0x2 && CraftBookPlugin.inst().getRandom().nextInt(50) == 0) {
            event.getBlock().setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 3, false);
            event.getBlock().getRelative(0, 1, 0).setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 11, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {

        if(CraftBookPlugin.inst().getConfiguration().betterPlantsFernFarming && event.getBlock().getType() == Material.DOUBLE_PLANT && event.getBlock().getData() >= 0x8) {
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {
                @Override
                public void run () {
                    event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), new ItemStack(Material.LONG_GRASS, 1, (short) 2));
                    event.getBlock().getRelative(0, -1, 0).setTypeIdAndData(Material.LONG_GRASS.getId(), (byte) 2, true);
                }
            }, 2L);
        }
    }
}