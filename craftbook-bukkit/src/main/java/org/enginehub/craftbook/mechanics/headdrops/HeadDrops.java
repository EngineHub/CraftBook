/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.headdrops;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import io.papermc.lib.PaperLib;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.util.EventUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

public class HeadDrops extends AbstractCraftBookMechanic {

    private static final Map<EntityType, PlayerProfile> TEXTURE_MAP = Maps.newEnumMap(EntityType.class);

    static {
        SkinData.addDefaultSkinData(TEXTURE_MAP);
    }

    private NamespacedKey headDropsEntityKey;

    @Override
    public void enable() {
        this.headDropsEntityKey = new NamespacedKey(CraftBookPlugin.inst(), "head_drops_entity");

        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
            "headdrops",
            Lists.newArrayList(),
            "CraftBook HeadDrops Commands",
            (commandManager, registration) -> HeadDropsCommands.register(commandManager, registration, this)
        );
    }

    @Override
    public void disable() {
        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.unregisterTopLevel("headdrops");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (playerKillsOnly && event.getEntity().getKiller() == null) {
            return;
        }

        if (event.getEntity().getKiller() != null && !event.getEntity().getKiller().hasPermission("craftbook.headdrops.drops")) {
            return;
        }

        //noinspection deprecation
        if (event.getEntityType().getName() == null) {
            return;
        }
        NamespacedKey typeName = event.getEntityType().getKey();

        double chance = Math.min(1, dropRate);
        if (customDropRates.containsKey(typeName)) {
            chance = Math.min(1, customDropRates.get(typeName));
        }

        if (event.getEntity().getKiller() != null && event.getEntity().getKiller().getInventory().getItemInMainHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
            chance = Math.min(1, chance + lootingModifier * event.getEntity().getKiller().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS));
        }

        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }

        ItemStack toDrop = null;

        if (event.getEntityType() == EntityType.PLAYER && enablePlayers) {
            PlayerProfile playerProfile = ((Player) event.getEntity()).getPlayerProfile();
            toDrop = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) toDrop.getItemMeta();
            meta.setPlayerProfile(playerProfile);
            meta.setDisplayName(ChatColor.RESET + playerProfile.getName() + "'s Head");
            toDrop.setItemMeta(meta);
        } else if (enableMobs) {
            if (overrideNatural) {
                switch (event.getEntityType()) {
                    case ZOMBIE:
                    case GIANT:
                        toDrop = new ItemStack(Material.ZOMBIE_HEAD, 1);
                        break;
                    case CREEPER:
                        toDrop = new ItemStack(Material.CREEPER_HEAD, 1);
                        break;
                    case SKELETON:
                        toDrop = new ItemStack(Material.SKELETON_SKULL, 1);
                        break;
                    case WITHER_SKELETON:
                        toDrop = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
                        break;
                    case ENDER_DRAGON:
                        toDrop = new ItemStack(Material.DRAGON_HEAD, 1);
                        break;
                }

                // Fall through here, as we still allow custom overrides.
            }

            ItemStack newStack = createFromEntityType(event.getEntityType());
            if (newStack != null) {
                toDrop = newStack;
            }
        } else {
            return;
        }

        if (toDrop != null) {
            event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), toDrop);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!nameOnClick || !EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            // This should not be possible.
            return;
        }

        Material clickedType = clicked.getType();

        if (clickedType == Material.PLAYER_HEAD || clickedType == Material.PLAYER_WALL_HEAD) {
            Skull skull = (Skull) PaperLib.getBlockState(clicked, false).getState();

            CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

            if (skull.getPersistentDataContainer().has(headDropsEntityKey, PersistentDataType.STRING)) {
                String entityTypeId = skull.getPersistentDataContainer().get(headDropsEntityKey, PersistentDataType.STRING);
                EntityType entityType = Registry.ENTITY_TYPE.get(parseKey(Objects.requireNonNull(entityTypeId)));
                //noinspection deprecation
                if (entityType == null || entityType.getName() == null) {
                    return;
                }

                player.printInfo(TranslatableComponent.of("craftbook.headdrops.click-message.mob", TextComponent.of(WordUtils.capitalize(entityType.getKey().getKey().replace("_", " ")))));
            } else {
                PlayerProfile profile = skull.getPlayerProfile();
                if (profile == null || profile.getName() == null || profile.getName().equals(SkinData.HEAD_NAME)) {
                    return;
                }

                player.printInfo(TranslatableComponent.of("craftbook.headdrops.click-message.player", TextComponent.of(profile.getName())));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getItemInHand().getType() != Material.PLAYER_HEAD) {
            return;
        }

        ItemMeta itemMeta = event.getItemInHand().getItemMeta();
        if (!itemMeta.getPersistentDataContainer().has(headDropsEntityKey, PersistentDataType.STRING)) {
            // Can't persist data if it doesn't exist.
            return;
        }

        String existingData = itemMeta.getPersistentDataContainer().get(headDropsEntityKey, PersistentDataType.STRING);

        Skull state = (Skull) PaperLib.getBlockState(event.getBlockPlaced(), false).getState();
        state.getPersistentDataContainer().set(
            headDropsEntityKey,
            PersistentDataType.STRING,
            Objects.requireNonNull(existingData)
        );
        state.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        Block block = event.getBlock();
        Material blockType = block.getType();

        if (blockType == Material.PLAYER_HEAD || blockType == Material.PLAYER_WALL_HEAD) {
            Skull skull = (Skull) PaperLib.getBlockState(block, false).getState();
            if (!skull.getPersistentDataContainer().has(headDropsEntityKey, PersistentDataType.STRING)) {
                return;
            }

            String entityTypeId = Objects.requireNonNull(skull.getPersistentDataContainer().get(headDropsEntityKey, PersistentDataType.STRING));
            EntityType type = Registry.ENTITY_TYPE.get(parseKey(entityTypeId));

            //noinspection deprecation
            if (type == null || type.getName() == null) {
                return;
            }

            ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) stack.getItemMeta();
            meta.getPersistentDataContainer().set(headDropsEntityKey, PersistentDataType.STRING, entityTypeId);
            meta.setDisplayName(ChatColor.RESET + WordUtils.capitalize(type.getKey().getKey().replace("_", " ")) + " Head");
            stack.setItemMeta(meta);

            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().toCenterLocation(), stack);
        }
    }

    @Nullable
    protected ItemStack createFromEntityType(EntityType entityType) {
        PlayerProfile profile = TEXTURE_MAP.get(entityType);
        NamespacedKey entityKey = entityType.getKey();
        if (customSkins.containsKey(entityKey)) {
            profile = customSkins.get(entityKey);
        }
        if (profile != null) {
            ItemStack toDrop = new ItemStack(Material.PLAYER_HEAD, 1);
            ItemMeta itemMeta = toDrop.getItemMeta();
            if (itemMeta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) itemMeta;
                skullMeta.setDisplayName(ChatColor.RESET + WordUtils.capitalize(entityKey.getKey().replace("_", " ")) + " Head");
                skullMeta.setPlayerProfile(profile);
                skullMeta.getPersistentDataContainer().set(headDropsEntityKey, PersistentDataType.STRING, entityKey.toString());
                toDrop.setItemMeta(skullMeta);
            } else {
                CraftBook.logger.warn("Spigot has failed to set a HeadDrop item to a head!");
            }

            return toDrop;
        } else {
            return null;
        }
    }

    private NamespacedKey parseKey(String name) {
        if (name.contains(":")) {
            String[] nameParts = name.split(":");
            //noinspection deprecation
            return new NamespacedKey(nameParts[0], nameParts[1]);
        } else {
            return NamespacedKey.minecraft(name);
        }
    }

    private boolean enableMobs;
    private boolean enablePlayers;
    private boolean playerKillsOnly;
    private boolean overrideNatural;
    private double dropRate;
    private double lootingModifier;
    private boolean nameOnClick;
    private HashMap<NamespacedKey, Double> customDropRates;
    private HashMap<NamespacedKey, PlayerProfile> customSkins;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("drop-mob-heads", "Whether mobs should drop their heads when killed.");
        enableMobs = config.getBoolean("drop-mob-heads", true);

        config.setComment("drop-player-heads", "Whether players should drop their heads when killed.");
        enablePlayers = config.getBoolean("drop-player-heads", true);

        config.setComment("require-player-killer", "Only drop heads when killed by a player. (Allows requiring permission)");
        playerKillsOnly = config.getBoolean("require-player-killer", true);

        config.setComment("override-natural-head-drops", "Override natural head drops, this will cause natural head drops to use the chances provided by CraftBook. (Eg, Wither Skeleton Heads)");
        overrideNatural = config.getBoolean("override-natural-head-drops", false);

        config.setComment("drop-rate", "A value between 1 and 0 which dictates the global chance of heads being dropped. This can be overridden per-entity type.");
        dropRate = config.getDouble("drop-rate", 0.05);

        config.setComment("looting-rate-modifier", "This amount is added to the chance for every looting level on an item. Eg, a chance of 0.05(5%) and a looting mod of 0.05(5%) on a looting 3 sword, would give a 0.20 chance (20%).");
        lootingModifier = config.getDouble("looting-rate-modifier", 0.05);

        config.setComment("show-name-right-click", "When enabled, right clicking a placed head will say the owner of the head.");
        nameOnClick = config.getBoolean("show-name-right-click", true);

        config.setComment("drop-rates", "A list of custom drop rates for different mobs");
        customDropRates = new HashMap<>();
        if (config.getKeys("drop-rates") != null) {
            for (String key : config.getKeys("drop-rates")) {
                customDropRates.put(parseKey(key), config.getDouble("drop-rates." + key));
            }
        } else {
            config.addNode("drop-rates");
        }

        config.setComment("custom-skins", "A list of custom skins for different mobs");
        customSkins = new HashMap<>();
        if (config.getKeys("custom-skins") != null) {
            for (String key : config.getKeys("custom-skins")) {
                customSkins.put(parseKey(key), SkinData.createProfile(config.getString("custom-skins." + key)));
            }
        } else {
            config.addNode("custom-skins");
        }
    }
}
