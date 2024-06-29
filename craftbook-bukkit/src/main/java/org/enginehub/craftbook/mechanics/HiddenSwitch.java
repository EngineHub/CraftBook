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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.jspecify.annotations.Nullable;

public class HiddenSwitch extends AbstractCraftBookMechanic {

    public HiddenSwitch(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    private static boolean isValidWallSign(@Nullable Block b) {

        // Must be Wall Sign
        if (b == null || !SignUtil.isWallSign(b)) return false;
        ChangedSign s = ChangedSign.create(b, Side.FRONT);

        return PlainTextComponentSerializer.plainText().serialize(s.getLine(1)).equalsIgnoreCase("[X]");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!event.getLine(1).equalsIgnoreCase("[x]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!lplayer.hasPermission("craftbook.mech.hiddenswitch")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSignChange(event);
            return;
        }

        event.setLine(1, "[X]");
    }

    public boolean testBlock(Block switchBlock, BlockFace eventFace, Player player) {

        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(player);
        ChangedSign s = null;
        Block testBlock = null;
        if (anyside) {
            for (BlockFace face : LocationUtil.getDirectFaces()) {
                testBlock = switchBlock.getRelative(face);
                if (SignUtil.isWallSign(testBlock) && ((WallSign) testBlock.getBlockData()).getFacing() == face) {
                    s = ChangedSign.create(testBlock, Side.FRONT);
                    break;
                }
            }
        } else {
            BlockFace face = eventFace.getOppositeFace();
            testBlock = switchBlock.getRelative(face);
            if (SignUtil.isWallSign(testBlock) && ((WallSign) testBlock.getBlockData()).getFacing() == face) {
                s = ChangedSign.create(testBlock, Side.FRONT);
            }
        }

        if (s == null)
            return false;

        String line1 = PlainTextComponentSerializer.plainText().serialize(s.getLine(1));
        if (line1.equalsIgnoreCase("[X]")) {

            ItemStack itemID = null;

            String line0 = PlainTextComponentSerializer.plainText().serialize(s.getLine(0));
            if (!line0.trim().isEmpty()) {
                itemID = ItemSyntax.getItem(line0.trim());
            }

            String line2 = PlainTextComponentSerializer.plainText().serialize(s.getLine(2));
            if (!line2.trim().isEmpty())
                if (!CraftBookPlugin.inst().inGroup(player, line2.trim())) {
                    lplayer.printError("mech.group");
                    return true;
                }

            boolean success = false;

            if (!ItemUtil.isStackValid(itemID)) {
                toggleSwitches(testBlock, eventFace.getOppositeFace());
                success = true;
            } else {
                if (ItemUtil.areItemsIdentical(player.getInventory().getItemInMainHand(), itemID)
                    || ItemUtil.areItemsIdentical(player.getInventory().getItemInOffHand(), itemID)) {
                    toggleSwitches(testBlock, eventFace.getOppositeFace());
                    success = true;
                } else
                    lplayer.printError("mech.hiddenswitch.key");
            }

            if (success)
                lplayer.print("mech.hiddenswitch.toggle");

            return !lplayer.isSneaking();
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND)
            return;

        if (!(event.getBlockFace() == BlockFace.EAST || event.getBlockFace() == BlockFace.WEST
            || event.getBlockFace() == BlockFace.NORTH || event.getBlockFace() == BlockFace.SOUTH
            || event.getBlockFace() == BlockFace.UP || event.getBlockFace() == BlockFace.DOWN))
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.mech.hiddenswitch.use"))
            return;

        if (!isValidWallSign(event.getClickedBlock().getRelative(1, 0, 0))
            && !isValidWallSign(event.getClickedBlock().getRelative(-1, 0, 0))
            && !isValidWallSign(event.getClickedBlock().getRelative(0, 0, 1))
            && !isValidWallSign(event.getClickedBlock().getRelative(0, 0, -1)))
            return;

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction()))
            return;

        if (testBlock(event.getClickedBlock(), event.getBlockFace(), event.getPlayer()))
            event.setCancelled(true);
    }

    private static void toggleSwitches(Block sign, BlockFace direction) {

        BlockFace[] checkFaces = new BlockFace[4];
        checkFaces[0] = BlockFace.UP;
        checkFaces[1] = BlockFace.DOWN;

        switch (direction) {
            case EAST:
            case WEST:
                checkFaces[2] = BlockFace.NORTH;
                checkFaces[3] = BlockFace.SOUTH;
                break;
            default:
                checkFaces[2] = BlockFace.EAST;
                checkFaces[3] = BlockFace.WEST;
                break;
        }

        for (BlockFace blockFace : checkFaces) {
            final Block checkBlock = sign.getRelative(blockFace);

            if (checkBlock.getType() == Material.LEVER) {
                Powerable powerable = (Powerable) checkBlock.getBlockData();
                powerable.setPowered(!powerable.isPowered());
                checkBlock.setBlockData(powerable);
            } else if (Tag.BUTTONS.getValues().contains(checkBlock.getType())) {
                Powerable powerable = (Powerable) checkBlock.getBlockData();
                powerable.setPowered(true);
                checkBlock.setBlockData(powerable);
                powerable.setPowered(false);
                Runnable turnOff = () -> checkBlock.setBlockData(powerable);
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), turnOff, Tag.WOODEN_BUTTONS.getValues().contains(checkBlock.getType()) ? 30L : 20L);
            }
        }
    }

    private boolean anyside;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("any-side", "Allows the Hidden Switch to be activated from any side of the block.");
        anyside = config.getBoolean("any-side", true);
    }
}