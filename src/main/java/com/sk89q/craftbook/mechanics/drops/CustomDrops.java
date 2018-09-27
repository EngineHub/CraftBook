package com.sk89q.craftbook.mechanics.drops;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.drops.rewards.DropReward;
import com.sk89q.craftbook.mechanics.drops.rewards.MonetaryDropReward;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.TernaryState;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Biome;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CustomDrops extends AbstractCraftBookMechanic {

    private YAMLProcessor config;

    private Set<CustomDropDefinition> definitions;

    @Override
    public boolean enable() {

        definitions = new LinkedHashSet<>();

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
            CraftBookBukkitUtil.printStacktrace(e);
            return false;
        }

        for(String key : config.getKeys("custom-drops")) {

            String type = config.getString("custom-drops." + key + ".type");

            boolean append = config.getBoolean("custom-drops." + key + ".append", false);
            TernaryState silkTouch = TernaryState.getFromString(config.getString("custom-drops." + key + ".silk-touch", "none"));
            List<String> regions = config.getStringList("custom-drops." + key + ".regions", null);
            List<String> requiredItems = config.getStringList("custom-drops." + key + ".required-items", null);
            List<String> biomeStrings = config.getStringList("custom-drops." + key + ".biomes", null);

            List<DropItemStack> drops = new ArrayList<>();

            for(String drop : config.getKeys("custom-drops." + key + ".drops")) {

                ItemStack item = ItemUtil.makeItemValid(ItemSyntax.getItem(config.getString("custom-drops." + key + ".drops." + drop + ".item")));

                if(item == null) continue; //Invalid Drop.

                DropItemStack stack = new DropItemStack(item);

                stack.setName(drop);

                stack.setChance(config.getDouble("custom-drops." + key + ".drops." + drop + ".chance", 100d));
                stack.setMinimum(config.getInt("custom-drops." + key + ".drops." + drop + ".minimum-amount", -1));
                stack.setMaximum(config.getInt("custom-drops." + key + ".drops." + drop + ".maximum-amount", -1));

                drops.add(stack);
            }

            List<DropReward> rewards = new ArrayList<>();

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
                BlockStateHolder data = BlockSyntax.getBlock(config.getString("custom-drops." + key + ".block"), true);

                def = new BlockCustomDropDefinition(key, drops, rewards, silkTouch, data);
            }

            if(def != null) {
                def.setAppend(append);
                if (regions != null) {
                    def.setRegions(regions);
                }
                def.setPermissionNode(config.getString("custom-drops." + key + ".permission-node", null));
                if (requiredItems != null) {
                    List<ItemStack> items = new ArrayList<>();
                    for (String requiredItem : requiredItems) {
                        items.add(ItemSyntax.getItem(requiredItem));
                    }
                    def.setItems(items);
                }
                if (biomeStrings != null && !biomeStrings.isEmpty()) {
                    List<Biome> biomes = new ArrayList<>();
                    for (String biomeString : biomeStrings) {
                        try {
                            Biome biome = Biome.valueOf(biomeString);
                            biomes.add(biome);
                        } catch (IllegalArgumentException e) {
                            CraftBookPlugin.logger().warning("Tried to assign invalid biome " + biomeString + " to custom drop!");
                        }
                    }
                    def.setBiomes(biomes);
                }
                definitions.add(def);
            }
        }

        return true;
    }

    public void save() {

        for(CustomDropDefinition def : definitions) {

            config.setProperty("custom-drops." + def.getName() + ".append", def.getAppend());
            config.setProperty("custom-drops." + def.getName() + ".silk-touch", def.getSilkTouch().toString());
            if (def.getPermissionNode() != null)
                config.setProperty("custom-drops." + def.getName() + ".permission-node", def.getPermissionNode());
            if (def.getRegions() != null)
                config.setProperty("custom-drops." + def.getName() + ".regions", def.getRegions());
            if (def.getItems() != null) {
                List<String> itemsList = new ArrayList<>();
                for (ItemStack itemStack : def.getItems()) {
                    itemsList.add(ItemSyntax.getStringFromItem(itemStack));
                }
                config.setProperty("custom-drops." + def.getName() + ".required-items", itemsList);
            }
            if (def.getBiomes() != null) {
                List<String> biomeStringList = new ArrayList<>();
                for (Biome biome : def.getBiomes()) {
                    biomeStringList.add(biome.name());
                }
                config.setProperty("custom-drops." + def.getName() + ".biomes", biomeStringList);
            }

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

        for(CustomDropDefinition def : definitions) {
            if(!(def instanceof BlockCustomDropDefinition)) continue; //Nope, we only want block drop definitions.

            if(!((BlockCustomDropDefinition) def).getBlockType().equalsFuzzy(BukkitAdapter.adapt(event.getBlock().getBlockData()))) continue;

            if (def.getPermissionNode() != null && !CraftBookPlugin.inst().wrapPlayer(event.getPlayer()).hasPermission(def.getPermissionNode())) {
                return;
            }

            if (def.getRegions() != null) {
                boolean found = false;
                for (String region : def.getRegions()) {
                    ProtectedRegion r = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(event.getBlock().getWorld())).getRegion(region);
                    if (r != null && r.contains(event.getBlock().getX(), event.getBlock().getY(),
                            event.getBlock().getZ())) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    continue;
            }

            if (def.getBiomes() != null) {
                boolean found = false;

                for (Biome biome : def.getBiomes()) {
                    if (event.getBlock().getBiome().equals(biome)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    continue;
                }
            }

            if (def.getItems() != null) {
                boolean found = false;

                for (ItemStack item : def.getItems()) {
                    if (ItemUtil.areItemsIdentical(event.getPlayer().getItemInHand(), item)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    continue;
                }
            }

            boolean isSilkTouch = event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
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

        if (removeVanillaDrops) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {

        if(!EventUtil.passesFilter(event))
            return;

        for(CustomDropDefinition def : definitions) {
            if(!(def instanceof EntityCustomDropDefinition)) continue; //Nope, we only want entity drop definitions.

            if(!((EntityCustomDropDefinition) def).getEntityType().equals(event.getEntityType())) continue;

            if (def.getPermissionNode() != null) {
                if (event.getEntity().getKiller() == null
                        || !CraftBookPlugin.inst().wrapPlayer(event.getEntity().getKiller()).hasPermission(def.getPermissionNode()))
                    return;
            }

            if (def.getRegions() != null) {
                boolean found = false;
                for (String region : def.getRegions()) {
                    ProtectedRegion r = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(event.getEntity().getWorld())).getRegion(region);
                    if (r != null && r.contains(event.getEntity().getLocation().getBlockX(),
                            event.getEntity().getLocation().getBlockY(),
                            event.getEntity().getLocation().getBlockZ())) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    continue;
            }

            if (def.getBiomes() != null) {
                boolean found = false;

                for (Biome biome : def.getBiomes()) {
                    if (event.getEntity().getLocation().getBlock().getBiome().equals(biome)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    continue;
                }
            }

            Player killer = event.getEntity().getKiller();

            if (def.getItems() != null) {
                boolean found = false;

                if (killer != null) {
                    for (ItemStack item : def.getItems()) {
                        if (ItemUtil.areItemsIdentical(killer.getInventory().getItemInMainHand(), item)) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    continue;
                }
            }

            boolean isSilkTouch = killer != null && killer.getInventory().getItemInMainHand() != null && killer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
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

            for(DropReward reward : def.getRewards()) {
                if(killer == null && reward.doesRequirePlayer()) continue;
                reward.giveReward(killer);
            }
        }

        if (removeVanillaDrops) {
            event.getDrops().clear();
        }
    }

    private boolean customDropPermissions;
    private boolean removeVanillaDrops;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "require-permissions", "Require a permission node to get custom drops.");
        customDropPermissions = config.getBoolean(path + "require-permissions", false);

        config.setComment(path + "remove-vanilla-drops", "Remove all vanilla drops.");
        removeVanillaDrops = config.getBoolean(path + "remove-vanilla-drops", false);
    }
}