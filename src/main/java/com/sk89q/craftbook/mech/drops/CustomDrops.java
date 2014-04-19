package com.sk89q.craftbook.mech.drops;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class CustomDrops extends AbstractCraftBookMechanic {

    private YAMLProcessor config;

    private Set<CustomDropDefinition> definitions;

    @Override
    public boolean enable() {

        definitions = new HashSet<CustomDropDefinition>();

        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "custom-drops.yml"), "custom-drops.yml");
        config = new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "custom-drops.yml"), false, YAMLFormat.EXTENDED);

        load();

        return true;
    }

    public boolean load() {

        definitions.clear();

        try {
            config.load();
        } catch (IOException e) {
            BukkitUtil.printStacktrace(e);
            return false;
        }

        for(String key : config.getKeys("custom-drops")) {

            String type = config.getString("custom-drops." + key + ".type");

            boolean append = config.getBoolean("custom-drops." + key + ".append", false);

            List<DropItemStack> drops = new ArrayList<DropItemStack>();

            for(String drop : config.getKeys("custom-drops." + key + ".drops")) {

                ItemStack item = ItemSyntax.getItem(config.getString("custom-drops." + key + ".drops." + drop + ".item"));

                DropItemStack stack = new DropItemStack(item);

                stack.setName(drop);

                stack.setChance(config.getInt("custom-drops." + key + ".drops." + drop + ".chance", 100));
                stack.setMinimum(config.getInt("custom-drops." + key + ".drops." + drop + ".minimum-amount", -1));
                stack.setMaximum(config.getInt("custom-drops." + key + ".drops." + drop + ".maximum-amount", -1));

                drops.add(stack);
            }

            CustomDropDefinition def = null;
            if(type.equalsIgnoreCase("entity") || type.equalsIgnoreCase("mob")) {

                EntityType ent = EntityType.valueOf(config.getString("custom-drops." + key + ".entity-type"));

                def = new EntityCustomDropDefinition(key, drops, ent);
            } else if(type.equalsIgnoreCase("block")) {

                ItemInfo data = new ItemInfo(config.getString("custom-drops." + key + ".block"));

                def = new BlockCustomDropDefinition(key, drops, data);
            }

            if(def != null) {
                def.setAppend(append);
                definitions.add(def);
            }
        }

        return true;
    }

    public void save() {

        for(CustomDropDefinition def : definitions) {

            config.setProperty("custom-drops." + def.getName() + ".append", def.getAppend());

            int i = 0;
            for(DropItemStack stack : def.getDrops()) {

                String stackName = stack.getName();
                if(stackName == null)
                    stackName = "Drop" + i++;

                config.setProperty("custom-drops." + def.getName() + ".drops." + stackName + ".item", ItemSyntax.getStringFromItem(stack.getStack()));
                config.setProperty("custom-drops." + def.getName() + ".drops." + stackName + ".chance", stack.getChance());
                config.setProperty("custom-drops." + def.getName() + ".drops." + stackName + ".minimum-amount", stack.getMinimum());
                config.setProperty("custom-drops." + def.getName() + ".drops." + stackName + ".maximum-amount", stack.getMaximum());
            }

            if(def instanceof EntityCustomDropDefinition) {
                config.setProperty("custom-drops." + def.getName() + ".type", "ENTITY");
                config.setProperty("custom-drops." + def.getName() + ".entity-type", ((EntityCustomDropDefinition) def).getEntityType().name());
            } else if(def instanceof BlockCustomDropDefinition) {
                config.setProperty("custom-drops." + def.getName() + ".type", "BLOCK");
                config.setProperty("custom-drops." + def.getName() + ".block", ((BlockCustomDropDefinition) def).getBlockType().toString());
            }
        }

        config.save();
    }

    public void addDefinition(CustomDropDefinition definition) {

        definitions.add(definition);
        save();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if (CraftBookPlugin.inst().getConfiguration().customDropPermissions && !CraftBookPlugin.inst().wrapPlayer(event.getPlayer()).hasPermission("craftbook.mech.drops")) return;

        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) //Don't drop in creative.
            return;

        if(!EventUtil.passesFilter(event))
            return;

        for(CustomDropDefinition def : definitions) {
            if(!(def instanceof BlockCustomDropDefinition)) continue; //Nope, we only want block drop definitions.

            if(!((BlockCustomDropDefinition) def).getBlockType().isSame(event.getBlock())) continue;

            if(!ProtectionUtil.canBuild(event.getPlayer(), event.getBlock().getLocation(), false))
                return;

            if(!def.getAppend()) {
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                if(event.getExpToDrop() > 0)
                    ((ExperienceOrb) event.getBlock().getWorld().spawnEntity(BlockUtil.getBlockCentre(event.getBlock()), EntityType.EXPERIENCE_ORB)).setExperience(event.getExpToDrop());
            }

            for(ItemStack stack : def.getRandomDrops()) {
                event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), stack);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {

        if(!EventUtil.passesFilter(event))
            return;

        for(CustomDropDefinition def : definitions) {
            if(!(def instanceof EntityCustomDropDefinition)) continue; //Nope, we only want entity drop definitions.

            if(!((EntityCustomDropDefinition) def).getEntityType().equals(event.getEntityType())) continue;

            if(!def.getAppend()) {
                event.getDrops().clear();
                if(event.getDroppedExp() > 0)
                    ((ExperienceOrb) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.EXPERIENCE_ORB)).setExperience(event.getDroppedExp());
            }

            for(ItemStack stack : def.getRandomDrops()) {
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), stack);
            }
        }
    }
}