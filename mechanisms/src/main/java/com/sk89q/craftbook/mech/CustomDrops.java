package com.sk89q.craftbook.mech;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

public class CustomDrops extends MechanismsPlugin implements Listener{

    MechanismsPlugin plugin;

    public CustomDrops(MechanismsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleCustomBlockDrops(BlockBreakEvent event) {
        if(plugin.getLocalConfiguration().customDropSettings.requirePermissions &&
                !event.getPlayer().hasPermission("craftbook.mech.drops")) return;
        if(event.isCancelled()) return;
        int id = event.getBlock().getTypeId();
        byte data = event.getBlock().getData();
        CustomDropManager.CustomItemDrop drop = plugin.getLocalConfiguration().customDrops.getBlockDrops(id);
        if(drop!=null) {
            CustomDropManager.DropDefinition[] drops = drop.getDrop(data);
            if(drops!=null) {
                Location l = event.getBlock().getLocation();
                World w = event.getBlock().getWorld();
                for(CustomDropManager.DropDefinition d : drops) {
                    ItemStack i = d.createStack();
                    int count = i.getAmount();
                    i.setAmount(1);
                    for(;count!=0;count--) {
                        w.dropItemNaturally(l, i);
                    }
                }

                //TODO Totally incorrect, because of some block's special behaviors. Fix later.
                event.getBlock().setTypeId(0);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void handleCustomMobDrops(EntityDeathEvent event) {
        EntityType entityType = event.getEntityType();
        if(entityType==null || entityType == EntityType.PLAYER) return;
        CustomDropManager.DropDefinition[] drops =
                plugin.getLocalConfiguration().customDrops.getMobDrop(entityType.getName());
        if(drops!=null) {
            event.getDrops().clear();
            for(CustomDropManager.DropDefinition d : drops) {
                ItemStack i = d.createStack();
                int count = i.getAmount();
                i.setAmount(1);
                for(;count!=0;count--) {
                    event.getDrops().add(i);
                }
            }
        }
    }
}
