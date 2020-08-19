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

import org.enginehub.craftbook.mechanics.AIMechanic;
import org.enginehub.craftbook.mechanics.Ammeter;
import org.enginehub.craftbook.mechanics.BetterLeads;
import org.enginehub.craftbook.mechanics.BetterPhysics;
import org.enginehub.craftbook.mechanics.BetterPistons;
import org.enginehub.craftbook.mechanics.BetterPlants;
import org.enginehub.craftbook.mechanics.ReadableBookshelf;
import org.enginehub.craftbook.mechanics.BounceBlocks;
import org.enginehub.craftbook.mechanics.Chair;
import org.enginehub.craftbook.mechanics.ChunkAnchor;
import org.enginehub.craftbook.mechanics.CommandSigns;
import org.enginehub.craftbook.mechanics.CookingPot;
import org.enginehub.craftbook.mechanics.Elevator;
import org.enginehub.craftbook.mechanics.RedstoneGlowstone;
import org.enginehub.craftbook.mechanics.HiddenSwitch;
import org.enginehub.craftbook.mechanics.JackOLantern;
import org.enginehub.craftbook.mechanics.LightStone;
import org.enginehub.craftbook.mechanics.LightSwitch;
import org.enginehub.craftbook.mechanics.MapChanger;
import org.enginehub.craftbook.mechanics.Marquee;
import org.enginehub.craftbook.mechanics.PaintingSwitch;
import org.enginehub.craftbook.mechanics.Payment;
import org.enginehub.craftbook.mechanics.RedstoneFire;
import org.enginehub.craftbook.mechanics.RedstoneJukebox;
import org.enginehub.craftbook.mechanics.Snow;
import org.enginehub.craftbook.mechanics.Sponge;
import org.enginehub.craftbook.mechanics.Teleporter;
import org.enginehub.craftbook.mechanics.TreeLopper;
import org.enginehub.craftbook.mechanics.XPStorer;
import org.enginehub.craftbook.mechanics.area.Area;
import org.enginehub.craftbook.mechanics.area.simple.Bridge;
import org.enginehub.craftbook.mechanics.area.simple.Door;
import org.enginehub.craftbook.mechanics.area.simple.Gate;
import org.enginehub.craftbook.mechanics.boat.LandBoats;
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
import org.enginehub.craftbook.mechanics.minecart.ExitRemover;
import org.enginehub.craftbook.mechanics.minecart.FallModifier;
import org.enginehub.craftbook.mechanics.minecart.ItemPickup;
import org.enginehub.craftbook.mechanics.minecart.MobBlocker;
import org.enginehub.craftbook.mechanics.minecart.MoreRails;
import org.enginehub.craftbook.mechanics.minecart.NoCollide;
import org.enginehub.craftbook.mechanics.minecart.PlaceAnywhere;
import org.enginehub.craftbook.mechanics.minecart.RailPlacer;
import org.enginehub.craftbook.mechanics.minecart.RemoveEntities;
import org.enginehub.craftbook.mechanics.minecart.SpeedModifiers;
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
import org.enginehub.craftbook.mechanics.variables.VariableManager;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class MechanicTypes {

    @Nullable
    public static final MechanicType<Ammeter> AMMETER = get("ammeter");
    @Nullable
    public static final MechanicType<AIMechanic> BETTER_AI = get("better_ai");
    @Nullable
    public static final MechanicType<BetterLeads> BETTER_LEADS = get("better_leads");
    @Nullable
    public static final MechanicType<BetterPhysics> BETTER_PHYSICS = get("better_physics");
    @Nullable
    public static final MechanicType<BetterPistons> BETTER_PISTONS = get("better_pistons");
    @Nullable
    public static final MechanicType<BetterPlants> BETTER_PLANTS = get("better_plants");
    @Nullable
    public static final MechanicType<EmptyDecay> BOAT_DECAY = get("boat_decay");
    @Nullable
    public static final MechanicType<ExitRemover> BOAT_EXIT_REMOVER = get("boat_exit_remover");
    @Nullable
    public static final MechanicType<RemoveEntities> BOAT_REMOVE_ENTITIES = get("boat_remove_entities");
    @Nullable
    public static final MechanicType<SpeedModifiers> BOAT_SPEED_MODIFIERS = get("boat_speed_modifiers");
    @Nullable
    public static final MechanicType<BounceBlocks> BOUNCE_BLOCKS = get("bounce_blocks");
    @Nullable
    public static final MechanicType<Bridge> BRIDGE = get("bridge");
    @Nullable
    public static final MechanicType<ImprovedCauldron> CAULDRON = get("cauldron");
    @Nullable
    public static final MechanicType<Chair> CHAIRS = get("chairs");
    @Nullable
    public static final MechanicType<ChunkAnchor> CHUNK_ANCHOR = get("chunk_anchor");
    @Nullable
    public static final MechanicType<CommandItems> COMMAND_ITEMS = get("command_items");
    @Nullable
    public static final MechanicType<CommandSigns> COMMAND_SIGNS = get("command_signs");
    @Nullable
    public static final MechanicType<CookingPot> COOKING_POT = get("cooking_pot");
    @Nullable
    public static final MechanicType<CustomCrafting> CUSTOM_CRAFTING = get("custom_crafting");
    @Nullable
    public static final MechanicType<CustomDrops> CUSTOM_DROPS = get("custom_drops");
    @Nullable
    public static final MechanicType<DispenserRecipes> DISPENSER_RECIPES = get("dispenser_recipes");
    @Nullable
    public static final MechanicType<Door> DOOR = get("door");
    @Nullable
    public static final MechanicType<Elevator> ELEVATOR = get("elevator");
    @Nullable
    public static final MechanicType<Gate> GATE = get("gate");
    @Nullable
    public static final MechanicType<HeadDrops> HEAD_DROPS = get("head_drops");
    @Nullable
    public static final MechanicType<HiddenSwitch> HIDDEN_SWITCH = get("hidden_switch");
    @Nullable
    public static final MechanicType<ICMechanic> INTEGRATED_CIRCUITS = get("integrated_circuits");
    @Nullable
    public static final MechanicType<JackOLantern> JACK_O_LANTERN = get("jack_o_lantern");
    @Nullable
    public static final MechanicType<RedstoneJukebox> JUKEBOX = get("jukebox");
    @Nullable
    public static final MechanicType<LandBoats> LAND_BOATS = get("land_boats");
    @Nullable
    public static final MechanicType<LightSwitch> LIGHT_SWITCH = get("light_switch");
    @Nullable
    public static final MechanicType<LightStone> LIGHTSTONE = get("lightstone");
    @Nullable
    public static final MechanicType<MapChanger> MAP_CHANGER = get("map_changer");
    @Nullable
    public static final MechanicType<Marquee> MARQUEE = get("marquee");
    @Nullable
    public static final MechanicType<CartBooster> MINECART_BOOSTER = get("minecart_booster");
    @Nullable
    public static final MechanicType<CollisionEntry> MINECART_COLLISION_ENTRY = get("minecart_collision_entry");
    @Nullable
    public static final MechanicType<ConstantSpeed> MINECART_CONSTANT_SPEED = get("minecart_constant_speed");
    @Nullable
    public static final MechanicType<EmptyDecay> MINECART_DECAY = get("minecart_decay");
    @Nullable
    public static final MechanicType<CartDeposit> MINECART_DEPOSIT = get("minecart_deposit");
    @Nullable
    public static final MechanicType<CartDispenser> MINECART_DISPENSER = get("minecart_dispenser");
    @Nullable
    public static final MechanicType<CartEjector> MINECART_EJECTOR = get("minecart_ejector");
    @Nullable
    public static final MechanicType<CartLift> MINECART_ELEVATOR = get("minecart_elevator");
    @Nullable
    public static final MechanicType<EmptySlowdown> MINECART_EMPTY_SLOWDOWN = get("minecart_empty_slowdown");
    @Nullable
    public static final MechanicType<ExitRemover> MINECART_EXIT_REMOVER = get("minecart_exit_remover");
    @Nullable
    public static final MechanicType<FallModifier> MINECART_FALL_MODIFIER = get("minecart_fall_modifier");
    @Nullable
    public static final MechanicType<ItemPickup> MINECART_ITEM_PICKUP = get("minecart_item_pickup");
    @Nullable
    public static final MechanicType<CartMaxSpeed> MINECART_MAX_SPEED = get("minecart_max_speed");
    @Nullable
    public static final MechanicType<CartMessenger> MINECART_MESSENGER = get("minecart_messenger");
    @Nullable
    public static final MechanicType<MobBlocker> MINECART_MOB_BLOCKER = get("minecart_mob_blocker");
    @Nullable
    public static final MechanicType<MoreRails> MINECART_MORE_RAILS = get("minecart_more_rails");
    @Nullable
    public static final MechanicType<NoCollide> MINECART_NO_COLLIDE = get("minecart_no_collide");
    @Nullable
    public static final MechanicType<PlaceAnywhere> MINECART_PLACE_ANYWHERE = get("minecart_place_anywhere");
    @Nullable
    public static final MechanicType<RailPlacer> MINECART_RAIL_PLACER = get("minecart_rail_placer");
    @Nullable
    public static final MechanicType<RemoveEntities> MINECART_REMOVE_ENTITIES = get("minecart_remove_entities");
    @Nullable
    public static final MechanicType<CartReverser> MINECART_REVERSER = get("minecart_reverser");
    @Nullable
    public static final MechanicType<CartSorter> MINECART_SORTER = get("minecart_sorter");
    @Nullable
    public static final MechanicType<SpeedModifiers> MINECART_SPEED_MODIFIERS = get("minecart_speed_modifiers");
    @Nullable
    public static final MechanicType<CartStation> MINECART_STATION = get("minecart_station");
    @Nullable
    public static final MechanicType<CartTeleporter> MINECART_TELEPORTER = get("minecart_teleporter");
    @Nullable
    public static final MechanicType<TemporaryCart> MINECART_TEMPORARY_CART = get("minecart_temporary_cart");
    @Nullable
    public static final MechanicType<VisionSteering> MINECART_VISION_STEERING = get("minecart_vision_steering");
    @Nullable
    public static final MechanicType<PaintingSwitch> PAINTING_SWITCHER = get("painting_switcher");
    @Nullable
    public static final MechanicType<Payment> PAY = get("pay");
    @Nullable
    public static final MechanicType<Pipes> PIPES = get("pipes");
    @Nullable
    public static final MechanicType<ReadableBookshelf> READABLE_BOOKSHELF = get("readable_bookshelf");
    @Nullable
    public static final MechanicType<RedstoneFire> REDSTONE_FIRE = get("redstone_fire");
    @Nullable
    public static final MechanicType<RedstoneGlowstone> REDSTONE_GLOWSTONE = get("redstone_glowstone");
    @Nullable
    public static final MechanicType<SignCopier> SIGN_COPIER = get("sign_copier");
    @Nullable
    public static final MechanicType<Snow> SNOW = get("snow");
    @Nullable
    public static final MechanicType<Sponge> SPONGE = get("sponge");
    @Nullable
    public static final MechanicType<Teleporter> TELEPORTER = get("teleporter");
    @Nullable
    public static final MechanicType<Area> TOGGLE_AREA = get("toggle_area");
    @Nullable
    public static final MechanicType<TreeLopper> TREE_LOPPER = get("tree_lopper");
    @Nullable
    public static final MechanicType<VariableManager> VARIABLES = get("variables");
    @Nullable
    public static final MechanicType<XPStorer> XP_STORER = get("xp_storer");

    private MechanicTypes() {
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends CraftBookMechanic> MechanicType<T> get(final String id) {
        return (MechanicType<T>) MechanicType.REGISTRY.get(id);
    }
}
