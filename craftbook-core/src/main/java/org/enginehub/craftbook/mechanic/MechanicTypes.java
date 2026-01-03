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

package org.enginehub.craftbook.mechanic;

import org.enginehub.craftbook.mechanics.Ammeter;
import org.enginehub.craftbook.mechanics.BetterLeads;
import org.enginehub.craftbook.mechanics.BetterPhysics;
import org.enginehub.craftbook.mechanics.BetterPlants;
import org.enginehub.craftbook.mechanics.BetterSponge;
import org.enginehub.craftbook.mechanics.BounceBlocks;
import org.enginehub.craftbook.mechanics.Chairs;
import org.enginehub.craftbook.mechanics.ChunkAnchor;
import org.enginehub.craftbook.mechanics.CookingPot;
import org.enginehub.craftbook.mechanics.Elevator;
import org.enginehub.craftbook.mechanics.HiddenSwitch;
import org.enginehub.craftbook.mechanics.JackOLantern;
import org.enginehub.craftbook.mechanics.LightStone;
import org.enginehub.craftbook.mechanics.LightSwitch;
import org.enginehub.craftbook.mechanics.Marquee;
import org.enginehub.craftbook.mechanics.PaintingSwitcher;
import org.enginehub.craftbook.mechanics.ReadableBookshelf;
import org.enginehub.craftbook.mechanics.RedstoneFire;
import org.enginehub.craftbook.mechanics.RedstoneGlowstone;
import org.enginehub.craftbook.mechanics.RedstoneJukebox;
import org.enginehub.craftbook.mechanics.Snow;
import org.enginehub.craftbook.mechanics.Teleporter;
import org.enginehub.craftbook.mechanics.TreeLopper;
import org.enginehub.craftbook.mechanics.XPStorer;
import org.enginehub.craftbook.mechanics.area.clipboard.ToggleArea;
import org.enginehub.craftbook.mechanics.betterai.BetterAI;
import org.enginehub.craftbook.mechanics.boat.BoatEmptyDecay;
import org.enginehub.craftbook.mechanics.boat.BoatExitRemover;
import org.enginehub.craftbook.mechanics.boat.BoatImpactDamage;
import org.enginehub.craftbook.mechanics.dispenser.DispenserRecipes;
import org.enginehub.craftbook.mechanics.headdrops.HeadDrops;
import org.enginehub.craftbook.mechanics.minecart.MoreRails;
import org.enginehub.craftbook.mechanics.minecart.TemporaryCart;
import org.enginehub.craftbook.mechanics.piston.BetterPistons;
import org.enginehub.craftbook.mechanics.signcopier.SignCopier;
import org.enginehub.craftbook.mechanics.variables.VariableManager;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MechanicTypes {

    public static final @Nullable Supplier<MechanicType<Ammeter>> AMMETER = get("ammeter");
    public static final @Nullable Supplier<MechanicType<BetterAI>> BETTER_AI = get("better_ai");
    public static final @Nullable Supplier<MechanicType<BetterLeads>> BETTER_LEADS = get("better_leads");
    public static final @Nullable Supplier<MechanicType<BetterPhysics>> BETTER_PHYSICS = get("better_physics");
    public static final @Nullable Supplier<MechanicType<BetterPistons>> BETTER_PISTONS = get("better_pistons");
    public static final @Nullable Supplier<MechanicType<BetterPlants>> BETTER_PLANTS = get("better_plants");
    public static final @Nullable Supplier<MechanicType<BetterSponge>> BETTER_SPONGE = get("better_sponge");
    public static final @Nullable Supplier<MechanicType<BoatEmptyDecay>> BOAT_EMPTY_DECAY = get("boat_empty_decay");
    public static final @Nullable Supplier<MechanicType<BoatExitRemover>> BOAT_EXIT_REMOVER = get("boat_exit_remover");
    public static final @Nullable Supplier<MechanicType<BoatImpactDamage>> BOAT_IMPACT_DAMAGE = get("boat_impact_damage");
    public static final @Nullable Supplier<MechanicType<BounceBlocks>> BOUNCE_BLOCKS = get("bounce_blocks");
    // public static final @Nullable Supplier<MechanicType<Bridge> BRIDGE = get("bridge");
    public static final @Nullable Supplier<MechanicType<Chairs>> CHAIRS = get("chairs");
    public static final @Nullable Supplier<MechanicType<ChunkAnchor>> CHUNK_ANCHOR = get("chunk_anchor");
    public static final @Nullable Supplier<MechanicType<CookingPot>> COOKING_POT = get("cooking_pot");
    public static final @Nullable Supplier<MechanicType<DispenserRecipes>> DISPENSER_RECIPES = get("dispenser_recipes");
    // public static final @Nullable Supplier<MechanicType<Door> DOOR = get("door");
    public static final @Nullable Supplier<MechanicType<Elevator>> ELEVATOR = get("elevator");
    // public static final @Nullable Supplier<MechanicType<Gate> GATE = get("gate");
    public static final @Nullable Supplier<MechanicType<HeadDrops>> HEAD_DROPS = get("head_drops");
    public static final @Nullable Supplier<MechanicType<HiddenSwitch>> HIDDEN_SWITCH = get("hidden_switch");
    public static final @Nullable Supplier<MechanicType<JackOLantern>> JACK_O_LANTERN = get("jack_o_lantern");
    public static final @Nullable Supplier<MechanicType<LightSwitch>> LIGHT_SWITCH = get("light_switch");
    public static final @Nullable Supplier<MechanicType<LightStone>> LIGHTSTONE = get("lightstone");
    public static final @Nullable Supplier<MechanicType<Marquee>> MARQUEE = get("marquee");
    // public static final @Nullable Supplier<MechanicType<CartBooster> MINECART_BOOSTER = get("minecart_booster");
    // public static final @Nullable Supplier<MechanicType<MinecartCollisionEntry> MINECART_COLLISION_ENTRY = get("minecart_collision_entry");
    // public static final @Nullable Supplier<MechanicType<CartDispenser> MINECART_DISPENSER = get("minecart_dispenser");
    // public static final @Nullable Supplier<MechanicType<CartEjector> MINECART_EJECTOR = get("minecart_ejector");
    // public static final @Nullable Supplier<MechanicType<CartLift> MINECART_ELEVATOR = get("minecart_elevator");
    // public static final @Nullable Supplier<MechanicType<MinecartEmptyDecay> MINECART_EMPTY_DECAY = get("minecart_empty_decay");
    // public static final @Nullable Supplier<MechanicType<MinecartExitRemover> MINECART_EXIT_REMOVER = get("minecart_exit_remover");
    // public static final @Nullable Supplier<MechanicType<MinecartImpactDamage> MINECART_IMPACT_DAMAGE = get("minecart_impact_damage");
    // public static final @Nullable Supplier<MechanicType<MinecartItemPickup> MINECART_ITEM_PICKUP = get("minecart_item_pickup");
    // public static final @Nullable Supplier<MechanicType<CartLightBraker> MINECART_LIGHT_BRAKER = get("minecart_light_braker");
    // public static final @Nullable Supplier<MechanicType<CartMaxBooster> MINECART_MAX_BOOSTER = get("minecart_max_booster");
    // public static final @Nullable Supplier<MechanicType<MinecartNoCollide> MINECART_NO_COLLIDE = get("minecart_no_collide");
    // public static final @Nullable Supplier<MechanicType<MinecartPhysicsControl> MINECART_PHYSICS_CONTROL = get("minecart_physics_control");
    // public static final @Nullable Supplier<MechanicType<MinecartRailPlacer> MINECART_RAIL_PLACER = get("minecart_rail_placer");
    // public static final @Nullable Supplier<MechanicType<CartReverser> MINECART_REVERSER = get("minecart_reverser");
    // public static final @Nullable Supplier<MechanicType<CartStation> MINECART_STATION = get("minecart_station");
    // public static final @Nullable Supplier<MechanicType<CartStrongBraker> MINECART_STRONG_BRAKER = get("minecart_strong_braker");
    // public static final @Nullable Supplier<MechanicType<CartTeleporter> MINECART_TELEPORTER = get("minecart_teleporter");
    public static final @Nullable Supplier<MechanicType<MoreRails>> MORE_RAILS = get("more_rails");
    public static final @Nullable Supplier<MechanicType<PaintingSwitcher>> PAINTING_SWITCHER = get("painting_switcher");
    public static final @Nullable Supplier<MechanicType<ReadableBookshelf>> READABLE_BOOKSHELF = get("readable_bookshelf");
    public static final @Nullable Supplier<MechanicType<RedstoneFire>> REDSTONE_FIRE = get("redstone_fire");
    public static final @Nullable Supplier<MechanicType<RedstoneGlowstone>> REDSTONE_GLOWSTONE = get("redstone_glowstone");
    public static final @Nullable Supplier<MechanicType<RedstoneJukebox>> REDSTONE_JUKEBOX = get("redstone_jukebox");
    public static final @Nullable Supplier<MechanicType<SignCopier>> SIGN_COPIER = get("sign_copier");
    public static final @Nullable Supplier<MechanicType<Snow>> SNOW = get("snow");
    public static final @Nullable Supplier<MechanicType<Teleporter>> TELEPORTER = get("teleporter");
    public static final @Nullable Supplier<MechanicType<TemporaryCart>> TEMPORARY_CART = get("temporary_cart");
    public static final @Nullable Supplier<MechanicType<ToggleArea>> TOGGLE_AREA = get("toggle_area");
    public static final @Nullable Supplier<MechanicType<TreeLopper>> TREE_LOPPER = get("tree_lopper");
    public static final @Nullable Supplier<MechanicType<VariableManager>> VARIABLES = get("variables");
    public static final @Nullable Supplier<MechanicType<XPStorer>> XP_STORER = get("xp_storer");

    private MechanicTypes() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends CraftBookMechanic> Supplier<@Nullable MechanicType<T>> get(final String id) {
        return () -> (MechanicType<T>) MechanicType.REGISTRY.get(id);
    }
}
