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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.HiddenSwitch;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.ArrayList;
import java.util.List;

public class BukkitHiddenSwitch extends HiddenSwitch implements Listener {

    public BukkitHiddenSwitch(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        String line1 = PlainTextComponentSerializer.plainText().serialize(event.line(1));
        if (!line1.equalsIgnoreCase("[x]")) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.hiddenswitch.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of(
                    "craftbook.mechanisms.create-permission",
                    TextComponent.of(getMechanicType().getName())
                ));
            }

            SignUtil.cancelSignChange(event);
            return;
        }

        event.line(1, Component.text("[X]"));
        player.printInfo(TranslatableComponent.of("craftbook.hiddenswitch.create"));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || !event.getBlockFace().isCartesian() || event.getPlayer().isSneaking()) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.hiddenswitch.use")) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        if (testBlock(clickedBlock, event.getBlockFace(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    public boolean testBlock(Block switchBlock, BlockFace eventFace, Player player) {
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(player);
        BukkitChangedSign sign = null;
        Block testBlock = null;
        if (allowAnyFace) {
            for (BlockFace face : LocationUtil.getDirectFaces()) {
                testBlock = switchBlock.getRelative(face);
                if (SignUtil.isWallSign(testBlock) && ((WallSign) testBlock.getBlockData()).getFacing() == face) {
                    sign = BukkitChangedSign.create(testBlock, Side.FRONT);
                    break;
                }
            }
        } else {
            BlockFace face = eventFace.getOppositeFace();
            testBlock = switchBlock.getRelative(face);
            if (SignUtil.isWallSign(testBlock) && ((WallSign) testBlock.getBlockData()).getFacing() == face) {
                sign = BukkitChangedSign.create(testBlock, Side.FRONT);
            }
        }

        if (sign == null) {
            return false;
        }

        String line1 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(1));
        if (line1.equalsIgnoreCase("[X]")) {
            String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
            if (!line2.trim().isEmpty()) {
                if (!CraftBookPlugin.inst().inGroup(player, line2.trim())) {
                    lplayer.printError(TranslatableComponent.of("craftbook.hiddenswitch.not-in-group"));
                    return true;
                }
            }

            if (!ProtectionUtil.canUse(player, switchBlock.getLocation(), eventFace, Action.RIGHT_CLICK_BLOCK)) {
                return false;
            }

            if (toggleSwitches(testBlock, eventFace.getOppositeFace())) {
                lplayer.printInfo(TranslatableComponent.of("craftbook.hiddenswitch.toggle"));
                return true;
            }
        }

        return false;
    }

    private boolean toggleSwitches(Block sign, BlockFace direction) {
        List<BlockFace> checkFaces = new ArrayList<>(4);
        checkFaces.add(BlockFace.UP);
        checkFaces.add(BlockFace.DOWN);

        switch (direction) {
            case EAST:
            case WEST:
                checkFaces.add(BlockFace.NORTH);
                checkFaces.add(BlockFace.SOUTH);
                break;
            default:
                checkFaces.add(BlockFace.EAST);
                checkFaces.add(BlockFace.WEST);
                break;
        }

        boolean toggledSwitch = false;

        for (BlockFace blockFace : checkFaces) {
            final Block checkBlock = sign.getRelative(blockFace);
            final Material checkBlockType = checkBlock.getType();

            if (checkBlockType == Material.LEVER) {
                Powerable powerable = (Powerable) checkBlock.getBlockData();
                powerable.setPowered(!powerable.isPowered());
                checkBlock.setBlockData(powerable);

                toggledSwitch = true;
                break;
            } else if (Tag.BUTTONS.getValues().contains(checkBlockType)) {
                Powerable powerable = (Powerable) checkBlock.getBlockData();
                powerable.setPowered(true);
                checkBlock.setBlockData(powerable);
                powerable.setPowered(false);
                Runnable turnOff = () -> {
                    // Check if the block is still a button, as it could have been removed
                    if (checkBlock.getType() == checkBlockType) {
                        checkBlock.setBlockData(powerable);
                    }
                };
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), turnOff, Tag.WOODEN_BUTTONS.getValues().contains(checkBlockType) ? 30L : 20L);

                toggledSwitch = true;
                break;
            }
        }

        return toggledSwitch;
    }
}
