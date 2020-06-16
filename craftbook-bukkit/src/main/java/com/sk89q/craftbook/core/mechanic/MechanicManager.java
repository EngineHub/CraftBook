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

package com.sk89q.craftbook.core.mechanic;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.ImmutableList;
import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.AIMechanic;
import com.sk89q.craftbook.mechanics.BetterLeads;
import com.sk89q.craftbook.mechanics.BetterPhysics;
import com.sk89q.craftbook.mechanics.BetterPistons;
import com.sk89q.craftbook.mechanics.BetterPlants;
import com.sk89q.craftbook.mechanics.Bookcase;
import com.sk89q.craftbook.mechanics.BounceBlocks;
import com.sk89q.craftbook.mechanics.Chair;
import com.sk89q.craftbook.mechanics.ChunkAnchor;
import com.sk89q.craftbook.mechanics.CommandSigns;
import com.sk89q.craftbook.mechanics.CookingPot;
import com.sk89q.craftbook.mechanics.Elevator;
import com.sk89q.craftbook.mechanics.GlowStone;
import com.sk89q.craftbook.mechanics.HiddenSwitch;
import com.sk89q.craftbook.mechanics.JackOLantern;
import com.sk89q.craftbook.mechanics.LightSwitch;
import com.sk89q.craftbook.mechanics.MapChanger;
import com.sk89q.craftbook.mechanics.Marquee;
import com.sk89q.craftbook.mechanics.Netherrack;
import com.sk89q.craftbook.mechanics.PaintingSwitch;
import com.sk89q.craftbook.mechanics.Payment;
import com.sk89q.craftbook.mechanics.RedstoneJukebox;
import com.sk89q.craftbook.mechanics.Sponge;
import com.sk89q.craftbook.mechanics.Teleporter;
import com.sk89q.craftbook.mechanics.TreeLopper;
import com.sk89q.craftbook.mechanics.XPStorer;
import com.sk89q.craftbook.mechanics.area.Area;
import com.sk89q.craftbook.mechanics.area.simple.Bridge;
import com.sk89q.craftbook.mechanics.area.simple.Door;
import com.sk89q.craftbook.mechanics.area.simple.Gate;
import com.sk89q.craftbook.mechanics.boat.Drops;
import com.sk89q.craftbook.mechanics.boat.LandBoats;
import com.sk89q.craftbook.mechanics.boat.Uncrashable;
import com.sk89q.craftbook.mechanics.boat.WaterPlaceOnly;
import com.sk89q.craftbook.mechanics.cauldron.ImprovedCauldron;
import com.sk89q.craftbook.mechanics.cauldron.legacy.Cauldron;
import com.sk89q.craftbook.mechanics.crafting.CustomCrafting;
import com.sk89q.craftbook.mechanics.dispenser.DispenserRecipes;
import com.sk89q.craftbook.mechanics.drops.CustomDrops;
import com.sk89q.craftbook.mechanics.headdrops.HeadDrops;
import com.sk89q.craftbook.mechanics.ic.ICMechanic;
import com.sk89q.craftbook.mechanics.items.CommandItems;
import com.sk89q.craftbook.mechanics.minecart.CollisionEntry;
import com.sk89q.craftbook.mechanics.minecart.ConstantSpeed;
import com.sk89q.craftbook.mechanics.minecart.EmptyDecay;
import com.sk89q.craftbook.mechanics.minecart.EmptySlowdown;
import com.sk89q.craftbook.mechanics.minecart.FallModifier;
import com.sk89q.craftbook.mechanics.minecart.ItemPickup;
import com.sk89q.craftbook.mechanics.minecart.MobBlocker;
import com.sk89q.craftbook.mechanics.minecart.MoreRails;
import com.sk89q.craftbook.mechanics.minecart.NoCollide;
import com.sk89q.craftbook.mechanics.minecart.PlaceAnywhere;
import com.sk89q.craftbook.mechanics.minecart.RailPlacer;
import com.sk89q.craftbook.mechanics.minecart.TemporaryCart;
import com.sk89q.craftbook.mechanics.minecart.VisionSteering;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartBooster;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartDeposit;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartDispenser;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartEjector;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartLift;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartMaxSpeed;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartMessenger;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartReverser;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartSorter;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartStation;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartTeleporter;
import com.sk89q.craftbook.mechanics.pipe.Pipes;
import com.sk89q.craftbook.mechanics.signcopier.SignCopier;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
import com.sk89q.craftbook.util.exceptions.MechanicInitializationException;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class MechanicManager {

    private final CraftBookPlugin plugin;

    /**
     * List of common mechanics.
     */
    private final List<CraftBookMechanic> loadedMechanics = new ArrayList<>();

    public MechanicManager(CraftBookPlugin plugin) {
        this.plugin = plugin;
    }

    @Deprecated
    private static void registerMechanic(String name, Class<? extends CraftBookMechanic> mechanicClass, MechanicCategory category) {
        CraftBookPlugin.logger().warning("Legacy mechanic registered: " + name);
        Converter<String, String> camelToSnake = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

        MechanicType<?> mechanicType = MechanicType.Builder
                .create()
                .id(camelToSnake.convert(name))
                .name(name)
                .className(mechanicClass.getName())
                .category(category)
                .build();

        MechanicType.REGISTRY.register(mechanicType.getId(), mechanicType);
    }

    private void registerMechanic(MechanicType<?> mechanicType) {
        MechanicType.REGISTRY.register(mechanicType.getId(), mechanicType);
    }

    public void setup() {
        registerMechanic("Variables", VariableManager.class, MechanicCategory.GENERAL);
        registerMechanic("CommandItems", CommandItems.class, MechanicCategory.CUSTOMISATION);
        registerMechanic("CustomCrafting", CustomCrafting.class, MechanicCategory.CUSTOMISATION);
        registerMechanic("DispenserRecipes", DispenserRecipes.class, MechanicCategory.GENERAL);
        registerMechanic("CustomDrops", CustomDrops.class, MechanicCategory.CUSTOMISATION);
        registerMechanic("BetterAi", AIMechanic.class, MechanicCategory.GENERAL);
        registerMechanic("PaintingSwitcher", PaintingSwitch.class, MechanicCategory.GENERAL);
        registerMechanic("BetterPhysics", BetterPhysics.class, MechanicCategory.GENERAL);
        registerMechanic("HeadDrops", HeadDrops.class, MechanicCategory.GENERAL);
        registerMechanic("BetterLeads", BetterLeads.class, MechanicCategory.GENERAL);
        registerMechanic("Marquee", Marquee.class, MechanicCategory.GENERAL);
        registerMechanic("TreeLopper", TreeLopper.class, MechanicCategory.GENERAL);
        registerMechanic("MapChanger", MapChanger.class, MechanicCategory.GENERAL);
        registerMechanic("XpStorer", XPStorer.class, MechanicCategory.GENERAL);
        registerMechanic("CommandSigns", CommandSigns.class, MechanicCategory.GENERAL);
        registerMechanic("LightSwitch", LightSwitch.class, MechanicCategory.GENERAL);
        registerMechanic("ChunkAnchor", ChunkAnchor.class, MechanicCategory.GENERAL);
        registerMechanic("HiddenSwitch", HiddenSwitch.class, MechanicCategory.GENERAL);
        registerMechanic("Bookcase", Bookcase.class, MechanicCategory.GENERAL);
        registerMechanic("SignCopier", SignCopier.class, MechanicCategory.TOOL);
        registerMechanic("Bridge", Bridge.class, MechanicCategory.GENERAL);
        registerMechanic("Door", Door.class, MechanicCategory.GENERAL);
        registerMechanic("Elevator", Elevator.class, MechanicCategory.GENERAL);
        registerMechanic("Teleporter", Teleporter.class, MechanicCategory.GENERAL);
        registerMechanic("ToggleArea", Area.class, MechanicCategory.GENERAL);
        registerMechanic("Cauldron", ImprovedCauldron.class, MechanicCategory.CUSTOMISATION);
        registerMechanic("LegacyCauldron", Cauldron.class, MechanicCategory.CUSTOMISATION);
        registerMechanic("Gate", Gate.class, MechanicCategory.GENERAL);
        registerMechanic("BetterPistons", BetterPistons.class, MechanicCategory.GENERAL);
        registerMechanic("CookingPot", CookingPot.class, MechanicCategory.GENERAL);
        registerMechanic("Sponge", Sponge.class, MechanicCategory.GENERAL);
        registerMechanic("BetterPlants", BetterPlants.class, MechanicCategory.GENERAL);
        registerMechanic("Chairs", Chair.class, MechanicCategory.GENERAL);
        registerMechanic("Pay", Payment.class, MechanicCategory.CIRCUIT);
        registerMechanic("Jukebox", RedstoneJukebox.class, MechanicCategory.CIRCUIT);
        registerMechanic("Glowstone", GlowStone.class, MechanicCategory.CIRCUIT);
        registerMechanic("Netherrack", Netherrack.class, MechanicCategory.CIRCUIT);
        registerMechanic("JackOLantern", JackOLantern.class, MechanicCategory.CIRCUIT);
        registerMechanic("Pipes", Pipes.class, MechanicCategory.CIRCUIT);
        registerMechanic("BounceBlocks", BounceBlocks.class, MechanicCategory.GENERAL);
        registerMechanic("IntegratedCircuits", ICMechanic.class, MechanicCategory.CIRCUIT);
        registerMechanic("MinecartBooster", CartBooster.class, MechanicCategory.MINECART);
        registerMechanic("MinecartReverser", CartReverser.class, MechanicCategory.MINECART);
        registerMechanic("MinecartSorter", CartSorter.class, MechanicCategory.MINECART);
        registerMechanic("MinecartStation", CartStation.class, MechanicCategory.MINECART);
        registerMechanic("MinecartEjector", CartEjector.class, MechanicCategory.MINECART);
        registerMechanic("MinecartDeposit", CartDeposit.class, MechanicCategory.MINECART);
        registerMechanic("MinecartTeleporter", CartTeleporter.class, MechanicCategory.MINECART);
        registerMechanic("MinecartElevator", CartLift.class, MechanicCategory.MINECART);
        registerMechanic("MinecartDispenser", CartDispenser.class, MechanicCategory.MINECART);
        registerMechanic("MinecartMessenger", CartMessenger.class, MechanicCategory.MINECART);
        registerMechanic("MinecartMaxSpeed", CartMaxSpeed.class, MechanicCategory.MINECART);
        registerMechanic("MinecartMoreRails", MoreRails.class, MechanicCategory.MINECART);
        registerMechanic("MinecartRemoveEntities", com.sk89q.craftbook.mechanics.minecart.RemoveEntities.class, MechanicCategory.MINECART);
        registerMechanic("MinecartVisionSteering", VisionSteering.class, MechanicCategory.MINECART);
        registerMechanic("MinecartDecay", EmptyDecay.class, MechanicCategory.MINECART);
        registerMechanic("MinecartMobBlocker", MobBlocker.class, MechanicCategory.MINECART);
        registerMechanic("MinecartExitRemover", com.sk89q.craftbook.mechanics.minecart.ExitRemover.class, MechanicCategory.MINECART);
        registerMechanic("MinecartCollisionEntry", CollisionEntry.class, MechanicCategory.MINECART);
        registerMechanic("MinecartItemPickup", ItemPickup.class, MechanicCategory.MINECART);
        registerMechanic("MinecartFallModifier", FallModifier.class, MechanicCategory.MINECART);
        registerMechanic("MinecartConstantSpeed", ConstantSpeed.class, MechanicCategory.MINECART);
        registerMechanic("MinecartRailPlacer", RailPlacer.class, MechanicCategory.MINECART);
        registerMechanic("MinecartSpeedModifiers", com.sk89q.craftbook.mechanics.minecart.SpeedModifiers.class, MechanicCategory.MINECART);
        registerMechanic("MinecartEmptySlowdown", EmptySlowdown.class, MechanicCategory.MINECART);
        registerMechanic("MinecartNoCollide", NoCollide.class, MechanicCategory.MINECART);
        registerMechanic("MinecartPlaceAnywhere", PlaceAnywhere.class, MechanicCategory.MINECART);
        registerMechanic("MinecartTemporaryCart", TemporaryCart.class, MechanicCategory.MINECART);
        registerMechanic("BoatRemoveEntities", com.sk89q.craftbook.mechanics.boat.RemoveEntities.class, MechanicCategory.BOAT);
        registerMechanic("BoatUncrashable", Uncrashable.class, MechanicCategory.BOAT);
        registerMechanic("BoatDrops", Drops.class, MechanicCategory.BOAT);
        registerMechanic("BoatDecay", com.sk89q.craftbook.mechanics.boat.EmptyDecay.class, MechanicCategory.BOAT);
        registerMechanic("BoatSpeedModifiers", com.sk89q.craftbook.mechanics.boat.SpeedModifiers.class, MechanicCategory.BOAT);
        registerMechanic("LandBoats", LandBoats.class, MechanicCategory.BOAT);
        registerMechanic("BoatExitRemover", com.sk89q.craftbook.mechanics.boat.ExitRemover.class, MechanicCategory.BOAT);
        registerMechanic("BoatWaterPlaceOnly", WaterPlaceOnly.class, MechanicCategory.BOAT);

        registerMechanic(MechanicType.Builder
                .create()
                .id("snow")
                .name("Snow")
                .className("com.sk89q.craftbook.mechanics.Snow")
                .category(MechanicCategory.GENERAL)
                .build()
        );

        registerMechanic(MechanicType.Builder
                .create()
                .id("ammeter")
                .name("Ammeter")
                .className("com.sk89q.craftbook.mechanics.Ammeter")
                .category(MechanicCategory.TOOL)
                .build()
        );

        registerMechanic(MechanicType.Builder
                .create()
                .id("lightstone")
                .name("LightStone")
                .className("com.sk89q.craftbook.mechanics.LightStone")
                .category(MechanicCategory.TOOL)
                .build()
        );
    }

    public void shutdown() {
        ImmutableList.copyOf(loadedMechanics).forEach(this::disableMechanic);
        loadedMechanics.clear();
    }

    /**
     * Gets a list of loaded mechanics.
     *
     * <p>
     *     This list is immutable
     * </p>
     *
     * @return A list of loaded mechanics
     */
    public List<CraftBookMechanic> getLoadedMechanics() {
        return ImmutableList.copyOf(this.loadedMechanics);
    }

    @SuppressWarnings("unchecked")
    public <T extends CraftBookMechanic> MechanicType<T> getMechanicType(T mechanic) {
        return (MechanicType<T>) MechanicType.REGISTRY.values().stream().filter(type -> type.isInstance(mechanic)).findAny().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T extends CraftBookMechanic> Optional<T> getMechanic(@Nullable MechanicType<T> mechanicType) {
        if (mechanicType == null) {
            return Optional.empty();
        }
        return (Optional<T>) this.loadedMechanics.stream().filter(mechanicType::isInstance).findAny();
    }

    public boolean isMechanicEnabled(@Nullable MechanicType<?> mechanicType) {
        return getMechanic(mechanicType).isPresent();
    }

    /**
     * Enables the mechanic with the specified type.
     *
     * @param mechanicType The type of the mechanic.
     * @throws MechanicInitializationException If the mechanic could not be enabled.
     */
    public void enableMechanic(MechanicType<?> mechanicType) throws MechanicInitializationException {
        if (isMechanicEnabled(mechanicType)) {
            throw new MechanicInitializationException(mechanicType, "Mechanic " + mechanicType.getId() + " is already enabled.");
        }
        try {
            CraftBookMechanic mech = mechanicType.getMechanicClass().getDeclaredConstructor().newInstance();
            mech.loadConfiguration(new File(new File(CraftBookPlugin.inst().getDataFolder(), "mechanics"), mechanicType.getName() + ".yml"));
            loadedMechanics.add(mech);

            if (!mech.enable()) {
                mech.disable();
                throw new MechanicInitializationException(mechanicType, "An error occurred while enabling");
            }
            Bukkit.getPluginManager().registerEvents(mech, plugin);
        } catch (Throwable t) {
            throw new MechanicInitializationException(mechanicType, "An error occurred while enabling", t);
        }
    }

    /**
     * Disables the mechanic.
     *
     * @param mechanic The mechanic.
     * @return If the mechanic could be disabled.
     */
    public boolean disableMechanic(CraftBookMechanic mechanic) {
        if (mechanic == null) {
            return false;
        }

        mechanic.disable();
        this.loadedMechanics.remove(mechanic);
        HandlerList.unregisterAll(mechanic);

        return true;
    }
}
