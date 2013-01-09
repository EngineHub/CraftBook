package com.sk89q.craftbook.mech;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemUtil;

public class CustomDrops implements Listener {

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    public CustomDrops() {

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleCustomBlockDrops(BlockBreakEvent event) {

        if (plugin.getConfiguration().customDropPermissions
                && !plugin.wrapPlayer(event.getPlayer()).hasPermission("craftbook.mech.drops")) return;

        int id = event.getBlock().getTypeId();
        byte data = event.getBlock().getData();

        CustomDropManager.CustomItemDrop drop = plugin.getConfiguration().customDrops.getBlockDrops(id);

        if (drop != null) {
            CustomDropManager.DropDefinition[] drops = drop.getDrop(data);
            if (drops != null) {
                Location l = event.getBlock().getLocation();
                World w = event.getBlock().getWorld();
                // Add the custom drops
                for (CustomDropManager.DropDefinition dropDefinition : drops) {
                    if (ItemUtil.isStackValid(dropDefinition.getItemStack())) {
                        w.dropItemNaturally(l, dropDefinition.getItemStack());
                    }
                }

                if (!drops[0].append) {
                    event.getBlock().setTypeId(0);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleCustomMobDrops(EntityDeathEvent event) {

        EntityType entityType = event.getEntityType();
        if (entityType == null || !entityType.isAlive() || entityType.equals(EntityType.PLAYER)) return;
        CustomDropManager.DropDefinition[] drops = plugin.getConfiguration().customDrops.getMobDrop(entityType
                .getName());
        if (drops != null) {
            if (!drops[0].append) event.getDrops().clear();
            // Add the custom drops
            for (CustomDropManager.DropDefinition dropDefinition : drops) {
                if (ItemUtil.isStackValid(dropDefinition.getItemStack())) {
                    event.getDrops().add(dropDefinition.getItemStack());
                }
            }
        }
    }
}