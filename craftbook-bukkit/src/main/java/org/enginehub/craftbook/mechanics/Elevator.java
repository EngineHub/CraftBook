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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SignClickEvent;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

import java.util.Locale;
import javax.annotation.Nullable;

/**
 * The default elevator mechanism -- wall signs in a vertical column that teleport the player
 * vertically when triggered.
 */
public class Elevator extends AbstractCraftBookMechanic {

    private enum LiftType {
        UP("[Lift Up]"),
        DOWN("[Lift Down]"),
        BOTH("[Lift UpDown]"),
        RECV("[Lift]");

        private final String label;

        LiftType(String label) {
            this.label = label;
        }

        /**
         * Get the label of this lift type.
         *
         * @return The label
         */
        public String getLabel() {
            return this.label;
        }

        /**
         * Get the lift type from this label.
         *
         * @param label The label
         * @return The lift type, or null
         */
        public static LiftType fromLabel(String label) {
            for (LiftType liftType : values()) {
                if (liftType.getLabel().equalsIgnoreCase(label)) {
                    return liftType;
                }
            }

            return null;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        LiftType dir = LiftType.fromLabel(event.getLine(1));
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
        event.setLine(1, dir.getLabel());
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

        if (!Tag.BUTTONS.isTagged(event.getClickedBlock().getType())) {
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
                    relativeHeight -= (int) relativeHeight;
                    shift = relativeHeight >= 0.5 ? BlockFace.UP : BlockFace.DOWN;
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
    public Block findDestination(BlockFace shift, Block clickedBlock) {
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
                && isValidLift(CraftBookBukkitUtil.toChangedSign(clickedBlock), CraftBookBukkitUtil.toChangedSign(destination))) {
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
            if (floor.getY() == 0) {
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
                TeleportFlag.Relative.X, TeleportFlag.Relative.Y, TeleportFlag.Relative.Z,
                TeleportFlag.Relative.PITCH, TeleportFlag.Relative.YAW,
                TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE,
                TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY)
            : bukkitPlayer.getVehicle().teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE);

        if (teleported) {
            teleportFinish(player, destination, shift);
        } else {
            player.printError(TranslatableComponent.of("craftbook.elevator.obstructed"));
        }
    }

    public static void teleportFinish(CraftBookPlayer player, Block destination, BlockFace shift) {
        ChangedSign destinationSign = null;
        if (!SignUtil.isSign(destination)) {
            if (Tag.BUTTONS.isTagged(destination.getType())) {
                Switch attachable = (Switch) destination.getBlockData();
                if (SignUtil.isSign(destination.getRelative(attachable.getFacing().getOppositeFace(), 2))) {
                    destinationSign = CraftBookBukkitUtil.toChangedSign(destination.getRelative(attachable.getFacing().getOppositeFace(), 2));
                }
            }
            if (destinationSign == null) {
                return;
            }
        } else {
            destinationSign = CraftBookBukkitUtil.toChangedSign(destination);
        }

        String title = destinationSign.getLine(0);
        if (!title.isEmpty()) {
            player.printInfo(TranslatableComponent.of("craftbook.elevator.floor-notice", TextComponent.of(title, TextColor.WHITE)));
        } else {
            player.printInfo(TranslatableComponent.of(shift.getModY() > 0
                ? "craftbook.elevator.moved-up"
                : "craftbook.elevator.moved-down"
            ));
        }
    }

    public static boolean isValidLift(ChangedSign start, ChangedSign stop) {
        if (start == null || stop == null) {
            return true;
        }

        if (start.getLine(2).toLowerCase(Locale.ROOT).startsWith("to:")) {
            try {
                return stop.getLine(0).equalsIgnoreCase(RegexUtil.COLON_PATTERN.split(start.getLine(2))[0].trim());
            } catch (Exception e) {
                start.setLine(2, "");
                return false;
            }
        } else {
            return true;
        }
    }

    @Nullable
    private LiftType findLift(Block block) {
        if (!SignUtil.isSign(block)) {
            if (elevatorButtonEnabled && Tag.BUTTONS.isTagged(block.getType())) {
                Switch b = (Switch) block.getBlockData();
                Block opposite = block.getRelative(b.getFacing().getOppositeFace(), 2);
                if (SignUtil.isSign(opposite)) {
                    ChangedSign sign = new ChangedSign(opposite, null);
                    return LiftType.fromLabel(sign.getLine(1));
                }
            }
            return null;
        }

        ChangedSign sign = new ChangedSign(block, null);
        return LiftType.fromLabel(sign.getLine(1));
    }

    private boolean elevatorAllowRedstone;
    private int elevatorRedstoneRadius;
    private boolean elevatorButtonEnabled;
    private boolean elevatorLoop;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("allow-redstone", "Allows elevators to be triggered by redstone, which will move all players in a radius.");
        elevatorAllowRedstone = config.getBoolean("allow-redstone", false);

        config.setComment("redstone-player-search-radius", "The radius that elevators will look for players in when triggered by redstone.");
        elevatorRedstoneRadius = config.getInt("redstone-player-search-radius", 3);

        config.setComment("enable-buttons", "Allow elevators to be used by a button on the other side of the block.");
        elevatorButtonEnabled = config.getBoolean("enable-buttons", true);

        config.setComment("allow-looping", "Allows elevators to loop the world height. The heighest lift up will go to the next lift on the bottom of the world and vice versa.");
        elevatorLoop = config.getBoolean("allow-looping", false);
    }
}
