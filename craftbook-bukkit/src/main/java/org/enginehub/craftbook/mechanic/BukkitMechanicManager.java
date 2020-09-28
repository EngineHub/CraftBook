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

import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.load.LoadPriority;
import org.enginehub.craftbook.mechanic.load.MechanicDependency;
import org.enginehub.craftbook.mechanic.load.PluginDependency;

public class BukkitMechanicManager extends MechanicManager {

    @Override
    public void setup() {
//        registerMechanic("CommandItems", org.enginehub.craftbook.mechanics.items.CommandItems.class, MechanicCategory.CUSTOMISATION);
//        registerMechanic("CustomCrafting", org.enginehub.craftbook.mechanics.crafting.CustomCrafting.class, MechanicCategory.CUSTOMISATION);
//        registerMechanic("DispenserRecipes", org.enginehub.craftbook.mechanics.dispenser.DispenserRecipes.class, MechanicCategory.GENERAL);
//        registerMechanic("CustomDrops", org.enginehub.craftbook.mechanics.drops.CustomDrops.class, MechanicCategory.CUSTOMISATION);
//        registerMechanic("BetterAi", org.enginehub.craftbook.mechanics.AIMechanic.class, MechanicCategory.GENERAL);
//        registerMechanic("HeadDrops", org.enginehub.craftbook.mechanics.headdrops.HeadDrops.class, MechanicCategory.GENERAL);
//        registerMechanic("BetterLeads", org.enginehub.craftbook.mechanics.BetterLeads.class, MechanicCategory.GENERAL);
//        registerMechanic("TreeLopper", org.enginehub.craftbook.mechanics.TreeLopper.class, MechanicCategory.GENERAL);
//        registerMechanic("MapChanger", org.enginehub.craftbook.mechanics.MapChanger.class, MechanicCategory.GENERAL);
//        registerMechanic("XpStorer", org.enginehub.craftbook.mechanics.XPStorer.class, MechanicCategory.GENERAL);
//        registerMechanic("CommandSigns", org.enginehub.craftbook.mechanics.CommandSigns.class, MechanicCategory.GENERAL);
//        registerMechanic("LightSwitch", org.enginehub.craftbook.mechanics.LightSwitch.class, MechanicCategory.GENERAL);
//        registerMechanic("ChunkAnchor", org.enginehub.craftbook.mechanics.ChunkAnchor.class, MechanicCategory.GENERAL);
//        registerMechanic("HiddenSwitch", org.enginehub.craftbook.mechanics.HiddenSwitch.class, MechanicCategory.GENERAL);
//        registerMechanic("SignCopier", org.enginehub.craftbook.mechanics.signcopier.SignCopier.class, MechanicCategory.TOOL);
//        registerMechanic("Bridge", org.enginehub.craftbook.mechanics.area.simple.Bridge.class, MechanicCategory.GENERAL);
//        registerMechanic("Door", org.enginehub.craftbook.mechanics.area.simple.Door.class, MechanicCategory.GENERAL);
//        registerMechanic("Elevator", org.enginehub.craftbook.mechanics.Elevator.class, MechanicCategory.GENERAL);
//        registerMechanic("Teleporter", org.enginehub.craftbook.mechanics.Teleporter.class, MechanicCategory.GENERAL);
//        registerMechanic("ToggleArea", org.enginehub.craftbook.mechanics.area.Area.class, MechanicCategory.GENERAL);
//        registerMechanic("Cauldron", org.enginehub.craftbook.mechanics.cauldron.ImprovedCauldron.class, MechanicCategory.CUSTOMISATION);
//        registerMechanic("Gate", org.enginehub.craftbook.mechanics.area.simple.Gate.class, MechanicCategory.GENERAL);
//        registerMechanic("BetterPistons", org.enginehub.craftbook.mechanics.BetterPistons.class, MechanicCategory.GENERAL);
//        registerMechanic("CookingPot", org.enginehub.craftbook.mechanics.CookingPot.class, MechanicCategory.GENERAL);
//        registerMechanic("Sponge", org.enginehub.craftbook.mechanics.Sponge.class, MechanicCategory.GENERAL);
//        registerMechanic("Pay", org.enginehub.craftbook.mechanics.Payment.class, MechanicCategory.CIRCUIT);
//        registerMechanic("Pipes", org.enginehub.craftbook.mechanics.pipe.Pipes.class, MechanicCategory.CIRCUIT);
//        registerMechanic("BounceBlocks", org.enginehub.craftbook.mechanics.BounceBlocks.class, MechanicCategory.GENERAL);
//        registerMechanic("IntegratedCircuits", org.enginehub.craftbook.mechanics.ic.ICMechanic.class, MechanicCategory.CIRCUIT);
//        registerMechanic("MinecartBooster", org.enginehub.craftbook.mechanics.minecart.blocks.CartBooster.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartReverser", org.enginehub.craftbook.mechanics.minecart.blocks.CartReverser.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartSorter", org.enginehub.craftbook.mechanics.minecart.blocks.CartSorter.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartStation", org.enginehub.craftbook.mechanics.minecart.blocks.CartStation.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartEjector", org.enginehub.craftbook.mechanics.minecart.blocks.CartEjector.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartDeposit", org.enginehub.craftbook.mechanics.minecart.blocks.CartDeposit.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartTeleporter", org.enginehub.craftbook.mechanics.minecart.blocks.CartTeleporter.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartElevator", org.enginehub.craftbook.mechanics.minecart.blocks.CartLift.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartDispenser", org.enginehub.craftbook.mechanics.minecart.blocks.CartDispenser.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartMessenger", org.enginehub.craftbook.mechanics.minecart.blocks.CartMessenger.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartMaxSpeed", org.enginehub.craftbook.mechanics.minecart.blocks.CartMaxSpeed.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartMoreRails", org.enginehub.craftbook.mechanics.minecart.MoreRails.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartRemoveEntities", org.enginehub.craftbook.mechanics.minecart.RemoveEntities.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartVisionSteering", org.enginehub.craftbook.mechanics.minecart.VisionSteering.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartDecay", org.enginehub.craftbook.mechanics.minecart.EmptyDecay.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartMobBlocker", org.enginehub.craftbook.mechanics.minecart.MobBlocker.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartExitRemover", org.enginehub.craftbook.mechanics.minecart.ExitRemover.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartCollisionEntry", org.enginehub.craftbook.mechanics.minecart.CollisionEntry.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartItemPickup", org.enginehub.craftbook.mechanics.minecart.ItemPickup.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartFallModifier", org.enginehub.craftbook.mechanics.minecart.FallModifier.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartConstantSpeed", org.enginehub.craftbook.mechanics.minecart.ConstantSpeed.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartRailPlacer", org.enginehub.craftbook.mechanics.minecart.RailPlacer.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartSpeedModifiers", org.enginehub.craftbook.mechanics.minecart.SpeedModifiers.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartEmptySlowdown", org.enginehub.craftbook.mechanics.minecart.EmptySlowdown.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartNoCollide", org.enginehub.craftbook.mechanics.minecart.NoCollide.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartPlaceAnywhere", org.enginehub.craftbook.mechanics.minecart.PlaceAnywhere.class, MechanicCategory.MINECART);
//        registerMechanic("MinecartTemporaryCart", org.enginehub.craftbook.mechanics.minecart.TemporaryCart.class, MechanicCategory.MINECART);
//        registerMechanic("BoatRemoveEntities", org.enginehub.craftbook.mechanics.boat.RemoveEntities.class, MechanicCategory.BOAT);
//        registerMechanic("BoatDecay", org.enginehub.craftbook.mechanics.boat.EmptyDecay.class, MechanicCategory.BOAT);
//        registerMechanic("BoatSpeedModifiers", org.enginehub.craftbook.mechanics.boat.SpeedModifiers.class, MechanicCategory.BOAT);
//        registerMechanic("LandBoats", org.enginehub.craftbook.mechanics.boat.LandBoats.class, MechanicCategory.BOAT);
//        registerMechanic("BoatExitRemover", org.enginehub.craftbook.mechanics.boat.ExitRemover.class, MechanicCategory.BOAT);

        MechanicType.Builder
            .create()
            .id("variables")
            .name("Variables")
            .description(TranslatableComponent.of("craftbook.variables.description"))
            .className("org.enginehub.craftbook.mechanics.variables.VariableManager")
            .category(MechanicCategory.GENERAL)
            .loadPriority(LoadPriority.EARLY)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("snow")
            .name("Snow")
            .description(TranslatableComponent.of("craftbook.snow.description"))
            .className("org.enginehub.craftbook.mechanics.Snow")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("ammeter")
            .name("Ammeter")
            .description(TranslatableComponent.of("craftbook.ammeter.description"))
            .className("org.enginehub.craftbook.mechanics.Ammeter")
            .category(MechanicCategory.TOOL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("lightstone")
            .name("LightStone")
            .description(TranslatableComponent.of("craftbook.lightstone.description"))
            .className("org.enginehub.craftbook.mechanics.LightStone")
            .category(MechanicCategory.TOOL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("redstone_fire")
            .name("RedstoneFire")
            .description(TranslatableComponent.of("craftbook.redstonefire.description"))
            .className("org.enginehub.craftbook.mechanics.RedstoneFire")
            .category(MechanicCategory.CIRCUIT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("redstone_jukebox")
            .name("RedstoneJukebox")
            .description(TranslatableComponent.of("craftbook.redstonejukebox.description"))
            .className("org.enginehub.craftbook.mechanics.RedstoneJukebox")
            .category(MechanicCategory.CIRCUIT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("jack_o_lantern")
            .name("JackOLantern")
            .description(TranslatableComponent.of("craftbook.jackolantern.description"))
            .className("org.enginehub.craftbook.mechanics.JackOLantern")
            .category(MechanicCategory.CIRCUIT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("marquee")
            .name("Marquee")
            .description(TranslatableComponent.of("craftbook.marquee.description"))
            .className("org.enginehub.craftbook.mechanics.Marquee")
            .category(MechanicCategory.GENERAL)
            .dependsOn(new MechanicDependency(MechanicType.REGISTRY.get("variables")))
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("better_plants")
            .name("BetterPlants")
            .description(TranslatableComponent.of("craftbook.betterplants.description"))
            .className("org.enginehub.craftbook.mechanics.BetterPlants")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("better_physics")
            .name("BetterPhysics")
            .description(TranslatableComponent.of("craftbook.betterphysics.description"))
            .className("org.enginehub.craftbook.mechanics.BetterPhysics")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("redstone_glowstone")
            .name("RedstoneGlowstone")
            .description(TranslatableComponent.of("craftbook.redstoneglowstone.description"))
            .className("org.enginehub.craftbook.mechanics.RedstoneGlowstone")
            .category(MechanicCategory.CIRCUIT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("readable_bookshelf")
            .name("ReadableBookshelf")
            .description(TranslatableComponent.of("craftbook.readablebookshelf.description"))
            .className("org.enginehub.craftbook.mechanics.ReadableBookshelf")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("chairs")
            .name("Chairs")
            .description(TranslatableComponent.of("craftbook.chairs.description"))
            .className("org.enginehub.craftbook.mechanics.Chairs")
            .category(MechanicCategory.GENERAL)
            .dependsOn(new PluginDependency("ProtocolLib"))
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("painting_switcher")
            .name("PaintingSwitcher")
            .description(TranslatableComponent.of("craftbook.paintingswitcher.description"))
            .className("org.enginehub.craftbook.mechanics.PaintingSwitcher")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        // TODO CommandItems needs to load early (after variables).
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
