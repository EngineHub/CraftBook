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

import org.enginehub.craftbook.bukkit.mechanics.BukkitAmmeter;
import org.enginehub.craftbook.bukkit.mechanics.BukkitBetterLeads;
import org.enginehub.craftbook.bukkit.mechanics.BukkitBetterPhysics;
import org.enginehub.craftbook.bukkit.mechanics.BukkitBetterPlants;
import org.enginehub.craftbook.bukkit.mechanics.BukkitBetterSponge;
import org.enginehub.craftbook.bukkit.mechanics.BukkitChairs;
import org.enginehub.craftbook.bukkit.mechanics.BukkitJackOLantern;
import org.enginehub.craftbook.bukkit.mechanics.BukkitLightStone;
import org.enginehub.craftbook.bukkit.mechanics.BukkitReadableBookshelf;
import org.enginehub.craftbook.bukkit.mechanics.BukkitRedstoneGlowstone;
import org.enginehub.craftbook.bukkit.mechanics.BukkitRedstoneJukebox;
import org.enginehub.craftbook.bukkit.mechanics.BukkitSnow;
import org.enginehub.craftbook.bukkit.mechanics.BukkitTeleporter;
import org.enginehub.craftbook.bukkit.mechanics.BukkitTreeLopper;
import org.enginehub.craftbook.bukkit.mechanics.BukkitXPStorer;
import org.enginehub.craftbook.bukkit.mechanics.betterai.BukkitBetterAI;
import org.enginehub.craftbook.bukkit.mechanics.boat.BukkitBoatEmptyDecay;
import org.enginehub.craftbook.bukkit.mechanics.boat.BukkitBoatExitRemover;
import org.enginehub.craftbook.bukkit.mechanics.boat.BukkitBoatImpactDamage;
import org.enginehub.craftbook.bukkit.mechanics.dispenser.BukkitDispenserRecipes;
import org.enginehub.craftbook.bukkit.mechanics.piston.BukkitBetterPistons;
import org.enginehub.craftbook.bukkit.mechanics.signcopier.BukkitSignCopier;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.ChunkAnchor;
import org.enginehub.craftbook.mechanics.CookingPot;
import org.enginehub.craftbook.mechanics.Elevator;
import org.enginehub.craftbook.mechanics.LightSwitch;
import org.enginehub.craftbook.mechanics.Marquee;
import org.enginehub.craftbook.mechanics.PaintingSwitcher;
import org.enginehub.craftbook.mechanics.RedstoneFire;
import org.enginehub.craftbook.mechanics.area.Bridge;
import org.enginehub.craftbook.mechanics.area.Door;
import org.enginehub.craftbook.mechanics.area.Gate;
import org.enginehub.craftbook.mechanics.area.clipboard.ToggleArea;
import org.enginehub.craftbook.mechanics.headdrops.HeadDrops;
import org.enginehub.craftbook.mechanics.minecart.MinecartCollisionEntry;
import org.enginehub.craftbook.mechanics.minecart.MinecartEmptyDecay;
import org.enginehub.craftbook.mechanics.minecart.MinecartExitRemover;
import org.enginehub.craftbook.mechanics.minecart.MinecartImpactDamage;
import org.enginehub.craftbook.mechanics.minecart.MinecartItemPickup;
import org.enginehub.craftbook.mechanics.minecart.MinecartNoCollide;
import org.enginehub.craftbook.mechanics.minecart.MinecartPhysicsControl;
import org.enginehub.craftbook.mechanics.minecart.MinecartRailPlacer;
import org.enginehub.craftbook.mechanics.minecart.MoreRails;
import org.enginehub.craftbook.mechanics.minecart.TemporaryCart;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartDispenser;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartEjector;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartLift;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartReverser;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartTeleporter;
import org.enginehub.craftbook.mechanics.minecart.blocks.speed.CartBooster;
import org.enginehub.craftbook.mechanics.minecart.blocks.speed.CartLightBraker;
import org.enginehub.craftbook.mechanics.minecart.blocks.speed.CartMaxBooster;
import org.enginehub.craftbook.mechanics.minecart.blocks.speed.CartStrongBraker;
import org.enginehub.craftbook.mechanics.minecart.blocks.station.CartStation;
import org.enginehub.craftbook.mechanics.variables.VariableManager;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
public class MechanicTypes {

    public static final @Nullable MechanicType<BukkitAmmeter> AMMETER = get("ammeter");
    public static final @Nullable MechanicType<BukkitBetterAI> BETTER_AI = get("better_ai");
    public static final @Nullable MechanicType<BukkitBetterLeads> BETTER_LEADS = get("better_leads");
    public static final @Nullable MechanicType<BukkitBetterPhysics> BETTER_PHYSICS = get("better_physics");
    public static final @Nullable MechanicType<BukkitBetterPistons> BETTER_PISTONS = get("better_pistons");
    public static final @Nullable MechanicType<BukkitBetterPlants> BETTER_PLANTS = get("better_plants");
    public static final @Nullable MechanicType<BukkitBetterSponge> BETTER_SPONGE = get("better_sponge");
    public static final @Nullable MechanicType<BukkitBoatEmptyDecay> BOAT_EMPTY_DECAY = get("boat_empty_decay");
    public static final @Nullable MechanicType<BukkitBoatExitRemover> BOAT_EXIT_REMOVER = get("boat_exit_remover");
    public static final @Nullable MechanicType<BukkitBoatImpactDamage> BOAT_IMPACT_DAMAGE = get("boat_impact_damage");
    public static final @Nullable MechanicType<Bridge> BRIDGE = get("bridge");
    public static final @Nullable MechanicType<BukkitChairs> CHAIRS = get("chairs");
    public static final @Nullable MechanicType<ChunkAnchor> CHUNK_ANCHOR = get("chunk_anchor");
    public static final @Nullable MechanicType<CookingPot> COOKING_POT = get("cooking_pot");
    public static final @Nullable MechanicType<BukkitDispenserRecipes> DISPENSER_RECIPES = get("dispenser_recipes");
    public static final @Nullable MechanicType<Door> DOOR = get("door");
    public static final @Nullable MechanicType<Elevator> ELEVATOR = get("elevator");
    public static final @Nullable MechanicType<Gate> GATE = get("gate");
    public static final @Nullable MechanicType<HeadDrops> HEAD_DROPS = get("head_drops");
    public static final @Nullable MechanicType<BukkitJackOLantern> JACK_O_LANTERN = get("jack_o_lantern");
    public static final @Nullable MechanicType<LightSwitch> LIGHT_SWITCH = get("light_switch");
    public static final @Nullable MechanicType<BukkitLightStone> LIGHTSTONE = get("lightstone");
    public static final @Nullable MechanicType<Marquee> MARQUEE = get("marquee");
    public static final @Nullable MechanicType<CartBooster> MINECART_BOOSTER = get("minecart_booster");
    public static final @Nullable MechanicType<MinecartCollisionEntry> MINECART_COLLISION_ENTRY = get("minecart_collision_entry");
    public static final @Nullable MechanicType<CartDispenser> MINECART_DISPENSER = get("minecart_dispenser");
    public static final @Nullable MechanicType<CartEjector> MINECART_EJECTOR = get("minecart_ejector");
    public static final @Nullable MechanicType<CartLift> MINECART_ELEVATOR = get("minecart_elevator");
    public static final @Nullable MechanicType<MinecartEmptyDecay> MINECART_EMPTY_DECAY = get("minecart_empty_decay");
    public static final @Nullable MechanicType<MinecartExitRemover> MINECART_EXIT_REMOVER = get("minecart_exit_remover");
    public static final @Nullable MechanicType<MinecartImpactDamage> MINECART_IMPACT_DAMAGE = get("minecart_impact_damage");
    public static final @Nullable MechanicType<MinecartItemPickup> MINECART_ITEM_PICKUP = get("minecart_item_pickup");
    public static final @Nullable MechanicType<CartLightBraker> MINECART_LIGHT_BRAKER = get("minecart_light_braker");
    public static final @Nullable MechanicType<CartMaxBooster> MINECART_MAX_BOOSTER = get("minecart_max_booster");
    public static final @Nullable MechanicType<MinecartNoCollide> MINECART_NO_COLLIDE = get("minecart_no_collide");
    public static final @Nullable MechanicType<MinecartPhysicsControl> MINECART_PHYSICS_CONTROL = get("minecart_physics_control");
    public static final @Nullable MechanicType<MinecartRailPlacer> MINECART_RAIL_PLACER = get("minecart_rail_placer");
    public static final @Nullable MechanicType<CartReverser> MINECART_REVERSER = get("minecart_reverser");
    public static final @Nullable MechanicType<CartStation> MINECART_STATION = get("minecart_station");
    public static final @Nullable MechanicType<CartStrongBraker> MINECART_STRONG_BRAKER = get("minecart_strong_braker");
    public static final @Nullable MechanicType<CartTeleporter> MINECART_TELEPORTER = get("minecart_teleporter");
    public static final @Nullable MechanicType<MoreRails> MORE_RAILS = get("more_rails");
    public static final @Nullable MechanicType<PaintingSwitcher> PAINTING_SWITCHER = get("painting_switcher");
    public static final @Nullable MechanicType<BukkitReadableBookshelf> READABLE_BOOKSHELF = get("readable_bookshelf");
    public static final @Nullable MechanicType<RedstoneFire> REDSTONE_FIRE = get("redstone_fire");
    public static final @Nullable MechanicType<BukkitRedstoneGlowstone> REDSTONE_GLOWSTONE = get("redstone_glowstone");
    public static final @Nullable MechanicType<BukkitRedstoneJukebox> REDSTONE_JUKEBOX = get("redstone_jukebox");
    public static final @Nullable MechanicType<BukkitSignCopier> SIGN_COPIER = get("sign_copier");
    public static final @Nullable MechanicType<BukkitSnow> SNOW = get("snow");
    public static final @Nullable MechanicType<BukkitTeleporter> TELEPORTER = get("teleporter");
    public static final @Nullable MechanicType<TemporaryCart> TEMPORARY_CART = get("temporary_cart");
    public static final @Nullable MechanicType<ToggleArea> TOGGLE_AREA = get("toggle_area");
    public static final @Nullable MechanicType<BukkitTreeLopper> TREE_LOPPER = get("tree_lopper");
    public static final @Nullable MechanicType<VariableManager> VARIABLES = get("variables");
    public static final @Nullable MechanicType<BukkitXPStorer> XP_STORER = get("xp_storer");

    private MechanicTypes() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends CraftBookMechanic> @Nullable MechanicType<T> get(final String id) {
        return (MechanicType<T>) MechanicType.REGISTRY.get(id);
    }
}
