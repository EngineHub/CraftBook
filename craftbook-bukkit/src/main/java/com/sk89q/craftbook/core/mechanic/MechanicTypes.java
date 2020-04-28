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

import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.mechanics.AIMechanic;
import com.sk89q.craftbook.mechanics.Ammeter;
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
import com.sk89q.craftbook.mechanics.LightStone;
import com.sk89q.craftbook.mechanics.LightSwitch;
import com.sk89q.craftbook.mechanics.MapChanger;
import com.sk89q.craftbook.mechanics.Marquee;
import com.sk89q.craftbook.mechanics.Netherrack;
import com.sk89q.craftbook.mechanics.PaintingSwitch;
import com.sk89q.craftbook.mechanics.Payment;
import com.sk89q.craftbook.mechanics.RedstoneJukebox;
import com.sk89q.craftbook.mechanics.Snow;
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
import com.sk89q.craftbook.mechanics.minecart.ExitRemover;
import com.sk89q.craftbook.mechanics.minecart.FallModifier;
import com.sk89q.craftbook.mechanics.minecart.ItemPickup;
import com.sk89q.craftbook.mechanics.minecart.MobBlocker;
import com.sk89q.craftbook.mechanics.minecart.MoreRails;
import com.sk89q.craftbook.mechanics.minecart.NoCollide;
import com.sk89q.craftbook.mechanics.minecart.PlaceAnywhere;
import com.sk89q.craftbook.mechanics.minecart.RailPlacer;
import com.sk89q.craftbook.mechanics.minecart.RemoveEntities;
import com.sk89q.craftbook.mechanics.minecart.SpeedModifiers;
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

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class MechanicTypes {

    @Nullable public static final MechanicType<Ammeter> AMMETER = get("ammeter");
    @Nullable public static final MechanicType<AIMechanic> BETTER_AI = get("better_ai");
    @Nullable public static final MechanicType<BetterLeads> BETTER_LEADS = get("better_leads");
    @Nullable public static final MechanicType<BetterPhysics> BETTER_PHYSICS = get("better_physics");
    @Nullable public static final MechanicType<BetterPistons> BETTER_PISTONS = get("better_pistons");
    @Nullable public static final MechanicType<BetterPlants> BETTER_PLANTS = get("better_plants");
    @Nullable public static final MechanicType<EmptyDecay> BOAT_DECAY = get("boat_decay");
    @Nullable public static final MechanicType<Drops> BOAT_DROPS = get("boat_drops");
    @Nullable public static final MechanicType<ExitRemover> BOAT_EXIT_REMOVER = get("boat_exit_remover");
    @Nullable public static final MechanicType<RemoveEntities> BOAT_REMOVE_ENTITIES = get("boat_remove_entities");
    @Nullable public static final MechanicType<SpeedModifiers> BOAT_SPEED_MODIFIERS = get("boat_speed_modifiers");
    @Nullable public static final MechanicType<Uncrashable> BOAT_UNCRASHABLE = get("boat_uncrashable");
    @Nullable public static final MechanicType<WaterPlaceOnly> BOAT_WATER_PLACE_ONLY = get("boat_water_place_only");
    @Nullable public static final MechanicType<Bookcase> BOOKCASE = get("bookcase");
    @Nullable public static final MechanicType<BounceBlocks> BOUNCE_BLOCKS = get("bounce_blocks");
    @Nullable public static final MechanicType<Bridge> BRIDGE = get("bridge");
    @Nullable public static final MechanicType<ImprovedCauldron> CAULDRON = get("cauldron");
    @Nullable public static final MechanicType<Chair> CHAIRS = get("chairs");
    @Nullable public static final MechanicType<ChunkAnchor> CHUNK_ANCHOR = get("chunk_anchor");
    @Nullable public static final MechanicType<CommandItems> COMMAND_ITEMS = get("command_items");
    @Nullable public static final MechanicType<CommandSigns> COMMAND_SIGNS = get("command_signs");
    @Nullable public static final MechanicType<CookingPot> COOKING_POT = get("cooking_pot");
    @Nullable public static final MechanicType<CustomCrafting> CUSTOM_CRAFTING = get("custom_crafting");
    @Nullable public static final MechanicType<CustomDrops> CUSTOM_DROPS = get("custom_drops");
    @Nullable public static final MechanicType<DispenserRecipes> DISPENSER_RECIPES = get("dispenser_recipes");
    @Nullable public static final MechanicType<Door> DOOR = get("door");
    @Nullable public static final MechanicType<Elevator> ELEVATOR = get("elevator");
    @Nullable public static final MechanicType<Gate> GATE = get("gate");
    @Nullable public static final MechanicType<GlowStone> GLOWSTONE = get("glowstone");
    @Nullable public static final MechanicType<HeadDrops> HEAD_DROPS = get("head_drops");
    @Nullable public static final MechanicType<HiddenSwitch> HIDDEN_SWITCH = get("hidden_switch");
    @Nullable public static final MechanicType<ICMechanic> INTEGRATED_CIRCUITS = get("integrated_circuits");
    @Nullable public static final MechanicType<JackOLantern> JACK_O_LANTERN = get("jack_o_lantern");
    @Nullable public static final MechanicType<RedstoneJukebox> JUKEBOX = get("jukebox");
    @Nullable public static final MechanicType<LandBoats> LAND_BOATS = get("land_boats");
    @Nullable public static final MechanicType<Cauldron> LEGACY_CAULDRON = get("legacy_cauldron");
    @Nullable public static final MechanicType<LightStone> LIGHT_STONE = get("light_stone");
    @Nullable public static final MechanicType<LightSwitch> LIGHT_SWITCH = get("light_switch");
    @Nullable public static final MechanicType<MapChanger> MAP_CHANGER = get("map_changer");
    @Nullable public static final MechanicType<Marquee> MARQUEE = get("marquee");
    @Nullable public static final MechanicType<CartBooster> MINECART_BOOSTER = get("minecart_booster");
    @Nullable public static final MechanicType<CollisionEntry> MINECART_COLLISION_ENTRY = get("minecart_collision_entry");
    @Nullable public static final MechanicType<ConstantSpeed> MINECART_CONSTANT_SPEED = get("minecart_constant_speed");
    @Nullable public static final MechanicType<EmptyDecay> MINECART_DECAY = get("minecart_decay");
    @Nullable public static final MechanicType<CartDeposit> MINECART_DEPOSIT = get("minecart_deposit");
    @Nullable public static final MechanicType<CartDispenser> MINECART_DISPENSER = get("minecart_dispenser");
    @Nullable public static final MechanicType<CartEjector> MINECART_EJECTOR = get("minecart_ejector");
    @Nullable public static final MechanicType<CartLift> MINECART_ELEVATOR = get("minecart_elevator");
    @Nullable public static final MechanicType<EmptySlowdown> MINECART_EMPTY_SLOWDOWN = get("minecart_empty_slowdown");
    @Nullable public static final MechanicType<ExitRemover> MINECART_EXIT_REMOVER = get("minecart_exit_remover");
    @Nullable public static final MechanicType<FallModifier> MINECART_FALL_MODIFIER = get("minecart_fall_modifier");
    @Nullable public static final MechanicType<ItemPickup> MINECART_ITEM_PICKUP = get("minecart_item_pickup");
    @Nullable public static final MechanicType<CartMaxSpeed> MINECART_MAX_SPEED = get("minecart_max_speed");
    @Nullable public static final MechanicType<CartMessenger> MINECART_MESSENGER = get("minecart_messenger");
    @Nullable public static final MechanicType<MobBlocker> MINECART_MOB_BLOCKER = get("minecart_mob_blocker");
    @Nullable public static final MechanicType<MoreRails> MINECART_MORE_RAILS = get("minecart_more_rails");
    @Nullable public static final MechanicType<NoCollide> MINECART_NO_COLLIDE = get("minecart_no_collide");
    @Nullable public static final MechanicType<PlaceAnywhere> MINECART_PLACE_ANYWHERE = get("minecart_place_anywhere");
    @Nullable public static final MechanicType<RailPlacer> MINECART_RAIL_PLACER = get("minecart_rail_placer");
    @Nullable public static final MechanicType<RemoveEntities> MINECART_REMOVE_ENTITIES = get("minecart_remove_entities");
    @Nullable public static final MechanicType<CartReverser> MINECART_REVERSER = get("minecart_reverser");
    @Nullable public static final MechanicType<CartSorter> MINECART_SORTER = get("minecart_sorter");
    @Nullable public static final MechanicType<SpeedModifiers> MINECART_SPEED_MODIFIERS = get("minecart_speed_modifiers");
    @Nullable public static final MechanicType<CartStation> MINECART_STATION = get("minecart_station");
    @Nullable public static final MechanicType<CartTeleporter> MINECART_TELEPORTER = get("minecart_teleporter");
    @Nullable public static final MechanicType<TemporaryCart> MINECART_TEMPORARY_CART = get("minecart_temporary_cart");
    @Nullable public static final MechanicType<VisionSteering> MINECART_VISION_STEERING = get("minecart_vision_steering");
    @Nullable public static final MechanicType<Netherrack> NETHERRACK = get("netherrack");
    @Nullable public static final MechanicType<PaintingSwitch> PAINTING_SWITCHER = get("painting_switcher");
    @Nullable public static final MechanicType<Payment> PAY = get("pay");
    @Nullable public static final MechanicType<Pipes> PIPES = get("pipes");
    @Nullable public static final MechanicType<SignCopier> SIGN_COPIER = get("sign_copier");
    @Nullable public static final MechanicType<Snow> SNOW = get("snow");
    @Nullable public static final MechanicType<Sponge> SPONGE = get("sponge");
    @Nullable public static final MechanicType<Teleporter> TELEPORTER = get("teleporter");
    @Nullable public static final MechanicType<Area> TOGGLE_AREA = get("toggle_area");
    @Nullable public static final MechanicType<TreeLopper> TREE_LOPPER = get("tree_lopper");
    @Nullable public static final MechanicType<VariableManager> VARIABLES = get("variables");
    @Nullable public static final MechanicType<XPStorer> XP_STORER = get("xp_storer");

    private MechanicTypes() {
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends CraftBookMechanic> MechanicType<T> get(final String id) {
        return (MechanicType<T>) MechanicType.REGISTRY.get(id);
    }
}
