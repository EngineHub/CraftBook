/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics;

import static com.sk89q.craftbook.core.util.documentation.DocumentationGenerator.createStringOfLength;
import static com.sk89q.craftbook.core.util.documentation.DocumentationGenerator.padToLength;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Skull;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Module(moduleId = "headdrops", moduleName = "HeadDrops", onEnable="onInitialize", onDisable="onDisable")
public class HeadDrops extends SpongeMechanic implements DocumentationProvider {

    private static final Pattern HEAD_DROPS_TABLE_PATTERN = Pattern.compile("%CUSTOM_HEAD_TYPES%", Pattern.LITERAL);

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private Map<EntityType, GameProfile> mobSkullMap = new HashMap<>();

    private ConfigValue<Boolean> playerKillsOnly = new ConfigValue<>("player-kills-only", "Only drop heads when mobs are killed by a player.",true);
    private ConfigValue<Double> dropRate = new ConfigValue<>("drop-rate", "Drop chance out of 1.", 0.05, TypeToken.of(Double.class));
    private ConfigValue<Double> lootingRateModifier = new ConfigValue<>("looting-rate-modifier",
            "Added chance for each looting level.", 0.05, TypeToken.of(Double.class));
    private ConfigValue<Boolean> showNameClick = new ConfigValue<>("show-name-on-click", "Show the name of the owner of a head on right click.",true);
    private ConfigValue<Boolean> playerHeads = new ConfigValue<>("player-heads", "Allow players to drop their heads on death.", true);
    private ConfigValue<Boolean> mobHeads = new ConfigValue<>("mob-heads", "Allow mobs to drop their heads on death.", true);

    private SpongePermissionNode killPermission = new SpongePermissionNode("craftbook.headdrops.kill",
            "Allow the player to get a HeadDrop from killing an entity.", PermissionDescription.ROLE_USER);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        killPermission.register();

        playerKillsOnly.load(config);
        dropRate.load(config);
        lootingRateModifier.load(config);
        showNameClick.load(config);
        playerHeads.load(config);
        mobHeads.load(config);

        mobSkullMap.clear();

        // Official or Guaranteed Static - Vanilla
        mobSkullMap.put(EntityTypes.BLAZE, GameProfile.of(UUID.fromString("4c38ed11-596a-4fd4-ab1d-26f386c1cbac"), "MHF_Blaze"));
        mobSkullMap.put(EntityTypes.CAVE_SPIDER, GameProfile.of(UUID.fromString("cab28771-f0cd-4fe7-b129-02c69eba79a5"), "MHF_CaveSpider"));
        mobSkullMap.put(EntityTypes.CHICKEN, GameProfile.of(UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"), "MHF_Chicken"));
        mobSkullMap.put(EntityTypes.COW, GameProfile.of(UUID.fromString("f159b274-c22e-4340-b7c1-52abde147713"), "MHF_Cow"));
        mobSkullMap.put(EntityTypes.ENDERMAN, GameProfile.of(UUID.fromString("40ffb372-12f6-4678-b3f2-2176bf56dd4b"), "MHF_Enderman"));
        mobSkullMap.put(EntityTypes.ENDERMITE, GameProfile.of(UUID.fromString("3df6a050-b93e-4d8b-8fa4-b5228a797b84"), "MHF_Endermite"));
        mobSkullMap.put(EntityTypes.ENDER_DRAGON, GameProfile.of(UUID.fromString("bd3802bb-be48-438c-bafb-cb9510e2aa2d"), "MHF_EnderDragon"));
        mobSkullMap.put(EntityTypes.GHAST, GameProfile.of(UUID.fromString("063085a6-797f-4785-be1a-21cd7580f752"), "MHF_Ghast"));
        mobSkullMap.put(EntityTypes.GUARDIAN, GameProfile.of(UUID.fromString("4005cac1-a16a-45aa-9e72-7fb514335717"), "MHF_Guardian"));
        mobSkullMap.put(EntityTypes.IRON_GOLEM, GameProfile.of(UUID.fromString("757f90b2-2344-4b8d-8dac-824232e2cece"), "MHF_Golem"));
        mobSkullMap.put(EntityTypes.MAGMA_CUBE, GameProfile.of(UUID.fromString("0972bdd1-4b86-49fb-9ecc-a353f8491a51"), "MHF_LavaSlime"));
        mobSkullMap.put(EntityTypes.MUSHROOM_COW, GameProfile.of(UUID.fromString("a46817d6-73c5-4f3f-b712-af6b3ff47b96"), "MHF_MushroomCow"));
        mobSkullMap.put(EntityTypes.OCELOT, GameProfile.of(UUID.fromString("1bee9df5-4f71-42a2-bf52-d97970d3fea3"), "MHF_Ocelot"));
        mobSkullMap.put(EntityTypes.PIG, GameProfile.of(UUID.fromString("8b57078b-f1bd-45df-83c4-d88d16768fbe"), "MHF_Pig"));
        mobSkullMap.put(EntityTypes.PIG_ZOMBIE, GameProfile.of(UUID.fromString("18a2bb50-334a-4084-9184-2c380251a24b"), "MHF_PigZombie"));
        mobSkullMap.put(EntityTypes.RABBIT, GameProfile.of(UUID.fromString("fbec11d4-80a7-4c1c-9de3-4136a16f1de0"), "MHF_Rabbit"));
        mobSkullMap.put(EntityTypes.SHEEP, GameProfile.of(UUID.fromString("dfaad551-4e7e-45a1-a6f7-c6fc5ec823ac"), "MHF_Sheep"));
        mobSkullMap.put(EntityTypes.SHULKER, GameProfile.of(UUID.fromString("160f7d8a-c6b0-4fc8-8925-9e9d6c9c57d5"), "MHF_Shulker"));
        mobSkullMap.put(EntityTypes.SLIME, GameProfile.of(UUID.fromString("870aba93-40e8-48b3-89c5-32ece00d6630"), "MHF_Slime"));
        mobSkullMap.put(EntityTypes.SPIDER, GameProfile.of(UUID.fromString("5ad55f34-41b6-4bd2-9c32-18983c635936"), "MHF_Spider"));
        mobSkullMap.put(EntityTypes.SQUID, GameProfile.of(UUID.fromString("72e64683-e313-4c36-a408-c66b64e94af5"), "MHF_Squid"));
        mobSkullMap.put(EntityTypes.WITHER, GameProfile.of(UUID.fromString("39af6844-6809-4d2f-8ba4-7e92d087be18"), "MHF_Wither"));
        mobSkullMap.put(EntityTypes.WOLF, GameProfile.of(UUID.fromString("8d2d1d6d-8034-4c89-bd86-809a31fd5193"), "MHF_Wolf"));
        mobSkullMap.put(EntityTypes.VILLAGER, GameProfile.of(UUID.fromString("bd482739-767c-45dc-a1f8-c33c40530952"), "MHF_Villager"));
    }

    @Listener
    public void onItemDrops(DropItemEvent.Destruct event, @First EntitySpawnCause spawnCause) {
        EntityType type = spawnCause.getEntity().getType();

        EntityDamageSource damageSource = event.getCause().first(EntityDamageSource.class).orElse(null);
        Entity killer = null;

        if (damageSource != null) {
            killer = damageSource.getSource();
            if (killer instanceof Subject && !killPermission.hasPermission((Subject) killer)) {
                return;
            }
        }

        if (playerKillsOnly.getValue() && !(killer instanceof Player)) {
            return;
        }

        double chance = Math.min(1, dropRate.getValue());

        if (killer != null && killer instanceof Player) {
            int level = ((Player) killer).getItemInHand(HandTypes.MAIN_HAND).filter(item -> item.get(Keys.ITEM_ENCHANTMENTS).isPresent())
                    .map(item -> item.get(Keys.ITEM_ENCHANTMENTS).get().stream()
                            .filter(enchant -> enchant.getEnchantment().equals(Enchantments.LOOTING)).findFirst())
                    .filter(Optional::isPresent).map(enchant -> enchant.get().getLevel()).orElse(0);
            chance = Math.min(1, chance + (lootingRateModifier.getValue() * level));
        }

        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }

        SkullType skullType = null;
        GameProfile profile = null;

        if (type == EntityTypes.PLAYER) {
            if (playerHeads.getValue()) {
                skullType = SkullTypes.PLAYER;
                profile = ((Player) spawnCause.getEntity()).getProfile();
            }
        } else if (mobHeads.getValue()) {
            if (type == EntityTypes.ZOMBIE) {
                skullType = SkullTypes.ZOMBIE;
            } else if (type == EntityTypes.CREEPER) {
                skullType = SkullTypes.CREEPER;
            } else if (type == EntityTypes.SKELETON) {
                skullType = SkullTypes.SKELETON;
            } else if (type == EntityTypes.WITHER_SKELETON) {
                skullType = SkullTypes.WITHER_SKELETON;
            } else if (type == EntityTypes.ENDER_DRAGON) {
                skullType = SkullTypes.ENDER_DRAGON;
            } else {
                // Add extra mob.
                profile = getForEntity(type);
                if (profile != null) {
                    skullType = SkullTypes.PLAYER;
                }
            }
        }

        if (skullType != null) {
            ItemStack itemStack = Sponge.getGame().getRegistry().createBuilder(ItemStack.Builder.class).itemType(ItemTypes.SKULL)
                    .add(Keys.SKULL_TYPE, skullType).build();
            if (profile != null) {
                RepresentedPlayerData skinData = Sponge.getGame().getDataManager().getManipulatorBuilder(RepresentedPlayerData.class).get().create();
                skinData = skinData.set(Keys.REPRESENTED_PLAYER, profile);
                itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RESET, WordUtils.capitalize(type.getName()) + " Head"));
                itemStack.offer(skinData);
            }
            Vector3d location = event.getEntities().stream().findFirst().orElse(spawnCause.getEntity()).getLocation().getPosition();
            Item item = (Item) event.getTargetWorld().createEntity(EntityTypes.ITEM, location);
            item.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot());
            event.getTargetWorld().spawnEntity(item, Cause.of(NamedCause.of("root", spawnCause)));
        }
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary.MainHand event, @Named(NamedCause.SOURCE) Player player) {
        if (!showNameClick.getValue()) {
            return;
        }
        event.getTargetBlock().getLocation().filter(location -> location.getBlockType() == BlockTypes.SKULL).ifPresent(location -> {
            Skull skull = (Skull) location.getTileEntity().get();
            if (skull.skullType().get() == SkullTypes.PLAYER) {
                GameProfile profile = skull.get(Keys.REPRESENTED_PLAYER).orElse(null);
                if (profile != null) {
                    if (mobSkullMap.containsValue(profile)) {
                        EntityType entityType = mobSkullMap.entrySet().stream().filter(entry -> entry.getValue().equals(profile)).findFirst().get().getKey();
                        profile.getName().ifPresent(name -> player.sendMessage(Text.of(TextColors.YELLOW, "The severed head of a " + entityType.getName())));
                    } else {
                        profile.getName().ifPresent(name -> player.sendMessage(Text.of(TextColors.YELLOW, "The severed head of " + name)));
                    }
                }
            }
        });
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        event.getTransactions().stream()
                .filter(transaction -> transaction.getOriginal().getState().getType() == BlockTypes.SKULL)
                .map(Transaction::getOriginal)
                .forEach(snapshot -> {
                    Location<World> location = snapshot.getLocation().get();
                    if (snapshot.get(Keys.SKULL_TYPE).get() == SkullTypes.PLAYER) {
                        GameProfile profile = snapshot.get(Keys.REPRESENTED_PLAYER).orElse(null);
                        if (profile != null) {
                            String name;

                            if (mobSkullMap.containsValue(profile)) {
                                EntityType entityType = mobSkullMap.entrySet().stream().filter(entry -> entry.getValue().equals(profile)).findFirst().get().getKey();
                                name = WordUtils.capitalize(entityType.getName());
                            } else {
                                name = profile.getName().orElse(null);
                                if (name != null) {
                                    name += "'s";
                                }
                            }

                            if (name != null) {
                                ItemStack itemStack = Sponge.getGame().getRegistry().createBuilder(ItemStack.Builder.class).itemType(ItemTypes.SKULL)
                                        .add(Keys.SKULL_TYPE, SkullTypes.PLAYER).build();
                                RepresentedPlayerData skinData = Sponge.getGame().getDataManager().getManipulatorBuilder(RepresentedPlayerData.class).get().create();

                                skinData = skinData.set(Keys.REPRESENTED_PLAYER, profile);
                                itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RESET, name + " Head"));
                                itemStack.offer(skinData);
                                Item item = (Item) event.getTargetWorld().createEntity(EntityTypes.ITEM, location.getPosition().add(0.5, 0.5, 0.5));
                                item.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot());
                                event.getTargetWorld().spawnEntity(item, Cause.of(NamedCause.of("root",
                                        SpawnCause.builder().type(SpawnTypes.DROPPED_ITEM).build())));

                                event.setCancelled(true);
                                Sponge.getScheduler().createTaskBuilder().execute(() ->
                                        location.setBlockType(BlockTypes.AIR, CraftBookPlugin.spongeInst().getCause().build())).submit(CraftBookPlugin.spongeInst().container);
                            }
                        }
                    }
                });
    }

    private GameProfile getForEntity(EntityType entityType) {
        return mobSkullMap.get(entityType);
    }

    @Override
    public String getPath() {
        return "mechanics/head_drops";
    }

    @Override
    public String performCustomConversions(String input) {
        StringBuilder headTable = new StringBuilder();

        headTable.append("Mob Head Drops\n");
        headTable.append("=================\n\n");

        headTable.append("HeadDrops supports all mob heads in the base game, as well as many more.\n\n");

        int mobTypeLength = "Mob".length(),
                headImageLength = "Image".length();

        for(Map.Entry<EntityType, GameProfile> entry : mobSkullMap.entrySet()) {
            if(entry.getKey().getName().length() > mobTypeLength)
                mobTypeLength = entry.getKey().getName().length();
            if((".. image:: https://minotar.net/helm/" + entry.getValue().getName().orElse("") + "/64.png").length() > headImageLength)
                headImageLength = (".. image:: https://minotar.net/helm/" + entry.getValue().getName().orElse("") + "/64.png").length();
        }

        String border = createStringOfLength(mobTypeLength, '=') + ' '
                + createStringOfLength(headImageLength, '=');

        headTable.append(border).append('\n');
        headTable.append(padToLength("Mob", mobTypeLength + 1)).append(padToLength("Image", headImageLength + 1)).append('\n');
        headTable.append(border).append('\n');
        for(Map.Entry<EntityType, GameProfile> entry : mobSkullMap.entrySet()) {
            headTable.append(padToLength(entry.getKey().getName(), mobTypeLength + 1))
                    .append(padToLength(".. image:: https://minotar.net/helm/" + entry.getValue().getName().orElse("") + "/64.png", headImageLength + 1)).append('\n');
        }
        headTable.append(border).append('\n');

        return HEAD_DROPS_TABLE_PATTERN.matcher(input).replaceAll(Matcher.quoteReplacement(headTable.toString()));
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[]{
                playerKillsOnly,
                dropRate,
                lootingRateModifier,
                showNameClick,
                playerHeads,
                mobHeads
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                killPermission
        };
    }
}
