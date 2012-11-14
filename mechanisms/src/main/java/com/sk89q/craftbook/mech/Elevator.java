// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mech;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.material.Button;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * The default elevator mechanism -- wall signs in a vertical column that
 * teleport the player vertically when triggered.
 *
 * @author sk89q
 * @author hash
 */
public class Elevator extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<Elevator> {

        public Factory(MechanismsPlugin plugin) {

            this.plugin = plugin;
        }

        private final MechanismsPlugin plugin;

        /**
         * Explore around the trigger to find a functional elevator; throw if
         * things look funny.
         *
         * @param pt the trigger (should be a signpost)
         *
         * @return an Elevator if we could make a valid one, or null if this
         *         looked nothing like an elevator.
         *
         * @throws InvalidMechanismException if the area looked like it was intended to be an
         *                                   elevator, but it failed.
         */
        @Override
        public Elevator detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);
            // check if this looks at all like something we're interested in first
            Direction dir = isLift(block);
            switch (dir) {
                case UP:
                case DOWN:
                    return new Elevator(block, dir, plugin);
                case RECV:
                    throw new NoDepartureException();
                default:
                    break;
            }
            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public Elevator detect(BlockWorldVector pt, LocalPlayer player, ChangedSign sign)
                throws InvalidMechanismException, ProcessedMechanismException {

            Direction dir = isLift(sign);
            switch (dir) {
                case UP:
                    player.checkPermission("craftbook.mech.elevator");

                    player.print("mech.lift.up-sign-created");
                    sign.setLine(1, "[Lift Up]");
                    break;
                case DOWN:
                    player.checkPermission("craftbook.mech.elevator");

                    player.print("mech.lift.down-sign-created");
                    sign.setLine(1, "[Lift Down]");
                    break;
                case RECV:
                    player.checkPermission("craftbook.mech.elevator");

                    player.print("mech.lift.target-sign-created");
                    sign.setLine(1, "[Lift]");
                    break;
                default:
                    return null;
            }
            throw new ProcessedMechanismException();
        }
    }

    /**
     * @param trigger if you didn't already check if this is a wall sign with
     *                appropriate text, you're going on Santa's naughty list.
     * @param dir     the direction (UP or DOWN) in which we're looking for a destination
     *
     * @throws InvalidMechanismException
     */
    private Elevator(Block trigger, Direction dir, MechanismsPlugin plugin) throws InvalidMechanismException {

        super();
        this.trigger = trigger;
        this.plugin = plugin;

        // find destination sign
        shift = dir == Direction.UP ? BlockFace.UP : BlockFace.DOWN;
        int f = dir == Direction.UP ? trigger.getWorld().getMaxHeight() : 0;
        destination = trigger;
        if (destination.getY() == f)             // heading up from top or down from bottom
            throw new InvalidConstructionException();
        boolean loopd = false;
        while (true) {
            destination = destination.getRelative(shift);
            Direction derp = isLift(destination);
            if (derp != Direction.NONE) {
                break;   // found it!
            }
            if (destination.getY() == trigger.getY())
                throw new InvalidConstructionException();
            if (plugin.getLocalConfiguration().elevatorSettings.loop && !loopd) {
                if (destination.getY() == trigger.getWorld().getMaxHeight()) {      // hit the top of the world
                    org.bukkit.Location low = destination.getLocation();
                    low.setY(0);
                    destination = destination.getWorld().getBlockAt(low);
                    loopd = true;
                } else if (destination.getY() == 0) {        // hit the bottom of the world
                    org.bukkit.Location low = destination.getLocation();
                    low.setY(trigger.getWorld().getMaxHeight());
                    destination = destination.getWorld().getBlockAt(low);
                    loopd = true;
                }
            } else {
                if (destination.getY() == trigger.getWorld().getMaxHeight())       // hit the top of the world
                    throw new InvalidConstructionException();
                if (destination.getY() == 0)         // hit the bottom of the world
                    throw new InvalidConstructionException();
            }
        }
        // and if we made it here without exceptions, destination is set.

        // finding solid ground is deferred until a click event comes in
        // since we teleport the player straight up, and the sign can be
        // clicked from blocks other than the ones directly in the elevator
        // shaft.
    }

    private final MechanismsPlugin plugin;

    private final Block trigger;
    private final BlockFace shift;
    private Block destination;

    public static enum Direction {
        NONE, UP, DOWN, RECV
    }


    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getLocalConfiguration().elevatorSettings.enable) return;

        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger)))
            return; //wth? our manager is insane

        LocalPlayer localPlayer = plugin.wrap(event.getPlayer());

        if (!localPlayer.hasPermission("craftbook.mech.elevator.use")) {
            event.setCancelled(true);
            localPlayer.printError("mech.use-permission");
            return;
        }

        makeItSo(plugin.wrap(event.getPlayer()));

        event.setCancelled(true);
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        /* we only affect players, so we don't care about redstone events */
    }

    private void makeItSo(LocalPlayer player) {
        // start with the block shifted vertically from the player
        // to the destination sign's height (plus one).
        Block floor = destination.getWorld().getBlockAt((int) Math.floor(player.getPosition().getPosition().getX()),
                destination.getY() + 1, (int) Math.floor(player.getPosition().getPosition().getZ()));
        // well, unless that's already a ceiling.
        if (!occupiable(floor)) {
            floor = floor.getRelative(BlockFace.DOWN);
        }

        // now iterate down until we find enough open space to stand in
        // or until we're 5 blocks away, which we consider too far.
        int foundFree = 0;
        boolean foundGround = false;
        for (int i = 0; i < 5; i++) {
            if (occupiable(floor)) {
                foundFree++;
            } else {
                foundGround = true;
                break;
            }
            if (floor.getY() == 0x0) {
                break;
            }
            floor = floor.getRelative(BlockFace.DOWN);
        }
        if (!foundGround) {
            player.printError("mech.lift.no-floor");
            return;
        }
        if (foundFree < 2) {
            player.printError("mech.lift.obstruct");
            return;
        }

        // Teleport!
        Location newLocation = player.getPosition();
        newLocation = newLocation.setPosition(player.getPosition().getPosition().setY(floor.getY() + 1));
        if (player.isInsideVehicle()) {
            newLocation = player.getVehicle().getLocation();
            newLocation = newLocation.setPosition(player.getVehicle().getLocation().getPosition().setY(floor.getY() +
                    2));
            player.getVehicle().teleport(newLocation);
        }
        player.setPosition(newLocation.getPosition(), newLocation.getPitch(), newLocation.getYaw());

        // Now, we want to read the sign so we can tell the player
        // his or her floor, but as that may not be avilable, we can
        // just print a generic message
        if(!(destination.getState() instanceof Sign))
            return;
        String title = ((Sign) destination.getState()).getLines()[0];
        if (!title.isEmpty()) {
            player.print(player.translate("mech.lift.floor") + ": " + title);
        } else {
            player.print("You went " + (shift.getModY() > 0 ? "up" : "down") + " a floor.");
        }
    }

    private static Elevator.Direction isLift(Block block) {

        BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            if (MechanismsPlugin.getInst().getLocalConfiguration().elevatorSettings.buttons && (block.getTypeId() ==
                    BlockID.STONE_BUTTON || block.getTypeId() == BlockID.WOODEN_BUTTON)) {
                Button b = (Button) block.getState().getData();
                Block sign = block.getRelative(b.getAttachedFace()).getRelative(b.getAttachedFace());
                if (sign.getState() instanceof Sign)
                    return isLift(BukkitUtil.toChangedSign((Sign) sign.getState(), ((Sign)sign.getState()).getLines()));
            }
            return Direction.NONE;
        }

        return isLift(BukkitUtil.toChangedSign((Sign) state, ((Sign)state).getLines()));
    }

    private static Elevator.Direction isLift(ChangedSign sign) {
        // if you were really feeling frisky this could definitely
        // be optomized by converting the string to a char[] and then
        // doing work

        if (sign.getLine(1).equalsIgnoreCase("[Lift Up]")) return Direction.UP;
        if (sign.getLine(1).equalsIgnoreCase("[Lift Down]")) return Direction.DOWN;
        if (sign.getLine(1).equalsIgnoreCase("[Lift]")) return Direction.RECV;
        return Direction.NONE;
    }

    private static boolean occupiable(Block block) {

        return BlockType.canPassThrough(block.getTypeId());
    }

    @Override
    public void unload() {
        /* we're not persistent */
    }

    @Override
    public boolean isActive() {
        /* we're not persistent */
        return false;
    }

    private static class NoDepartureException extends InvalidMechanismException {

        private static final long serialVersionUID = 3845311158458450314L;

        public NoDepartureException() {

            super("Cannot depart from this lift (can only arrive).");
        }
    }

    private static class InvalidConstructionException extends
    InvalidMechanismException {

        private static final long serialVersionUID = 2306504048848430689L;

        public InvalidConstructionException() {

            super("This lift has no destination.");
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {

    }
}