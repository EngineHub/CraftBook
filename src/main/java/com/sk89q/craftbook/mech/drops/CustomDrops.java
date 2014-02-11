package com.sk89q.craftbook.mech.drops;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class CustomDrops extends AbstractCraftBookMechanic {

    //TODO finish

    private YAMLProcessor config;

    private Set<CustomDropDefinition> definitions;

    @Override
    public boolean enable() {

        definitions = new HashSet<CustomDropDefinition>();

        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "custom-drops.yml"), "custom-drops.yml");
        config = new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "custom-drops.yml"), false, YAMLFormat.EXTENDED);

        try {
            config.load();
        } catch (IOException e) {
            BukkitUtil.printStacktrace(e);
            return false;
        }

        for(String key : config.getKeys("custom-drops")) {

            String type = config.getString(key + ".type");

            List<DropItemStack> drops = new ArrayList<DropItemStack>();

            CustomDropDefinition def = null;
            if(type.equalsIgnoreCase("entity") || type.equalsIgnoreCase("mob")) {

                EntityType ent = EntityType.valueOf(config.getString(key + ".entity-type"));

                def = new EntityCustomDropDefinition(drops, ent);
            } else if(type.equalsIgnoreCase("block")) {

                ItemInfo data = new ItemInfo(ItemSyntax.getItem(config.getString(key + ".block")));

                def = new BlockCustomDropDefinition(drops, data);
            }

            if(def != null)
                definitions.add(def);
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        for(CustomDropDefinition def : definitions) {
            if(!(def instanceof BlockCustomDropDefinition)) continue; //Nope, we only want block drop definitions.

            if(!((BlockCustomDropDefinition) def).getBlockType().isSame(event.getBlock())) continue;

            if(!def.getAppend()) {
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                ((ExperienceOrb) event.getBlock().getWorld().spawnEntity(BlockUtil.getBlockCentre(event.getBlock()), EntityType.EXPERIENCE_ORB)).setExperience(event.getExpToDrop());
            }

            for(ItemStack stack : def.getRandomDrops()) {
                event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), stack);
            }
        }
    }
}