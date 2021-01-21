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
//        registerMechanic("CustomDrops", org.enginehub.craftbook.mechanics.drops.CustomDrops.class, MechanicCategory.CUSTOMISATION);
//        registerMechanic("BetterAi", org.enginehub.craftbook.mechanics.AIMechanic.class, MechanicCategory.GENERAL);
//        registerMechanic("HeadDrops", org.enginehub.craftbook.mechanics.headdrops.HeadDrops.class, MechanicCategory.GENERAL);
//        registerMechanic("BetterLeads", org.enginehub.craftbook.mechanics.BetterLeads.class, MechanicCategory.GENERAL);
//        registerMechanic("TreeLopper", org.enginehub.craftbook.mechanics.TreeLopper.class, MechanicCategory.GENERAL);
//        registerMechanic("XpStorer", org.enginehub.craftbook.mechanics.XPStorer.class, MechanicCategory.GENERAL);
//        registerMechanic("CommandSigns", org.enginehub.craftbook.mechanics.CommandSigns.class, MechanicCategory.GENERAL);
//        registerMechanic("LightSwitch", org.enginehub.craftbook.mechanics.LightSwitch.class, MechanicCategory.GENERAL);
//        registerMechanic("HiddenSwitch", org.enginehub.craftbook.mechanics.HiddenSwitch.class, MechanicCategory.GENERAL);
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
            .id("sign_copier")
            .name("SignCopier")
            .description(TranslatableComponent.of("craftbook.signcopier.description"))
            .className("org.enginehub.craftbook.mechanics.signcopier.SignCopier")
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

        MechanicType.Builder
            .create()
            .id("better_sponge")
            .name("BetterSponge")
            .description(TranslatableComponent.of("craftbook.bettersponge.description"))
            .className("org.enginehub.craftbook.mechanics.BetterSponge")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("head_drops")
            .name("HeadDrops")
            .description(TranslatableComponent.of("craftbook.headdrops.description"))
            .className("org.enginehub.craftbook.mechanics.headdrops.HeadDrops")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("chunk_anchor")
            .name("ChunkAnchor")
            .description(TranslatableComponent.of("craftbook.chunkanchor.description"))
            .className("org.enginehub.craftbook.mechanics.ChunkAnchor")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("boat_empty_decay")
            .name("BoatEmptyDecay")
            .description(TranslatableComponent.of("craftbook.boatemptydecay.description"))
            .className("org.enginehub.craftbook.mechanics.boat.BoatEmptyDecay")
            .category(MechanicCategory.BOAT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("boat_exit_remover")
            .name("BoatExitRemover")
            .description(TranslatableComponent.of("craftbook.boatexitremover.description"))
            .className("org.enginehub.craftbook.mechanics.boat.BoatExitRemover")
            .category(MechanicCategory.BOAT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("boat_impact_damage")
            .name("BoatImpactDamage")
            .description(TranslatableComponent.of("craftbook.boatimpactdamage.description"))
            .className("org.enginehub.craftbook.mechanics.boat.BoatImpactDamage")
            .category(MechanicCategory.BOAT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_collision_entry")
            .name("MinecartCollisionEntry")
            .description(TranslatableComponent.of("craftbook.minecartcollisionentry.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.MinecartCollisionEntry")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_empty_decay")
            .name("MinecartEmptyDecay")
            .description(TranslatableComponent.of("craftbook.minecartemptydecay.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.MinecartEmptyDecay")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_rail_placer")
            .name("MinecartRailPlacer")
            .description(TranslatableComponent.of("craftbook.minecartrailplacer.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.MinecartRailPlacer")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_item_pickup")
            .name("MinecartItemPickup")
            .description(TranslatableComponent.of("craftbook.minecartitempickup.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.MinecartItemPickup")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("temporary_cart")
            .name("TemporaryCart")
            .description(TranslatableComponent.of("craftbook.temporarycart.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.TemporaryCart")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_exit_remover")
            .name("MinecartExitRemover")
            .description(TranslatableComponent.of("craftbook.minecartexitremover.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.MinecartExitRemover")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_physics_control")
            .name("MinecartPhysicsControl")
            .description(TranslatableComponent.of("craftbook.minecartphysicscontrol.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.MinecartPhysicsControl")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_no_collide")
            .name("MinecartNoCollide")
            .description(TranslatableComponent.of("craftbook.minecartnocollide.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.MinecartNoCollide")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_impact_damage")
            .name("MinecartImpactDamage")
            .description(TranslatableComponent.of("craftbook.minecartimpactdamage.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.MinecartImpactDamage")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("more_rails")
            .name("MoreRails")
            .description(TranslatableComponent.of("craftbook.morerails.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.MoreRails")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("elevator")
            .name("Elevator")
            .description(TranslatableComponent.of("craftbook.elevator.description"))
            .className("org.enginehub.craftbook.mechanics.Elevator")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("dispenser_recipes")
            .name("DispenserRecipes")
            .description(TranslatableComponent.of("craftbook.dispenserrecipes.description"))
            .className("org.enginehub.craftbook.mechanics.dispenser.DispenserRecipes")
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
