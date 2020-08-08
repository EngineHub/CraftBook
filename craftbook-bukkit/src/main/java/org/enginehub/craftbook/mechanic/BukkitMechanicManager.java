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

package org.enginehub.craftbook.mechanic;

import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.load.LoadPriority;
import org.enginehub.craftbook.mechanics.AIMechanic;
import org.enginehub.craftbook.mechanics.BetterLeads;
import org.enginehub.craftbook.mechanics.BetterPhysics;
import org.enginehub.craftbook.mechanics.BetterPistons;
import org.enginehub.craftbook.mechanics.BetterPlants;
import org.enginehub.craftbook.mechanics.Bookcase;
import org.enginehub.craftbook.mechanics.BounceBlocks;
import org.enginehub.craftbook.mechanics.Chair;
import org.enginehub.craftbook.mechanics.ChunkAnchor;
import org.enginehub.craftbook.mechanics.CommandSigns;
import org.enginehub.craftbook.mechanics.CookingPot;
import org.enginehub.craftbook.mechanics.Elevator;
import org.enginehub.craftbook.mechanics.GlowStone;
import org.enginehub.craftbook.mechanics.HiddenSwitch;
import org.enginehub.craftbook.mechanics.LightSwitch;
import org.enginehub.craftbook.mechanics.MapChanger;
import org.enginehub.craftbook.mechanics.Marquee;
import org.enginehub.craftbook.mechanics.PaintingSwitch;
import org.enginehub.craftbook.mechanics.Payment;
import org.enginehub.craftbook.mechanics.Sponge;
import org.enginehub.craftbook.mechanics.Teleporter;
import org.enginehub.craftbook.mechanics.TreeLopper;
import org.enginehub.craftbook.mechanics.XPStorer;
import org.enginehub.craftbook.mechanics.area.Area;
import org.enginehub.craftbook.mechanics.area.simple.Bridge;
import org.enginehub.craftbook.mechanics.area.simple.Door;
import org.enginehub.craftbook.mechanics.area.simple.Gate;
import org.enginehub.craftbook.mechanics.boat.LandBoats;
import org.enginehub.craftbook.mechanics.boat.Uncrashable;
import org.enginehub.craftbook.mechanics.boat.WaterPlaceOnly;
import org.enginehub.craftbook.mechanics.cauldron.ImprovedCauldron;
import org.enginehub.craftbook.mechanics.crafting.CustomCrafting;
import org.enginehub.craftbook.mechanics.dispenser.DispenserRecipes;
import org.enginehub.craftbook.mechanics.drops.CustomDrops;
import org.enginehub.craftbook.mechanics.headdrops.HeadDrops;
import org.enginehub.craftbook.mechanics.ic.ICMechanic;
import org.enginehub.craftbook.mechanics.items.CommandItems;
import org.enginehub.craftbook.mechanics.minecart.CollisionEntry;
import org.enginehub.craftbook.mechanics.minecart.ConstantSpeed;
import org.enginehub.craftbook.mechanics.minecart.EmptyDecay;
import org.enginehub.craftbook.mechanics.minecart.EmptySlowdown;
import org.enginehub.craftbook.mechanics.minecart.FallModifier;
import org.enginehub.craftbook.mechanics.minecart.ItemPickup;
import org.enginehub.craftbook.mechanics.minecart.MobBlocker;
import org.enginehub.craftbook.mechanics.minecart.MoreRails;
import org.enginehub.craftbook.mechanics.minecart.NoCollide;
import org.enginehub.craftbook.mechanics.minecart.PlaceAnywhere;
import org.enginehub.craftbook.mechanics.minecart.RailPlacer;
import org.enginehub.craftbook.mechanics.minecart.TemporaryCart;
import org.enginehub.craftbook.mechanics.minecart.VisionSteering;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartBooster;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartDeposit;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartDispenser;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartEjector;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartLift;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartMaxSpeed;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartMessenger;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartReverser;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartSorter;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartStation;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartTeleporter;
import org.enginehub.craftbook.mechanics.pipe.Pipes;
import org.enginehub.craftbook.mechanics.signcopier.SignCopier;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class BukkitMechanicManager extends MechanicManager {

    @Override
    public void setup() {
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
        registerMechanic("Gate", Gate.class, MechanicCategory.GENERAL);
        registerMechanic("BetterPistons", BetterPistons.class, MechanicCategory.GENERAL);
        registerMechanic("CookingPot", CookingPot.class, MechanicCategory.GENERAL);
        registerMechanic("Sponge", Sponge.class, MechanicCategory.GENERAL);
        registerMechanic("BetterPlants", BetterPlants.class, MechanicCategory.GENERAL);
        registerMechanic("Chairs", Chair.class, MechanicCategory.GENERAL);
        registerMechanic("Pay", Payment.class, MechanicCategory.CIRCUIT);
        registerMechanic("Glowstone", GlowStone.class, MechanicCategory.CIRCUIT);
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
        registerMechanic("MinecartRemoveEntities", org.enginehub.craftbook.mechanics.minecart.RemoveEntities.class, MechanicCategory.MINECART);
        registerMechanic("MinecartVisionSteering", VisionSteering.class, MechanicCategory.MINECART);
        registerMechanic("MinecartDecay", EmptyDecay.class, MechanicCategory.MINECART);
        registerMechanic("MinecartMobBlocker", MobBlocker.class, MechanicCategory.MINECART);
        registerMechanic("MinecartExitRemover", org.enginehub.craftbook.mechanics.minecart.ExitRemover.class, MechanicCategory.MINECART);
        registerMechanic("MinecartCollisionEntry", CollisionEntry.class, MechanicCategory.MINECART);
        registerMechanic("MinecartItemPickup", ItemPickup.class, MechanicCategory.MINECART);
        registerMechanic("MinecartFallModifier", FallModifier.class, MechanicCategory.MINECART);
        registerMechanic("MinecartConstantSpeed", ConstantSpeed.class, MechanicCategory.MINECART);
        registerMechanic("MinecartRailPlacer", RailPlacer.class, MechanicCategory.MINECART);
        registerMechanic("MinecartSpeedModifiers", org.enginehub.craftbook.mechanics.minecart.SpeedModifiers.class, MechanicCategory.MINECART);
        registerMechanic("MinecartEmptySlowdown", EmptySlowdown.class, MechanicCategory.MINECART);
        registerMechanic("MinecartNoCollide", NoCollide.class, MechanicCategory.MINECART);
        registerMechanic("MinecartPlaceAnywhere", PlaceAnywhere.class, MechanicCategory.MINECART);
        registerMechanic("MinecartTemporaryCart", TemporaryCart.class, MechanicCategory.MINECART);
        registerMechanic("BoatRemoveEntities", org.enginehub.craftbook.mechanics.boat.RemoveEntities.class, MechanicCategory.BOAT);
        registerMechanic("BoatUncrashable", Uncrashable.class, MechanicCategory.BOAT);
        registerMechanic("BoatDecay", org.enginehub.craftbook.mechanics.boat.EmptyDecay.class, MechanicCategory.BOAT);
        registerMechanic("BoatSpeedModifiers", org.enginehub.craftbook.mechanics.boat.SpeedModifiers.class, MechanicCategory.BOAT);
        registerMechanic("LandBoats", LandBoats.class, MechanicCategory.BOAT);
        registerMechanic("BoatExitRemover", org.enginehub.craftbook.mechanics.boat.ExitRemover.class, MechanicCategory.BOAT);
        registerMechanic("BoatWaterPlaceOnly", WaterPlaceOnly.class, MechanicCategory.BOAT);

        registerMechanic(MechanicType.Builder
                .create()
                .id("variables")
                .name("Variables")
                .className("org.enginehub.craftbook.mechanics.variables.VariableManager")
                .category(MechanicCategory.GENERAL)
                .loadPriority(LoadPriority.EARLY)
                .build()
        );

        registerMechanic(MechanicType.Builder
                .create()
                .id("snow")
                .name("Snow")
                .className("org.enginehub.craftbook.mechanics.Snow")
                .category(MechanicCategory.GENERAL)
                .build()
        );

        registerMechanic(MechanicType.Builder
                .create()
                .id("ammeter")
                .name("Ammeter")
                .className("org.enginehub.craftbook.mechanics.Ammeter")
                .category(MechanicCategory.TOOL)
                .build()
        );

        registerMechanic(MechanicType.Builder
                .create()
                .id("lightstone")
                .name("LightStone")
                .className("org.enginehub.craftbook.mechanics.LightStone")
                .category(MechanicCategory.TOOL)
                .build()
        );

        registerMechanic(MechanicType.Builder
                .create()
                .id("redstone_fire")
                .name("RedstoneFire")
                .className("org.enginehub.craftbook.mechanics.RedstoneFire")
                .category(MechanicCategory.CIRCUIT)
                .build()
        );

        registerMechanic(MechanicType.Builder
                .create()
                .id("jukebox")
                .name("Jukebox")
                .className("org.enginehub.craftbook.mechanics.RedstoneJukebox")
                .category(MechanicCategory.CIRCUIT)
                .build()
        );

        registerMechanic(MechanicType.Builder
                .create()
                .id("jack_o_lantern")
                .name("JackOLantern")
                .className("org.enginehub.craftbook.mechanics.JackOLantern")
                .category(MechanicCategory.CIRCUIT)
                .build()
        );

        // TODO Variables & CommandItems need to load early (variables before CommandItems). Marquee *depends* on Variables
    }

    @Override
    protected void enableMechanicPlatformListeners(CraftBookMechanic mechanic) {
        Bukkit.getPluginManager().registerEvents((Listener) mechanic, CraftBookPlugin.inst());
    }

    @Override
    protected void disableMechanicPlatformListeners(CraftBookMechanic mechanic) {
        HandlerList.unregisterAll((Listener) mechanic);
    }
}
