/*
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

package org.enginehub.craftbook.bukkit.mechanics.headdrops;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.Maps;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
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
import org.bukkit.event.Listener;
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
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.headdrops.HeadDrops;
import org.enginehub.craftbook.mechanics.headdrops.SkinData;
import org.enginehub.craftbook.util.EventUtil;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class BukkitHeadDrops extends HeadDrops implements Listener {

    private static final Map<EntityType, PlayerProfile> TEXTURE_MAP = Maps.newHashMap();
    protected static final String HEAD_NAME = "cb-headdrops";
    private static final UUID DEFAULT_UUID = UUID.fromString("a233eb4b-4cab-42cd-9fd9-7e7b9a3f74be");

    protected static PlayerProfile createProfile(String texture) {
        PlayerProfile profile = Bukkit.createProfile(DEFAULT_UUID, HEAD_NAME);
        profile.setProperty(new ProfileProperty("textures", texture));

        return profile;
    }

    static {
        // skip if the CRAFTBOOK_DOCGEN environment variable is set
        // This breaks docgen currently.
        if (System.getenv("CRAFTBOOK_DOCGEN") == null) {
            SkinData.addDefaultSkinData((entityType, textureString) -> {
                if (entityType != null) {
                    TEXTURE_MAP.put(BukkitAdapter.adapt(entityType), createProfile(textureString));
                }
            });
        }
    }

    private final NamespacedKey headDropsEntityKey = new NamespacedKey("craftbook", "head_drops_entity");

    public BukkitHeadDrops(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() {
        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
            "headdrops",
            List.of(),
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

        if (event.getEntity().getKiller() != null && event.getEntity().getKiller().getInventory().getItemInMainHand().containsEnchantment(Enchantment.LOOTING)) {
            chance = Math.min(1, chance + lootingModifier * event.getEntity().getKiller().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOTING));
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
            meta.displayName(Component.text(playerProfile.getName() + "'s Head"));
            toDrop.setItemMeta(meta);
        } else if (enableMobs) {
            if (overrideNatural) {
                toDrop = switch (event.getEntityType()) {
                    case ZOMBIE, GIANT -> new ItemStack(Material.ZOMBIE_HEAD, 1);
                    case CREEPER -> new ItemStack(Material.CREEPER_HEAD, 1);
                    case SKELETON -> new ItemStack(Material.SKELETON_SKULL, 1);
                    case WITHER_SKELETON -> new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
                    case ENDER_DRAGON -> new ItemStack(Material.DRAGON_HEAD, 1);
                    case PIGLIN -> new ItemStack(Material.PIGLIN_HEAD, 1);
                    default -> null;
                };

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
            Skull skull = (Skull) clicked.getState(false);

            CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

            if (skull.getPersistentDataContainer().has(headDropsEntityKey, PersistentDataType.STRING)) {
                String entityTypeId = skull.getPersistentDataContainer().get(headDropsEntityKey, PersistentDataType.STRING);
                EntityType entityType = Registry.ENTITY_TYPE.get(parseKey(Objects.requireNonNull(entityTypeId)));
                //noinspection deprecation
                if (entityType == null || entityType.getName() == null) {
                    return;
                }

                try {
                    player.printInfo(TranslatableComponent.of("craftbook.headdrops.click-message.mob", TranslatableComponent.of(entityType.translationKey())));
                } catch (IllegalArgumentException e) {
                    player.printInfo(TranslatableComponent.of("craftbook.headdrops.click-message.mob", TextComponent.of(entityType.getKey().getKey().replace("_", " "))));
                }
            } else {
                PlayerProfile profile = skull.getPlayerProfile();
                if (profile == null || profile.getName() == null || profile.getName().equals(HEAD_NAME)) {
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

        Skull state = (Skull) event.getBlockPlaced().getState(false);
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
            Skull skull = (Skull) block.getState(false);
            if (!skull.getPersistentDataContainer().has(headDropsEntityKey, PersistentDataType.STRING)) {
                return;
            }

            String entityTypeId = Objects.requireNonNull(skull.getPersistentDataContainer().get(headDropsEntityKey, PersistentDataType.STRING));
            EntityType type = Registry.ENTITY_TYPE.get(parseKey(entityTypeId));

            //noinspection deprecation
            if (type == null || type.getName() == null) {
                return;
            }

            PlayerProfile profile = TEXTURE_MAP.get(type);

            if (profile == null) {
                return;
            }

            ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) stack.getItemMeta();
            meta.setPlayerProfile(profile);
            meta.getPersistentDataContainer().set(headDropsEntityKey, PersistentDataType.STRING, entityTypeId);
            meta.displayName(Component.translatable(type.translationKey()).append(Component.text(" Head")).style(Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
            stack.setItemMeta(meta);

            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().toCenterLocation(), stack);
        }
    }

    protected @Nullable ItemStack createFromEntityType(EntityType entityType) {
        PlayerProfile profile = TEXTURE_MAP.get(entityType);
        NamespacedKey entityKey = entityType.getKey();
        if (customSkins.containsKey(entityKey)) {
            profile = customSkins.get(entityKey);
        }
        if (profile != null) {
            ItemStack toDrop = new ItemStack(Material.PLAYER_HEAD, 1);
            ItemMeta itemMeta = toDrop.getItemMeta();
            if (itemMeta instanceof SkullMeta skullMeta) {
                skullMeta.displayName(Component.translatable(entityType.translationKey()).append(Component.text(" Head")).style(Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
                skullMeta.setPlayerProfile(profile);
                skullMeta.getPersistentDataContainer().set(headDropsEntityKey, PersistentDataType.STRING, entityKey.toString());
                toDrop.setItemMeta(skullMeta);
            } else {
                CraftBook.LOGGER.warn("Spigot has failed to set a HeadDrop item to a head!");
            }

            return toDrop;
        } else {
            return null;
        }
    }

    private NamespacedKey parseKey(String name) {
        if (name.contains(":")) {
            String[] nameParts = name.split(":");
            return new NamespacedKey(nameParts[0], nameParts[1]);
        } else {
            return NamespacedKey.minecraft(name);
        }
    }

    private HashMap<NamespacedKey, Double> customDropRates;
    private HashMap<NamespacedKey, PlayerProfile> customSkins;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        super.loadFromConfiguration(config);

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
                customSkins.put(parseKey(key), createProfile(config.getString("custom-skins." + key)));
            }
        } else {
            config.addNode("custom-skins");
        }
    }

    private static final Set<EntityType> IGNORED_ENTITIES = Set.of(
            EntityType.PLAYER, EntityType.ZOMBIE, EntityType.CREEPER,
            EntityType.SKELETON, EntityType.WITHER_SKELETON,
            EntityType.ARMOR_STAND, EntityType.ENDER_DRAGON, EntityType.PIGLIN,
            EntityType.UNKNOWN, EntityType.MANNEQUIN
    );

    @SuppressWarnings("unused")
    private static void printMissingSkins() {
        String missingText = Registry.ENTITY_TYPE.stream()
                .filter(type -> !IGNORED_ENTITIES.contains(type) && type.isAlive())
                .filter(type -> !TEXTURE_MAP.containsKey(type))
                .map(EntityType::getKey)
                .map(NamespacedKey::toString)
                .collect(Collectors.joining(", "));

        if (!missingText.isEmpty()) {
            CraftBook.LOGGER.warn(missingText);
        }
    }
}
