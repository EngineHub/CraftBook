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

package org.enginehub.craftbook.bukkit.mechanics;

import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.events.SignClickEvent;
import org.enginehub.craftbook.bukkit.events.SourcedBlockRedstoneEvent;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.Elevator;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * The default elevator mechanism -- wall signs in a vertical column that teleport the player
 * vertically when triggered.
 */
public class BukkitElevator extends Elevator implements Listener {

    public BukkitElevator(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        LiftType dir = LiftType.fromLabel(PlainTextComponentSerializer.plainText().serialize(event.line(1)));
        if (dir == null) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.elevator.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of(
                    "craftbook.mechanisms.create-permission",
                    TextComponent.of(getMechanicType().getName())
                ));
            }

            SignUtil.cancelSignChange(event);
            return;
        }

        player.printInfo(TranslatableComponent.of("craftbook.elevator.create." + dir.name().toLowerCase(Locale.ROOT)));
        event.line(1, Component.text(dir.getLabel()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!elevatorAllowRedstone || event.isMinor() || !event.isOn()) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        LiftType dir = findLift(event.getBlock());
        if (dir != LiftType.UP && dir != LiftType.DOWN) {
            // Redstone elevators can only operate on up and down.
            return;
        }

        BlockFace shift = dir == LiftType.UP ? BlockFace.UP : BlockFace.DOWN;
        Block destination = findDestination(shift, event.getBlock());

        if (destination == null) {
            return;
        }

        for (Player player : event.getBlock().getLocation().getNearbyPlayers(elevatorRedstoneRadius)) {
            CraftBookPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(player);

            if (!localPlayer.hasPermission("craftbook.elevator.use")) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                    localPlayer.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
                }
                continue;
            }

            activateElevator(localPlayer, player, destination, shift);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (!elevatorButtonEnabled
            || event.getHand() != EquipmentSlot.HAND
            || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null || !Tag.BUTTONS.isTagged(clickedBlock.getType())) {
            return;
        }

        onCommonClick(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {
        onCommonClick(event);
    }

    public void onCommonClick(PlayerInteractEvent event) {
        if (!EventUtil.passesFilter(event)
            || event.getHand() != EquipmentSlot.HAND
            || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        CraftBookPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        Block block = event.getClickedBlock();

        // check if this looks at all like something we're interested in first
        LiftType dir = findLift(block);

        if (dir == null) {
            return;
        }

        BlockFace shift;
        switch (dir) {
            case UP:
                shift = BlockFace.UP;
                break;
            case DOWN:
                shift = BlockFace.DOWN;
                break;
            case BOTH:
                if (event.getInteractionPoint() != null) {
                    double relativeHeight = event.getInteractionPoint().getY();
                    double fractionalPart = relativeHeight - Math.floor(relativeHeight);  // Normalize to 0.0-1.0
                    if (relativeHeight < 0) {
                        // Invert behavior for negative Y coordinates
                        fractionalPart = 1.0 - fractionalPart;
                        shift = fractionalPart >= 0.5 ? BlockFace.DOWN : BlockFace.UP;
                    } else {
                        shift = fractionalPart >= 0.5 ? BlockFace.UP : BlockFace.DOWN;
                    }
                    break;
                }
                return;
            case RECV:
                localPlayer.printError(TranslatableComponent.of("craftbook.elevator.no-depart"));
                return;
            default:
                return;
        }

        Block destination = findDestination(shift, block);

        if (destination == null) {
            localPlayer.printError(TranslatableComponent.of("craftbook.elevator.no-destination"));
            return;
        }

        if (!localPlayer.hasPermission("craftbook.elevator.use")) {
            event.setCancelled(true);
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                localPlayer.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), block.getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                localPlayer.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        activateElevator(localPlayer, event.getPlayer(), destination, shift);

        // At this point, even if it failed, the user intended to use an elevator. So we'll cancel the event.
        event.setCancelled(true);
    }

    /**
     * Finds the destination based on the given lift sign.
     *
     * @param shift The direction
     * @param clickedBlock The lift block
     * @return The destination, or null if none found
     */
    public @Nullable Block findDestination(BlockFace shift, Block clickedBlock) {
        int maximumSearchPoint = shift == BlockFace.UP
            ? clickedBlock.getWorld().getMaxHeight()
            : clickedBlock.getWorld().getMinHeight();
        Block destination = clickedBlock;
        // heading up from top or down from bottom
        if (destination.getY() == maximumSearchPoint) {
            return null;
        }

        while (true) {
            destination = destination.getRelative(shift);
            LiftType destinationLiftType = findLift(destination);
            if (destinationLiftType != null
                && isValidLift(clickedBlock, destination)) {
                break; // found it!
            }

            // We're back to the start point, no elevator to find here.
            if (destination.getY() == clickedBlock.getY()) {
                return null;
            }

            if (destination.getY() == maximumSearchPoint) {
                if (elevatorLoop) {
                    Location temporaryLocation = destination.getLocation();
                    temporaryLocation.setY(
                        shift == BlockFace.UP
                        ? clickedBlock.getWorld().getMinHeight()
                        : clickedBlock.getWorld().getMaxHeight()
                    );
                    destination = temporaryLocation.getBlock();
                } else {
                    return null;
                }
            }
        }

        return destination;
    }

    private void activateElevator(CraftBookPlayer player, Player bukkitPlayer, Block destination, BlockFace shift) {
        com.sk89q.worldedit.util.Location floor = player.getLocation().setY(destination.getY() + 1);
        BlockState floorBlock = player.getWorld().getBlock(floor.toVector().toBlockPoint());
        // well, unless that's already a ceiling.
        if (floorBlock.getBlockType().getMaterial().isMovementBlocker() && !BlockCategories.SIGNS.contains(floorBlock)) {
            floor = floor.setY(floor.getY() - 1);
            floorBlock = player.getWorld().getBlock(floor.toVector().toBlockPoint());
        }

        // now iterate down until we find enough open space to stand in
        // or until we're 5 blocks away, which we consider too far.
        int foundFree = 0;
        boolean foundGround = false;
        for (int i = 0; i < 5; i++) {
            if (!floorBlock.getBlockType().getMaterial().isMovementBlocker() || BlockCategories.SIGNS.contains(floorBlock)) {
                foundFree++;
            } else {
                foundGround = true;
                break;
            }
            if (floor.getY() == player.getWorld().getMinY()) {
                break;
            }
            floor = floor.setY(floor.getY() - 1);
            floorBlock = player.getWorld().getBlock(floor.toVector().toBlockPoint());
        }
        if (!foundGround) {
            player.printError(TranslatableComponent.of("craftbook.elevator.no-floor"));
            return;
        }
        if (foundFree < 2) {
            player.printError(TranslatableComponent.of("craftbook.elevator.obstructed"));
            return;
        }

        final Location newLocation = bukkitPlayer.getLocation();
        newLocation.setY(floor.getBlockY() + 1);

        boolean teleported = bukkitPlayer.getVehicle() == null
            ? bukkitPlayer.teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN,
                TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y, TeleportFlag.Relative.VELOCITY_Z,
                TeleportFlag.Relative.VELOCITY_ROTATION)
            : bukkitPlayer.getVehicle().teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

        if (teleported) {
            teleportFinish(player, destination, shift);
        } else {
            player.printError(TranslatableComponent.of("craftbook.elevator.obstructed"));
        }
    }

    public static void teleportFinish(CraftBookPlayer player, Block destination, BlockFace shift) {
        BukkitChangedSign destinationSign = null;
        if (!SignUtil.isSign(destination)) {
            if (Tag.BUTTONS.isTagged(destination.getType()) && destination.getBlockData() instanceof Switch attachable) {
                if (SignUtil.isSign(destination.getRelative(attachable.getFacing().getOppositeFace(), 2))) {
                    Sign sign = (Sign) destination.getRelative(attachable.getFacing().getOppositeFace(), 2).getState(false);
                    for (Side side : Side.values()) {
                        if (LiftType.fromLabel(sign.getSide(side).getLine(1)) != null) {
                            destinationSign = BukkitChangedSign.create(sign, side);
                            break;
                        }
                    }
                }
            }
        } else {
            Sign sign = (Sign) destination.getState(false);
            for (Side side : Side.values()) {
                if (LiftType.fromLabel(sign.getSide(side).getLine(1)) != null) {
                    destinationSign = BukkitChangedSign.create(sign, side);
                    break;
                }
            }
        }
        if (destinationSign == null) {
            // Can't find the sign - just ignore the welcome message
            return;
        }

        String title = PlainTextComponentSerializer.plainText().serialize(destinationSign.getLine(0));
        if (!title.isEmpty()) {
            player.printInfo(TranslatableComponent.of("craftbook.elevator.floor-notice", TextComponent.of(title, TextColor.WHITE)));
        } else {
            player.printInfo(TranslatableComponent.of(shift.getModY() > 0
                ? "craftbook.elevator.moved-up"
                : "craftbook.elevator.moved-down"
            ));
        }
    }

    public static boolean isValidLift(Block start, Block stop) {
        if (!SignUtil.isSign(start) || !SignUtil.isSign(stop)) {
            return true;
        }

        Sign startSign = (Sign) start.getState(false);
        for (Side side : Side.values()) {
            if (startSign.getSide(side).getLine(2).toLowerCase(Locale.ROOT).startsWith("to:")) {
                Sign stopSign = (Sign) stop.getState(false);

                try {
                    return stopSign.getSide(side).getLine(0).equalsIgnoreCase(RegexUtil.COLON_PATTERN.split(startSign.getSide(side).getLine(2))[0].trim());
                } catch (Exception e) {
                    startSign.getSide(side).line(2, Component.text(""));
                    return false;
                }
            }
        }

        return true;
    }

    private @Nullable LiftType findLift(@Nullable Block block) {
        if (block == null) {
            return null;
        }

        if (!SignUtil.isSign(block)) {
            if (elevatorButtonEnabled && Tag.BUTTONS.isTagged(block.getType())) {
                Switch b = (Switch) block.getBlockData();
                Block opposite = block.getRelative(b.getFacing().getOppositeFace(), 2);
                if (SignUtil.isSign(opposite)) {
                    Sign sign = (Sign) opposite.getState(false);
                    for (Side side : Side.values()) {
                        LiftType type = LiftType.fromLabel(sign.getSide(side).getLine(1));
                        if (type != null) {
                            return type;
                        }
                    }
                }
            }
            return null;
        }

        Sign sign = (Sign) block.getState(false);
        for (Side side : Side.values()) {
            LiftType type = LiftType.fromLabel(sign.getSide(side).getLine(1));
            if (type != null) {
                return type;
            }
        }

        return null;
    }
}
