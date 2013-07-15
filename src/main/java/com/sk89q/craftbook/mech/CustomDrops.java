package com.sk89q.craftbook.mech;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.MechanicListenerAdapter;
import com.sk89q.craftbook.util.ItemUtil;

public class CustomDrops implements Listener {

    private CraftBookPlugin plugin = CraftBookPlugin.inst();
    public CustomDropManager customDrops;

    public CustomDrops() {
        customDrops = new CustomDropManager(CraftBookPlugin.inst().getDataFolder());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleCustomBlockDrops(BlockBreakEvent event) {

        if(MechanicListenerAdapter.shouldIgnoreEvent(event))
            return;
        if (plugin.getConfiguration().customDropPermissions
                && !plugin.wrapPlayer(event.getPlayer()).hasPermission("craftbook.mech.drops")) return;

        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) //Don't drop in creative.
            return;

        int id = event.getBlock().getTypeId();
        byte data = event.getBlock().getData();

        CustomDropManager.CustomItemDrop drop = customDrops.getBlockDrops(id);

        if (drop != null) {
            CustomDropManager.DropDefinition[] drops = drop.getDrop(data);
            if (drops != null) {
                Location l = event.getBlock().getLocation();
                World w = event.getBlock().getWorld();
                // Add the custom drops
                for (CustomDropManager.DropDefinition dropDefinition : drops) {
                    ItemStack stack = dropDefinition.getItemStack();
                    if (ItemUtil.isStackValid(stack)) {
                        w.dropItemNaturally(l, stack);
                    }
                }

                if (!drops[0].append) {
                    event.getBlock().setTypeId(0);
                    event.setCancelled(true);
                    ((ExperienceOrb) event.getBlock().getWorld().spawnEntity(l, EntityType.EXPERIENCE_ORB)).setExperience(event.getExpToDrop());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleCustomMobDrops(EntityDeathEvent event) {

        EntityType entityType = event.getEntityType();
        if (entityType == null || !entityType.isAlive() || entityType.equals(EntityType.PLAYER)) return;
        CustomDropManager.DropDefinition[] drops = customDrops.getMobDrop(entityType
                .getName());
        if (drops != null) {
            if (!drops[0].append) {
                event.getDrops().clear();
                ((ExperienceOrb) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.EXPERIENCE_ORB)).setExperience(event.getDroppedExp());
            }
            // Add the custom drops
            for (CustomDropManager.DropDefinition dropDefinition : drops) {
                ItemStack stack = dropDefinition.getItemStack();
                if (ItemUtil.isStackValid(stack)) {
                    event.getDrops().add(stack);
                }
            }
        }
    }
}