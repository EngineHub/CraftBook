/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;

import java.util.Optional;
import java.util.UUID;

@Module(moduleName = "HeadDrops", onEnable="onInitialize", onDisable="onDisable")
public class HeadDrops extends SpongeMechanic {

    @Listener
    public void onEntityDeath(DropItemEvent.Pre event) {
        Entity entity;
        if(event.getCause().first(Entity.class).isPresent())
            entity = event.getCause().first(Entity.class).get();
        else
            return;

        EntityType type = entity.getType();

        SkullData data = null;
        GameProfile profile = null;

        if (type == EntityTypes.PLAYER) {
            // This be a player.
            data = Sponge.getGame().getDataManager().getManipulatorBuilder(SkullData.class).get().create();
            data.set(Keys.SKULL_TYPE, SkullTypes.PLAYER);
            profile = ((Player) entity).getProfile();
        } else if (type == EntityTypes.ZOMBIE) {
            // This be a zombie.
            data = Sponge.getGame().getDataManager().getManipulatorBuilder(SkullData.class).get().create();
            data.set(Keys.SKULL_TYPE, SkullTypes.ZOMBIE);
        } else if (type == EntityTypes.CREEPER) {
            // This be a creeper.
            data = Sponge.getGame().getDataManager().getManipulatorBuilder(SkullData.class).get().create();
            data.set(Keys.SKULL_TYPE, SkullTypes.CREEPER);
        } else if (type == EntityTypes.SKELETON) {
            // This be a skeleton.
            data = Sponge.getGame().getDataManager().getManipulatorBuilder(SkullData.class).get().create();
            data.set(Keys.SKULL_TYPE, SkullTypes.SKELETON);
        } else {

            MobSkullType skullType = MobSkullType.getFromEntityType(type);
            if (skullType != null) {

                // Add extra mob.
                profile = skullType.getProfile();
                data = Sponge.getGame().getDataManager().getManipulatorBuilder(SkullData.class).get().create();
                data.set(Keys.SKULL_TYPE, SkullTypes.PLAYER);
            }
        }

        if (data != null) {
            ItemStack stack = Sponge.getGame().getRegistry().createBuilder(ItemStack.Builder.class).itemType(ItemTypes.SKULL).itemData(data).build();
            if (profile != null) {
                RepresentedPlayerData owner = Sponge.getGame().getDataManager().getManipulatorBuilder(RepresentedPlayerData.class).get().create();
                owner.set(Keys.REPRESENTED_PLAYER, profile);
                stack.offer(owner);
            }
            event.getDroppedItems().add(stack.createSnapshot());
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event) {

    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {

    }

    private enum MobSkullType {

        // Official or Guaranteed Static - Vanilla
        BLAZE(GameProfile.of(UUID.fromString("4c38ed11-596a-4fd4-ab1d-26f386c1cbac"), "MHF_Blaze")),
        CAVE_SPIDER(GameProfile.of(UUID.fromString("cab28771-f0cd-4fe7-b129-02c69eba79a5"), "MHF_CaveSpider")),
        CHICKEN(GameProfile.of(UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"), "MHF_Chicken")),
        COW(GameProfile.of(UUID.fromString("f159b274-c22e-4340-b7c1-52abde147713"), "MHF_Cow")),
        ENDERMAN(GameProfile.of(UUID.fromString("40ffb372-12f6-4678-b3f2-2176bf56dd4b"), "MHF_Enderman")),
        GHAST(GameProfile.of(UUID.fromString("063085a6-797f-4785-be1a-21cd7580f752"), "MHF_Ghast")),
        IRON_GOLEM(GameProfile.of(UUID.fromString("757f90b2-2344-4b8d-8dac-824232e2cece"), "MHF_Golem")),
        MAGMA_CUBE(GameProfile.of(UUID.fromString("0972bdd1-4b86-49fb-9ecc-a353f8491a51"), "MHF_LavaSlime")),
        MUSHROOM_COW(GameProfile.of(UUID.fromString("a46817d6-73c5-4f3f-b712-af6b3ff47b96"), "MHF_MushroomCow")),
        OCELOT(GameProfile.of(UUID.fromString("1bee9df5-4f71-42a2-bf52-d97970d3fea3"), "MHF_Ocelot")),
        PIG(GameProfile.of(UUID.fromString("8b57078b-f1bd-45df-83c4-d88d16768fbe"), "MHF_Pig")),
        PIG_ZOMBIE(GameProfile.of(UUID.fromString("18a2bb50-334a-4084-9184-2c380251a24b"), "MHF_PigZombie")),
        SHEEP(GameProfile.of(UUID.fromString("dfaad551-4e7e-45a1-a6f7-c6fc5ec823ac"), "MHF_Sheep")),
        SLIME(GameProfile.of(UUID.fromString("870aba93-40e8-48b3-89c5-32ece00d6630"), "MHF_Slime")),
        SPIDER(GameProfile.of(UUID.fromString("5ad55f34-41b6-4bd2-9c32-18983c635936"), "MHF_Spider")),
        SQUID(GameProfile.of(UUID.fromString("72e64683-e313-4c36-a408-c66b64e94af5"), "MHF_Squid")),
        WITHER(GameProfile.of(UUID.fromString("39af6844-6809-4d2f-8ba4-7e92d087be18"), "MHF_Wither")),
        WOLF(GameProfile.of(UUID.fromString("8d2d1d6d-8034-4c89-bd86-809a31fd5193"), "MHF_Wolf")),
        VILLAGER(GameProfile.of(UUID.fromString("bd482739-767c-45dc-a1f8-c33c40530952"), "MHF_Villager"));

        MobSkullType(GameProfile profile) {

            this.profile = profile;
        }

        private GameProfile profile;

        public GameProfile getProfile() {
            return profile;
        }

        public static MobSkullType getFromEntityType(EntityType entType) {
            try {
                return MobSkullType.valueOf(entType.getName());
            } catch (Exception e) {
                return null;
            }
        }

        public static EntityType getEntityType(GameProfile profile) {
            if (profile == null) return null;

            for (MobSkullType type : values())
                if (type.profile.getUniqueId().equals(profile.getUniqueId())) {
                    Optional<EntityType> tt = Sponge.getGame().getRegistry().getType(EntityType.class, type.name());
                    if (!tt.isPresent()) continue;
                    return tt.get();
                }

            return null;
        }
    }

}
