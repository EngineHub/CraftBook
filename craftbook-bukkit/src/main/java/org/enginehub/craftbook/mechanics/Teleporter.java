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
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
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
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ParsingUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SignClickEvent;

/**
 * Teleporter mechanic; teleports players to another location based on position.
 */
public class Teleporter extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        String signLine1 = PlainTextComponentSerializer.plainText().serialize(event.line(1));
        if (!signLine1.equalsIgnoreCase("[Teleporter]")) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.mech.teleporter")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of(
                    "craftbook.mechanisms.create-permission",
                    TextComponent.of(getMechanicType().getName())
                ));
            }

            SignUtil.cancelSignChange(event);
            return;
        }

        String posLine = PlainTextComponentSerializer.plainText().serialize(event.line(2));
        if (posLine.length() > 0) {
            String[] pos = RegexUtil.COMMA_PATTERN.split(ParsingUtil.parseLine(posLine, player));
            if (pos.length <= 2) {
                player.printError(TranslatableComponent.of("craftbook.teleporter.invalid-destination"));
                SignUtil.cancelSignChange(event);
                return;
            }
        }

        player.printInfo(TranslatableComponent.of("craftbook.teleporter.create"));
        event.line(1, Component.text("[Teleporter]"));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (!teleporterButtonEnabled
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

        Block block = null;
        ChangedSign sign = null;

        if (SignUtil.isSign(event.getClickedBlock())) {
            Sign bukkitSign = (Sign) event.getClickedBlock().getState(false);
            sign = ChangedSign.create(
                event.getClickedBlock(),
                bukkitSign.getInteractableSideFor(event.getInteractionPoint()),
                bukkitSign.lines().toArray(new Component[0]),
                localPlayer
            );
            block = event.getClickedBlock();
        } else if (Tag.BUTTONS.isTagged(event.getClickedBlock().getType())) {
            Directional b = (Directional) event.getClickedBlock().getBlockData();
            Block oppositeBlock = event.getClickedBlock().getRelative(b.getFacing().getOppositeFace(), 2);
            if (SignUtil.isSign(oppositeBlock)) {
                Sign bukkitSign = (Sign) oppositeBlock.getState(false);
                for (Side side : Side.values()) {
                    String line1 = PlainTextComponentSerializer.plainText().serialize(bukkitSign.getSide(side).line(1));
                    if (line1.equals("[Teleporter]")) {
                        block = oppositeBlock;
                        sign = ChangedSign.create(block, side, bukkitSign.lines().toArray(new Component[0]), localPlayer);
                        break;
                    }
                }
            }
        } else {
            return;
        }

        if (block == null || sign == null) {
            return;
        }

        String line1 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(1));
        if (!line1.equals("[Teleporter]")) {
            return;
        }

        String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
        if (line2.length() == 0) {
            localPlayer.printError(TranslatableComponent.of("craftbook.teleporter.no-depart"));
            return;
        }

        String[] pos = RegexUtil.COMMA_PATTERN.split(line2);
        if (pos.length <= 2) {
            localPlayer.printError(TranslatableComponent.of("craftbook.teleporter.invalid-destination"));
            return;
        }

        Block destination;

        try {
            int x = Integer.parseInt(pos[0]);
            int y = Integer.parseInt(pos[1]);
            int z = Integer.parseInt(pos[2]);

            destination = block.getWorld().getBlockAt(x, y, z);
        } catch (NumberFormatException ignored) {
            localPlayer.printError(TranslatableComponent.of("craftbook.teleporter.invalid-destination"));
            return;
        }

        if (teleporterRequireSign) {
            if (!SignUtil.isSign(destination)) {
                localPlayer.printError(TranslatableComponent.of("craftbook.teleporter.no-sign"));
                return;
            }

            Sign bukkitDestSign = (Sign) destination.getState(false);
            boolean found = false;
            for (Side side : Side.values()) {
                if (bukkitDestSign.getSide(side).getLine(1).equals("[Teleporter]")) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                localPlayer.printError(TranslatableComponent.of("craftbook.teleporter.no-sign"));
                return;
            }
        }

        if (!localPlayer.hasPermission("craftbook.teleporter.use")) {
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

        if (teleporterMaxRange > 0) {
            if (localPlayer.getLocation().toVector().distanceSq(BukkitAdapter.adapt(destination.getLocation()).toVector()) > teleporterMaxRange * teleporterMaxRange) {
                localPlayer.printError(TranslatableComponent.of("craftbook.teleporter.too-far"));
                return;
            }
        }

        activateTeleporter(localPlayer, event.getPlayer(), destination);

        event.setCancelled(true);
    }

    private void activateTeleporter(CraftBookPlayer player, Player bukkitPlayer, Block destination) {
        com.sk89q.worldedit.util.Location floor = BukkitAdapter.adapt(destination.getLocation()).setY(destination.getY() + 1);
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
            player.printError(TranslatableComponent.of("craftbook.teleporter.no-floor"));
            return;
        }
        if (foundFree < 2) {
            player.printError(TranslatableComponent.of("craftbook.teleporter.obstructed"));
            return;
        }

        Location newLocation = LocationUtil.getBlockCentreTop(BukkitAdapter.adapt(floor).getBlock());
        player.trySetPosition(BukkitAdapter.adapt(newLocation).toVector(), newLocation.getPitch(), newLocation.getYaw());

        boolean teleported = bukkitPlayer.getVehicle() == null
            ? bukkitPlayer.teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN,
                TeleportFlag.Relative.X, TeleportFlag.Relative.Y, TeleportFlag.Relative.Z,
                TeleportFlag.Relative.PITCH, TeleportFlag.Relative.YAW,
                TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE,
                TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY)
            : bukkitPlayer.getVehicle().teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE);

        if (teleported) {
            player.printInfo(TranslatableComponent.of("craftbook.teleporter.teleported"));
        } else {
            player.printError(TranslatableComponent.of("craftbook.teleporter.obstructed"));
        }
    }

    private boolean teleporterRequireSign;
    private int teleporterMaxRange;
    private boolean teleporterButtonEnabled;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("require-sign", "Require a sign to be at the destination of the teleportation.");
        teleporterRequireSign = config.getBoolean("require-sign", false);

        config.setComment("max-range", "The maximum distance between the start and end of a teleporter. Set to 0 for infinite.");
        teleporterMaxRange = config.getInt("max-range", 0);

        config.setComment("enable-buttons", "Allow teleporters to be used by a button on the other side of the block.");
        teleporterButtonEnabled = config.getBoolean("enable-buttons", true);
    }
}
