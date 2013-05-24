// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.mech;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Button;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.BukkitVehicle;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * The default elevator mechanism -- wall signs in a vertical column that teleport the player vertically when triggered.
 *
 * @author sk89q
 * @author hash
 */
public class Elevator extends AbstractMechanic {

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    public static class Factory extends AbstractMechanicFactory<Elevator> {

        /**
         * Explore around the trigger to find a functional elevator; throw if things look funny.
         *
         * @param pt the trigger (should be a signpost)
         *
         * @return an Elevator if we could make a valid one, or null if this looked nothing like an elevator.
         *
         * @throws InvalidMechanismException if the area looked like it was intended to be an elevator, but it failed.
         */
        @Override
        public Elevator detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);
            // check if this looks at all like something we're interested in first
            Direction dir = isLift(block);
            switch (dir) {
                case UP:
                case DOWN:
                    return new Elevator(block, dir);
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
        public Elevator detect(BlockWorldVector pt, LocalPlayer player,
                ChangedSign sign) throws InvalidMechanismException,
                ProcessedMechanismException {

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
     * @param trigger if you didn't already check if this is a wall sign with appropriate text,
     *                you're going on Santa's naughty list.
     * @param dir     the direction (UP or DOWN) in which we're looking for a destination
     *
     * @throws InvalidMechanismException
     */
    private Elevator(Block trigger, Direction dir) throws InvalidMechanismException {

        super();
        this.trigger = trigger;

        // find destination sign
        shift = dir == Direction.UP ? BlockFace.UP : BlockFace.DOWN;
        int f = dir == Direction.UP ? trigger.getWorld().getMaxHeight() : 0;
        destination = trigger;
        if (destination.getY() == f) // heading up from top or down from bottom
            throw new InvalidConstructionException();
        boolean loopd = false;
        while (true) {
            destination = destination.getRelative(shift);
            Direction derp = isLift(destination);
            if (derp != Direction.NONE && isValidLift(BukkitUtil.toChangedSign(trigger),
                    BukkitUtil.toChangedSign(destination))) {
                break; // found it!
            }
            if (destination.getY() == trigger.getY()) throw new InvalidConstructionException();
            if (plugin.getConfiguration().elevatorLoop && !loopd) {
                if (destination.getY() == trigger.getWorld().getMaxHeight()) { // hit the top of the world
                    org.bukkit.Location low = destination.getLocation();
                    low.setY(0);
                    destination = destination.getWorld().getBlockAt(low);
                    loopd = true;
                } else if (destination.getY() == 0) { // hit the bottom of the world
                    org.bukkit.Location low = destination.getLocation();
                    low.setY(trigger.getWorld().getMaxHeight());
                    destination = destination.getWorld().getBlockAt(low);
                    loopd = true;
                }
            } else {
                if (destination.getY() == trigger.getWorld().getMaxHeight()) // hit the top of the world
                    throw new InvalidConstructionException();
                if (destination.getY() == 0) // hit the bottom of the world
                    throw new InvalidConstructionException();
            }
        }
        // and if we made it here without exceptions, destination is set.

        // finding solid ground is deferred until a click event comes in
        // since we teleport the player straight up, and the sign can be
        // clicked from blocks other than the ones directly in the elevator
        // shaft.
    }

    private final Block trigger;
    private final BlockFace shift;
    private Block destination;

    public static enum Direction {
        NONE, UP, DOWN, RECV
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if(task != null) {
            event.getPlayer().sendMessage("Elevator Busy!");
            return;
        }

        if (!plugin.getConfiguration().elevatorEnabled) return;

        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger)))
            return; // wth? our manager is insane

        LocalPlayer localPlayer = plugin.wrapPlayer(event.getPlayer());

        if (!localPlayer.hasPermission("craftbook.mech.elevator.use")) {
            event.setCancelled(true);
            localPlayer.printError("mech.use-permission");
            return;
        }

        makeItSo(localPlayer);

        event.setCancelled(true);
    }

    private void makeItSo(LocalPlayer player) {
        // start with the block shifted vertically from the player
        // to the destination sign's height (plus one).
        Block floor = destination.getWorld().getBlockAt((int) Math.floor(player.getPosition().getPosition().getX()),
                destination.getY() + 1,
                (int) Math.floor(player.getPosition().getPosition().getZ()));
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

        teleportPlayer(player, floor);
    }

    public void teleportPlayer(final LocalPlayer player, final Block floor) {

        final Location newLocation = BukkitUtil.toLocation(player.getPosition());
        newLocation.setY(floor.getY() + 1);

        if(CraftBookPlugin.inst().getConfiguration().elevatorSlowMove) {

            final Location lastLocation = BukkitUtil.toLocation(player.getPosition());

            task = CraftBookPlugin.inst().getServer().getScheduler().runTaskTimer(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run () {

                    Player p = ((BukkitPlayer)player).getPlayer();
                    p.setAllowFlight(true);
                    p.setFlying(true);
                    p.setFallDistance(0f);
                    p.setNoDamageTicks(2);
                    double speed = CraftBookPlugin.inst().getConfiguration().elevatorMoveSpeed;
                    newLocation.setPitch(p.getLocation().getPitch());
                    newLocation.setYaw(p.getLocation().getYaw());

                    if(Math.abs(newLocation.getY() - p.getLocation().getY()) < 0.7) {
                        p.teleport(newLocation);
                        teleportFinish(player);
                        task.cancel();
                        task = null;
                        return;
                    }

                    if(lastLocation.getBlockX() != p.getLocation().getBlockX() || lastLocation.getBlockZ() != p.getLocation().getBlockZ()) {
                        p.teleport(newLocation);
                        player.print("You have left the elevator!");
                        teleportFinish(player);
                        p.setFlying(p.getGameMode() == GameMode.CREATIVE);
                        p.setAllowFlight(p.getGameMode() == GameMode.CREATIVE);
                        task.cancel();
                        task = null;
                        return;
                    }

                    if(newLocation.getY() > p.getLocation().getY()) {
                        p.setVelocity(new Vector(0, speed,0));
                        if(!BlockType.canPassThrough(p.getLocation().add(0, 2, 0).getBlock().getTypeId()))
                            p.teleport(p.getLocation().add(0, speed, 0));
                    } else if (newLocation.getY() < p.getLocation().getY()) {
                        p.setVelocity(new Vector(0, -speed,0));
                        if(!BlockType.canPassThrough(p.getLocation().add(0, -1, 0).getBlock().getTypeId()))
                            p.teleport(p.getLocation().add(0, -speed, 0));
                    } else {
                        teleportFinish(player);
                        task.cancel();
                        task = null;
                        p.setFlying(p.getGameMode() == GameMode.CREATIVE);
                        p.setAllowFlight(p.getGameMode() == GameMode.CREATIVE);
                        return;
                    }

                    lastLocation.setY(p.getLocation().getY());
                }
            }, 1, 1);
        } else {
            // Teleport!
            if (player.isInsideVehicle()) {
                newLocation.setX(((BukkitVehicle)player.getVehicle()).getVehicle().getLocation().getX());
                newLocation.setY(floor.getY() + 2);
                newLocation.setZ(((BukkitVehicle)player.getVehicle()).getVehicle().getLocation().getZ());
                newLocation.setYaw(((BukkitVehicle)player.getVehicle()).getVehicle().getLocation().getYaw());
                newLocation.setPitch(((BukkitVehicle)player.getVehicle()).getVehicle().getLocation().getPitch());
                ((BukkitVehicle)player.getVehicle()).getVehicle().teleport(newLocation);
            }
            player.setPosition(BukkitUtil.toLocation(newLocation).getPosition(), newLocation.getPitch(), newLocation.getYaw());

            teleportFinish(player);
        }
    }

    private BukkitTask task;

    public void teleportFinish(LocalPlayer player) {
        // Now, we want to read the sign so we can tell the player
        // his or her floor, but as that may not be avilable, we can
        // just print a generic message
        Sign info = null;
        if (!(destination.getState() instanceof Sign)) {
            if (destination.getState().getData() instanceof Button) {

                Button button = (Button) destination.getState().getData();
                if (destination.getRelative(button.getAttachedFace(), 2).getState() instanceof Sign)
                    info = (Sign) destination.getRelative(button.getAttachedFace(), 2).getState();
            }
            if (info == null)
                return;
        } else
            info = (Sign) destination.getState();
        String title = info.getLines()[0];
        if (!title.isEmpty()) {
            player.print(player.translate("mech.lift.floor") + ": " + title);
        } else {
            player.print(shift.getModY() > 0 ? "mech.lift.up" : "mech.lift.down");
        }
    }

    public static boolean isValidLift(ChangedSign start, ChangedSign stop) {

        if (start == null || stop == null) return true;
        if (start.getLine(2).toLowerCase().startsWith("to:")) {
            try {
                return stop.getLine(0).equalsIgnoreCase(RegexUtil.COLON_PATTERN.split(start.getLine(2))[0].trim());
            } catch (Exception e) {
                start.setLine(2, "");
                return false;
            }
        } else return true;
    }

    private static Elevator.Direction isLift(Block block) {

        BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            if (CraftBookPlugin.inst().getConfiguration().elevatorButtonEnabled
                    && (block.getTypeId() == BlockID.STONE_BUTTON || block.getTypeId() == BlockID.WOODEN_BUTTON)) {
                Button b = (Button) block.getState().getData();
                Block sign = block.getRelative(b.getAttachedFace()).getRelative(b.getAttachedFace());
                if (sign.getState() instanceof Sign)
                    return isLift(BukkitUtil.toChangedSign((Sign) sign.getState(),
                            ((Sign) sign.getState()).getLines()));
            }
            return Direction.NONE;
        }

        return isLift(BukkitUtil.toChangedSign((Sign) state, ((Sign) state).getLines()));
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

    private static class NoDepartureException extends InvalidMechanismException {

        private static final long serialVersionUID = 3845311158458450314L;

        public NoDepartureException() {

            super("Cannot depart from this lift (can only arrive).");
        }
    }

    private static class InvalidConstructionException extends InvalidMechanismException {

        private static final long serialVersionUID = 2306504048848430689L;

        public InvalidConstructionException() {

            super("This lift has no destination.");
        }
    }
}
