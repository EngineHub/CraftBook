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

package org.enginehub.craftbook.bukkit.mechanic;

import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicCategory;
import org.enginehub.craftbook.mechanic.MechanicManager;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanic.load.LoadPriority;
import org.enginehub.craftbook.mechanic.load.MechanicDependency;

public class BukkitMechanicManager extends MechanicManager {

    @Override
    public void setup() {
        //        registerMechanic("CommandItems", org.enginehub.craftbook.mechanics.items.CommandItems.class, MechanicCategory.CUSTOMISATION);
        //        registerMechanic("CustomCrafting", org.enginehub.craftbook.mechanics.crafting.CustomCrafting.class, MechanicCategory.CUSTOMISATION);
        //        registerMechanic("CustomDrops", org.enginehub.craftbook.mechanics.drops.CustomDrops.class, MechanicCategory.CUSTOMISATION);
        //        registerMechanic("CommandSigns", org.enginehub.craftbook.mechanics.CommandSigns.class, MechanicCategory.GENERAL);
        //        registerMechanic("Cauldron", org.enginehub.craftbook.mechanics.cauldron.ImprovedCauldron.class, MechanicCategory.CUSTOMISATION);
        //        registerMechanic("Pay", org.enginehub.craftbook.mechanics.Payment.class, MechanicCategory.CIRCUIT);
        //        registerMechanic("Pipes", org.enginehub.craftbook.mechanics.pipe.Pipes.class, MechanicCategory.CIRCUIT);
        //        registerMechanic("IntegratedCircuits", org.enginehub.craftbook.mechanics.ic.ICMechanic.class, MechanicCategory.CIRCUIT);
        //        registerMechanic("MinecartSorter", org.enginehub.craftbook.mechanics.minecart.blocks.CartSorter.class, MechanicCategory.MINECART);
        //        registerMechanic("MinecartDeposit", org.enginehub.craftbook.mechanics.minecart.blocks.CartDeposit.class, MechanicCategory.MINECART);
        //        registerMechanic("MinecartMessenger", org.enginehub.craftbook.mechanics.minecart.blocks.CartMessenger.class, MechanicCategory.MINECART);
        //        registerMechanic("MinecartMaxSpeed", org.enginehub.craftbook.mechanics.minecart.blocks.CartMaxSpeed.class, MechanicCategory.MINECART);

        MechanicType.Builder
            .create()
            .id("variables")
            .name("Variables")
            .description(TranslatableComponent.of("craftbook.variables.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.variables.BukkitVariableManager")
            .category(MechanicCategory.GENERAL)
            .loadPriority(LoadPriority.EARLY)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("snow")
            .name("Snow")
            .description(TranslatableComponent.of("craftbook.snow.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitSnow")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("ammeter")
            .name("Ammeter")
            .description(TranslatableComponent.of("craftbook.ammeter.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitAmmeter")
            .category(MechanicCategory.TOOL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("lightstone")
            .name("LightStone")
            .description(TranslatableComponent.of("craftbook.lightstone.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitLightStone")
            .category(MechanicCategory.TOOL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("sign_copier")
            .name("SignCopier")
            .description(TranslatableComponent.of("craftbook.signcopier.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.signcopier.BukkitSignCopier")
            .category(MechanicCategory.TOOL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("redstone_fire")
            .name("RedstoneFire")
            .description(TranslatableComponent.of("craftbook.redstonefire.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitRedstoneFire")
            .category(MechanicCategory.CIRCUIT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("redstone_jukebox")
            .name("RedstoneJukebox")
            .description(TranslatableComponent.of("craftbook.redstonejukebox.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitRedstoneJukebox")
            .category(MechanicCategory.CIRCUIT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("jack_o_lantern")
            .name("JackOLantern")
            .description(TranslatableComponent.of("craftbook.jackolantern.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitJackOLantern")
            .category(MechanicCategory.CIRCUIT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("marquee")
            .name("Marquee")
            .description(TranslatableComponent.of("craftbook.marquee.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitMarquee")
            .category(MechanicCategory.GENERAL)
            .dependsOn(new MechanicDependency(org.enginehub.craftbook.mechanic.MechanicTypes.VARIABLES.get()))
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("better_plants")
            .name("BetterPlants")
            .description(TranslatableComponent.of("craftbook.betterplants.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitBetterPlants")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("better_physics")
            .name("BetterPhysics")
            .description(TranslatableComponent.of("craftbook.betterphysics.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitBetterPhysics")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("redstone_glowstone")
            .name("RedstoneGlowstone")
            .description(TranslatableComponent.of("craftbook.redstoneglowstone.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitRedstoneGlowstone")
            .category(MechanicCategory.CIRCUIT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("readable_bookshelf")
            .name("ReadableBookshelf")
            .description(TranslatableComponent.of("craftbook.readablebookshelf.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitReadableBookshelf")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("chairs")
            .name("Chairs")
            .description(TranslatableComponent.of("craftbook.chairs.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitChairs")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("painting_switcher")
            .name("PaintingSwitcher")
            .description(TranslatableComponent.of("craftbook.paintingswitcher.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitPaintingSwitcher")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("better_sponge")
            .name("BetterSponge")
            .description(TranslatableComponent.of("craftbook.bettersponge.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitBetterSponge")
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
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitChunkAnchor")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("boat_empty_decay")
            .name("BoatEmptyDecay")
            .description(TranslatableComponent.of("craftbook.boatemptydecay.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.boat.BukkitBoatEmptyDecay")
            .category(MechanicCategory.BOAT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("boat_exit_remover")
            .name("BoatExitRemover")
            .description(TranslatableComponent.of("craftbook.boatexitremover.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.boat.BukkitBoatExitRemover")
            .category(MechanicCategory.BOAT)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("boat_impact_damage")
            .name("BoatImpactDamage")
            .description(TranslatableComponent.of("craftbook.boatimpactdamage.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.boat.BukkitBoatImpactDamage")
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
            .className("org.enginehub.craftbook.bukkit.mechanics.minecart.BukkitTemporaryCart")
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
            .className("org.enginehub.craftbook.bukkit.mechanics.minecart.BukkitMoreRails")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("elevator")
            .name("Elevator")
            .description(TranslatableComponent.of("craftbook.elevator.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitElevator")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("dispenser_recipes")
            .name("DispenserRecipes")
            .description(TranslatableComponent.of("craftbook.dispenserrecipes.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.dispenser.BukkitDispenserRecipes")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("light_switch")
            .name("LightSwitch")
            .description(TranslatableComponent.of("craftbook.lightswitch.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitLightSwitch")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("better_leads")
            .name("BetterLeads")
            .description(TranslatableComponent.of("craftbook.betterleads.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitBetterLeads")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("better_ai")
            .name("BetterAI")
            .description(TranslatableComponent.of("craftbook.betterai.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.betterai.BukkitBetterAI")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_reverser")
            .name("MinecartReverser")
            .description(TranslatableComponent.of("craftbook.minecartreverser.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.blocks.CartReverser")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_booster")
            .name("MinecartBooster")
            .description(TranslatableComponent.of("craftbook.minecartbooster.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.blocks.speed.CartBooster")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_max_booster")
            .name("MinecartMaxBooster")
            .description(TranslatableComponent.of("craftbook.minecartmaxbooster.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.blocks.speed.CartMaxBooster")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_light_braker")
            .name("MinecartLightBraker")
            .description(TranslatableComponent.of("craftbook.minecartlightbraker.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.blocks.speed.CartLightBraker")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_strong_braker")
            .name("MinecartStrongBraker")
            .description(TranslatableComponent.of("craftbook.minecartstrongbraker.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.blocks.speed.CartStrongBraker")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_dispenser")
            .name("MinecartDispenser")
            .description(TranslatableComponent.of("craftbook.minecartdispenser.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.blocks.CartDispenser")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_ejector")
            .name("MinecartEjector")
            .description(TranslatableComponent.of("craftbook.minecartejector.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.blocks.CartEjector")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_elevator")
            .name("MinecartElevator")
            .description(TranslatableComponent.of("craftbook.minecartelevator.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.blocks.CartLift")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_station")
            .name("MinecartStation")
            .description(TranslatableComponent.of("craftbook.minecartstation.description"))
            .className("org.enginehub.craftbook.mechanics.minecart.blocks.station.CartStation")
            .category(MechanicCategory.MINECART)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("better_pistons")
            .name("BetterPistons")
            .description(TranslatableComponent.of("craftbook.betterpistons.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.piston.BukkitBetterPistons")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("xp_storer")
            .name("XPStorer")
            .description(TranslatableComponent.of("craftbook.xpstorer.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitXPStorer")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("teleporter")
            .name("Teleporter")
            .description(TranslatableComponent.of("craftbook.teleporter.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitTeleporter")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("cooking_pot")
            .name("CookingPot")
            .description(TranslatableComponent.of("craftbook.cookingpot.description"))
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitCookingPot")
            .category(MechanicCategory.GENERAL)
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("bridge")
            .name("Bridge")
            .description(TranslatableComponent.of("craftbook.bridge.description"))
            .category(MechanicCategory.GENERAL)
            .className("org.enginehub.craftbook.mechanics.area.Bridge")
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("door")
            .name("Door")
            .description(TranslatableComponent.of("craftbook.door.description"))
            .category(MechanicCategory.GENERAL)
            .className("org.enginehub.craftbook.mechanics.area.Door")
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("gate")
            .name("Gate")
            .description(TranslatableComponent.of("craftbook.gate.description"))
            .category(MechanicCategory.GENERAL)
            .className("org.enginehub.craftbook.mechanics.area.Gate")
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("minecart_teleporter")
            .name("MinecartTeleporter")
            .description(TranslatableComponent.of("craftbook.minecartteleporter.description"))
            .category(MechanicCategory.MINECART)
            .className("org.enginehub.craftbook.mechanics.minecart.blocks.CartTeleporter")
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("tree_lopper")
            .name("TreeLopper")
            .description(TranslatableComponent.of("craftbook.treelopper.description"))
            .category(MechanicCategory.GENERAL)
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitTreeLopper")
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("toggle_area")
            .name("ToggleArea")
            .description(TranslatableComponent.of("craftbook.togglearea.description"))
            .category(MechanicCategory.GENERAL)
            .className("org.enginehub.craftbook.bukkit.mechanics.area.clipboard.BukkitToggleArea")
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("hidden_switch")
            .name("HiddenSwitch")
            .description(TranslatableComponent.of("craftbook.hiddenswitch.description"))
            .category(MechanicCategory.GENERAL)
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitHiddenSwitch")
            .buildAndRegister();

        MechanicType.Builder
            .create()
            .id("bounce_blocks")
            .name("BounceBlocks")
            .description(TranslatableComponent.of("craftbook.bounceblocks.description"))
            .category(MechanicCategory.GENERAL)
            .className("org.enginehub.craftbook.bukkit.mechanics.BukkitBounceBlocks")
            .buildAndRegister();

        // TODO CommandItems needs to load early (after variables).
    }

    @Override
    protected void enableMechanicPlatformListeners(CraftBookMechanic mechanic) {
        if (mechanic instanceof Listener eventListener) {
            Bukkit.getPluginManager().registerEvents(eventListener, CraftBookPlugin.inst());
        }
    }

    @Override
    protected void disableMechanicPlatformListeners(CraftBookMechanic mechanic) {
        if (mechanic instanceof Listener eventListener) {
            HandlerList.unregisterAll(eventListener);
        }
    }
}
