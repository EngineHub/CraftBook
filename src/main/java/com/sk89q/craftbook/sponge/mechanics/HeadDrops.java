package com.sk89q.craftbook.sponge.mechanics;

import java.util.UUID;

import org.spongepowered.api.GameProfile;
import org.spongepowered.api.data.manipulators.OwnableData;
import org.spongepowered.api.data.manipulators.SkullData;
import org.spongepowered.api.data.types.SkullTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.block.BlockBreakEvent;
import org.spongepowered.api.event.block.BlockPlaceEvent;
import org.spongepowered.api.event.entity.living.LivingDropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import com.google.common.base.Optional;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;

public class HeadDrops extends SpongeMechanic {

    @Subscribe
    public void onEntityDeath(LivingDropItemEvent event) {

        EntityType type = event.getEntity().getType();

        SkullData data = null;
        GameProfile profile = null;

        if (type == EntityTypes.PLAYER) {
            // This be a player.
            data = CraftBookPlugin.game.getRegistry().getManipulatorRegistry().getBuilder(SkullData.class).get().create();
            data.setValue(SkullTypes.PLAYER);
            profile = ((Player) event.getEntity()).getProfile();
        } else if (type == EntityTypes.ZOMBIE) {
            // This be a zombie.
            data = CraftBookPlugin.game.getRegistry().getManipulatorRegistry().getBuilder(SkullData.class).get().create();
            data.setValue(SkullTypes.ZOMBIE);
        } else if (type == EntityTypes.CREEPER) {
            // This be a creeper.
            data = CraftBookPlugin.game.getRegistry().getManipulatorRegistry().getBuilder(SkullData.class).get().create();
            data.setValue(SkullTypes.CREEPER);
        } else if (type == EntityTypes.SKELETON) {
            // This be a skeleton.
            data = CraftBookPlugin.game.getRegistry().getManipulatorRegistry().getBuilder(SkullData.class).get().create();
            data.setValue(SkullTypes.SKELETON);
        } else {

            MobSkullType skullType = MobSkullType.getFromEntityType(type);
            if (skullType != null) {

                // Add extra mob.
                profile = skullType.getProfile();
                data = CraftBookPlugin.game.getRegistry().getManipulatorRegistry().getBuilder(SkullData.class).get().create();
                data.setValue(SkullTypes.PLAYER);
            }
        }

        if (data != null) {
            ItemStack stack = CraftBookPlugin.game.getRegistry().getItemBuilder().itemType(ItemTypes.SKULL).itemData(data).build();
            if (profile != null) {
                OwnableData owner = stack.getOrCreate(OwnableData.class).get();
                owner.setProfile(profile);
                stack.offer(owner);
            }
            event.getDroppedItems().add(stack);
            event.getDroppedItems();
        }
    }

    @Subscribe
    public void onBlockPlace(BlockPlaceEvent event) {

    }

    @Subscribe
    public void onBlockBreak(BlockBreakEvent event) {

    }

    private enum MobSkullType {

        // Official or Guaranteed Static - Vanilla
        BLAZE(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("4c38ed11-596a-4fd4-ab1d-26f386c1cbac"), "MHF_Blaze")),
        CAVE_SPIDER(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("cab28771-f0cd-4fe7-b129-02c69eba79a5"), "MHF_CaveSpider")),
        CHICKEN(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"), "MHF_Chicken")),
        COW(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("f159b274-c22e-4340-b7c1-52abde147713"), "MHF_Cow")),
        ENDERMAN(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("40ffb372-12f6-4678-b3f2-2176bf56dd4b"), "MHF_Enderman")),
        GHAST(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("063085a6-797f-4785-be1a-21cd7580f752"), "MHF_Ghast")),
        IRON_GOLEM(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("757f90b2-2344-4b8d-8dac-824232e2cece"), "MHF_Golem")),
        MAGMA_CUBE(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("0972bdd1-4b86-49fb-9ecc-a353f8491a51"), "MHF_LavaSlime")),
        MUSHROOM_COW(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("a46817d6-73c5-4f3f-b712-af6b3ff47b96"), "MHF_MushroomCow")),
        OCELOT(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("1bee9df5-4f71-42a2-bf52-d97970d3fea3"), "MHF_Ocelot")),
        PIG(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("8b57078b-f1bd-45df-83c4-d88d16768fbe"), "MHF_Pig")),
        PIG_ZOMBIE(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("18a2bb50-334a-4084-9184-2c380251a24b"), "MHF_PigZombie")),
        SHEEP(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("dfaad551-4e7e-45a1-a6f7-c6fc5ec823ac"), "MHF_Sheep")),
        SLIME(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("870aba93-40e8-48b3-89c5-32ece00d6630"), "MHF_Slime")),
        SPIDER(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("5ad55f34-41b6-4bd2-9c32-18983c635936"), "MHF_Spider")),
        SQUID(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("72e64683-e313-4c36-a408-c66b64e94af5"), "MHF_Squid")),
        WITHER(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("39af6844-6809-4d2f-8ba4-7e92d087be18"), "MHF_Wither")),
        WOLF(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("8d2d1d6d-8034-4c89-bd86-809a31fd5193"), "MHF_Wolf")),
        VILLAGER(CraftBookPlugin.game.getRegistry().createGameProfile(UUID.fromString("bd482739-767c-45dc-a1f8-c33c40530952"), "MHF_Villager"));

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
                if (type.getProfile().getUniqueId().equals(profile.getUniqueId())) {
                    Optional<EntityType> tt = CraftBookPlugin.game.getRegistry().getType(EntityType.class, type.name());
                    if (!tt.isPresent()) continue;
                    return tt.get();
                }

            return null;
        }
    }

}
