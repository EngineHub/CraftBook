package com.sk89q.craftbook.mechanics.drops;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mechanics.drops.rewards.DropReward;
import com.sk89q.craftbook.mechanics.drops.rewards.MonetaryDropReward;
import com.sk89q.craftbook.util.*;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            TernaryState silkTouch = TernaryState.getFromString(config.getString("custom-drops." + key + ".silk-touch", "none"));

            List<DropItemStack> drops = new ArrayList<DropItemStack>();

            for(String drop : config.getKeys("custom-drops." + key + ".drops")) {

                ItemStack item = ItemSyntax.getItem(config.getString("custom-drops." + key + ".drops." + drop + ".item"));

                if(item == null) continue; //Invalid Drop.

                DropItemStack stack = new DropItemStack(item);

                stack.setName(drop);

                stack.setChance(config.getInt("custom-drops." + key + ".drops." + drop + ".chance", 100));
                stack.setMinimum(config.getInt("custom-drops." + key + ".drops." + drop + ".minimum-amount", -1));
                stack.setMaximum(config.getInt("custom-drops." + key + ".drops." + drop + ".maximum-amount", -1));

                drops.add(stack);
            }

            List<DropReward> rewards = new ArrayList<DropReward>();

            if(config.getKeys("custom-drops." + key + ".rewards") != null) {
                for(String reward : config.getKeys("custom-drops." + key + ".rewards")) {

                    String rewardType = config.getString("custom-drops." + key + ".rewards." + reward + ".type", "None");

                    DropReward dropReward = null;

                    if(rewardType.equalsIgnoreCase("money")) {

                        double amount = config.getDouble("custom-drops." + key + ".rewards." + reward + ".amount", 10);

                        dropReward = new MonetaryDropReward(reward, amount);
                    }

                    if(dropReward == null) continue;

                    rewards.add(dropReward);
                }
            }

            CustomDropDefinition def = null;
            if(type.equalsIgnoreCase("entity") || type.equalsIgnoreCase("mob")) {

                EntityType ent = EntityType.valueOf(config.getString("custom-drops." + key + ".entity-type"));

                def = new EntityCustomDropDefinition(key, drops, rewards, silkTouch, ent);
            } else if(type.equalsIgnoreCase("block")) {

                ItemInfo data = new ItemInfo(config.getString("custom-drops." + key + ".block"));

                def = new BlockCustomDropDefinition(key, drops, rewards, silkTouch, data);
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
            config.setProperty("custom-drops." + def.getName() + ".silk-touch", def.getSilkTouch().toString());

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

            for(DropReward reward : def.getRewards()) {
                if(reward instanceof MonetaryDropReward) {
                    config.setProperty("custom-drops." + def.getName() + ".rewards." + reward.getName() + ".type", "money");
                    config.setProperty("custom-drops." + def.getName() + ".rewards." + reward.getName() + ".amount", ((MonetaryDropReward) reward).getAmount());
                }
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

        if (customDropPermissions && !CraftBookPlugin.inst().wrapPlayer(event.getPlayer()).hasPermission("craftbook.mech.drops")) return;

        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) //Don't drop in creative.
            return;

        if(!EventUtil.passesFilter(event))
            return;

        if(!ProtectionUtil.canBuild(event.getPlayer(), event.getBlock().getLocation(), false))
            return;

        for(CustomDropDefinition def : definitions) {
            if(!(def instanceof BlockCustomDropDefinition)) continue; //Nope, we only want block drop definitions.

            if(!((BlockCustomDropDefinition) def).getBlockType().isSame(event.getBlock())) continue;

            boolean isSilkTouch = ((Player) event.getPlayer()).getItemInHand() != null && event.getPlayer().getItemInHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
            if(!def.getSilkTouch().doesPass(isSilkTouch))
                continue;

            if(!def.getAppend()) {
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                if(event.getExpToDrop() > 0)
                    ((ExperienceOrb) event.getBlock().getWorld().spawnEntity(BlockUtil.getBlockCentre(event.getBlock()), EntityType.EXPERIENCE_ORB)).setExperience(event.getExpToDrop());
            }

            for(ItemStack stack : def.getRandomDrops()) {
                event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), stack);
            }

            for(DropReward reward : def.getRewards()) {
                reward.giveReward(event.getPlayer());
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

            boolean isSilkTouch = event.getEntity() instanceof Player && ((Player) event.getEntity()).getItemInHand() != null && ((Player) event.getEntity()).getItemInHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
            if(!def.getSilkTouch().doesPass(isSilkTouch))
                continue;

            if(!def.getAppend()) {
                event.getDrops().clear();
                if(event.getDroppedExp() > 0)
                    ((ExperienceOrb) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.EXPERIENCE_ORB)).setExperience(event.getDroppedExp());
            }

            for(ItemStack stack : def.getRandomDrops()) {
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), stack);
            }

            Player killer = event.getEntity().getKiller();

            for(DropReward reward : def.getRewards()) {
                if(killer == null && reward.doesRequirePlayer()) continue;
                reward.giveReward(killer);
            }
        }
    }

    private boolean customDropPermissions;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "require-permissions", "Require a permission node to get custom drops.");
        customDropPermissions = config.getBoolean(path + "require-permissions", false);
    }
}