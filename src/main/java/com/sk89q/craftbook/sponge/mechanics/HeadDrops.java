package com.sk89q.craftbook.sponge.mechanics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.data.manipulators.OwnableData;
import org.spongepowered.api.data.manipulators.entities.SkullData;
import org.spongepowered.api.data.types.SkullTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.living.LivingDeathEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import com.google.common.base.Optional;
import com.sk89q.craftbook.sponge.CraftBookPlugin;

public class HeadDrops extends SpongeMechanic {

    @Subscribe
    public void onEntityDeath(LivingDeathEvent event) {

        EntityType type = event.getLiving().getType();

        SkullData data = null;
        UUID ownerUUID = null;

        if(type == EntityTypes.PLAYER) {
            //This be a player.
            data = CraftBookPlugin.game.getRegistry().getManipulatorRegistry().getBuilder(SkullData.class).get().create();
            data.setValue(SkullTypes.PLAYER);
            ownerUUID = event.getLiving().getUniqueId();
        } else if(type == EntityTypes.ZOMBIE) {
            //This be a zombie.
            data = CraftBookPlugin.game.getRegistry().getManipulatorRegistry().getBuilder(SkullData.class).get().create();
            data.setValue(SkullTypes.ZOMBIE);
        } else if(type == EntityTypes.CREEPER) {
            //This be a creeper.
            data = CraftBookPlugin.game.getRegistry().getManipulatorRegistry().getBuilder(SkullData.class).get().create();
            data.setValue(SkullTypes.CREEPER);
        } else if(type == EntityTypes.SKELETON) {
            //This be a skeleton.
            data = CraftBookPlugin.game.getRegistry().getManipulatorRegistry().getBuilder(SkullData.class).get().create();
            data.setValue(SkullTypes.SKELETON);
        } else {

            MobSkullType skullType = MobSkullType.getFromEntityType(type);
            if(skullType != null) {

                //Add extra mob.
                //TODO - find out which data does that.
                data = CraftBookPlugin.game.getRegistry().getManipulatorRegistry().getBuilder(SkullData.class).get().create();
                data.setValue(SkullTypes.PLAYER);
            }
        }

        if(data != null) {
            ItemStack stack = CraftBookPlugin.game.getRegistry().getItemBuilder().itemType(ItemTypes.SKULL).itemData(data).build();
            if(ownerUUID != null) {
                OwnableData owner = stack.getOrCreate(OwnableData.class).get();
                owner.setProfile(CraftBookPlugin.game.getRegistry().createGameProfile(ownerUUID, "Skull"));
            }
            event.getDroppedItems().add(stack);
        }
    }

    private enum MobSkullType {

        //Official
        BLAZE("MHF_Blaze", "Blaze_Head"),
        CAVE_SPIDER("MHF_CaveSpider"),
        CHICKEN("MHF_Chicken", "scraftbrothers1"),
        COW("MHF_Cow", "VerifiedBernard", "CarlosTheCow"),
        ENDERMAN("MHF_Enderman", "Violit"),
        GHAST("MHF_Ghast", "_QuBra_"),
        MAGMA_CUBE("MHF_LavaSlime"),
        MUSHROOM_COW("MHF_MushroomCow", "Mooshroom_Stew"),
        PIG("MHF_Pig", "XlexerX"),
        PIG_ZOMBIE("MHF_PigZombie", "ManBearPigZombie", "scraftbrothers5"),
        SHEEP("MHF_Sheep", "SGT_KICYORASS", "Eagle_Peak"),
        SLIME("MHF_Slime", "HappyHappyMan"),
        SPIDER("MHF_Spider", "Kelevra_V"),
        VILLAGER("MHF_Villager", "Villager", "Kuvase", "scraftbrothers9"),
        IRON_GOLEM("MHF_Golem", "zippie007"),
        SQUID("MHF_Squid", "squidette8"),
        OCELOT("MHF_Ocelot", "scraftbrothers3"),

        //Unofficial/Community
        BAT("coolwhip101", "bozzobrain"),
        ENDER_DRAGON("KingEndermen", "KingEnderman"),
        SILVERFISH("Xzomag", "AlexVMiner"),
        SNOWMAN("scraftbrothers2", "Koebasti"),
        HORSE("gavertoso"),
        WOLF("Budwolf"),
        WITCH("scrafbrothers4");

        MobSkullType(String playerName, String ... oldNames) {

            this.playerName = playerName;
            this.oldNames = new HashSet<String>(Arrays.asList(oldNames));
        }

        private String playerName;
        private Set<String> oldNames;

        public String getPlayerName() {

            return playerName;
        }

        public boolean isOldName(String name) {

            return oldNames.contains(name);
        }

        public static MobSkullType getFromEntityType(EntityType entType) {

            try {
                return MobSkullType.valueOf(entType.getName());
            } catch(Exception e){
                return null;
            }
        }

        public static EntityType getEntityType(String name) {

            if (name == null)
                return null;

            for(MobSkullType type : values())
                if(type.getPlayerName().equalsIgnoreCase(name) || type.isOldName(name)) {
                    Optional<EntityType> tt = CraftBookPlugin.game.getRegistry().getType(EntityType.class, name);
                    if(!tt.isPresent())
                        continue;
                    return tt.get();
                }

            return null;
        }
    }

}
