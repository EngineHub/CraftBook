package com.sk89q.craftbook.mechanics.drops.legacy;

import java.io.File;

import org.bukkit.Bukkit;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.util.yaml.YAMLProcessor;

@Deprecated
public class LegacyCustomDrops extends AbstractCraftBookMechanic {

    public LegacyCustomDropManager customDrops;

    /*@EventHandler(priority = EventPriority.HIGH)
    public void handleCustomBlockDrops(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event))
            return;

        if (CraftBookPlugin.inst().getConfiguration().customDropPermissions && !CraftBookPlugin.inst().wrapPlayer(event.getPlayer()).hasPermission("craftbook.mech.drops")) return;

        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) //Don't drop in creative.
            return;

        int id = event.getBlock().getTypeId();
        byte data = event.getBlock().getData();

        LegacyCustomDropManager.CustomItemDrop drop = customDrops.getBlockDrops(id);

        if (drop != null) {
            LegacyCustomDropManager.DropDefinition[] drops = drop.getDrop(data);
            if (drops != null) {
                Location l = event.getBlock().getLocation();
                World w = event.getBlock().getWorld();
                // Add the custom drops
                for (LegacyCustomDropManager.DropDefinition dropDefinition : drops) {
                    ItemStack stack = dropDefinition.getItemStack();
                    if (ItemUtil.isStackValid(stack))
                        w.dropItemNaturally(l, stack);
                }

                if (!drops[0].append) {
                    event.getBlock().setType(Material.AIR);
                    event.setCancelled(true);
                    ((ExperienceOrb) event.getBlock().getWorld().spawnEntity(l, EntityType.EXPERIENCE_ORB)).setExperience(event.getExpToDrop());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handleCustomMobDrops(EntityDeathEvent event) {

        if(!EventUtil.passesFilter(event))
            return;
        EntityType entityType = event.getEntityType();
        if (entityType == null || !entityType.isAlive() || entityType.equals(EntityType.PLAYER)) return;
        LegacyCustomDropManager.DropDefinition[] drops = customDrops.getMobDrop(event.getEntity());
        if (drops != null) {
            if (!drops[0].append) {
                event.getDrops().clear();
                ((ExperienceOrb) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.EXPERIENCE_ORB)).setExperience(event.getDroppedExp());
            }
            // Add the custom drops
            for (LegacyCustomDropManager.DropDefinition dropDefinition : drops) {
                ItemStack stack = dropDefinition.getItemStack();
                if (ItemUtil.isStackValid(stack))
                    event.getDrops().add(stack);
            }
        }
    }*/

    @Override
    public boolean enable () {

        final File blockDefinitions = new File(CraftBookPlugin.inst().getDataFolder(), "custom-block-drops.txt");
        final File mobDefinitions = new File(CraftBookPlugin.inst().getDataFolder(), "custom-mob-drops.txt");

        if(!blockDefinitions.exists() && !mobDefinitions.exists()) return true;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
            customDrops = new LegacyCustomDropManager(CraftBookPlugin.inst().getDataFolder());
            if(blockDefinitions.exists())
                blockDefinitions.delete();
            if(mobDefinitions.exists())
                mobDefinitions.delete();
        }, 10L);
        return true;
    }

    @Override
    public void disable () {
        customDrops = null;
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}