package com.sk89q.craftbook.mech;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;

public class HiddenSwitch extends AbstractCraftBookMechanic {

    private static boolean isValidWallSign(Block b) {

        // Must be Wall Sign
        if (b == null || b.getType() != Material.WALL_SIGN) return false;
        ChangedSign s = BukkitUtil.toChangedSign(b);

        return s.getLine(1).equalsIgnoreCase("[X]");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!event.getLine(1).equalsIgnoreCase("[x]")) return;
        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.hiddenswitch")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        event.setLine(1, "[X]");
    }

    public boolean testBlock(Block switchBlock, BlockFace eventFace, Player player) {

        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(player);
        ChangedSign s = null;
        Block testBlock = null;
        if(CraftBookPlugin.inst().getConfiguration().hiddenSwitchAnyside) {

            for(BlockFace face : LocationUtil.getDirectFaces()) {
                testBlock = switchBlock.getRelative(face);
                if(testBlock.getType() == Material.WALL_SIGN) {
                    s = BukkitUtil.toChangedSign(testBlock);
                    break;
                }
            }
        } else {
            BlockFace face = eventFace.getOppositeFace();
            testBlock = switchBlock.getRelative(face);
            if(testBlock.getType() == Material.WALL_SIGN)
                s = BukkitUtil.toChangedSign(testBlock);
        }

        if(s == null)
            return false;

        if (s.getLine(1).equalsIgnoreCase("[X]")) {

            ItemStack itemID = null;

            if (!s.getLine(0).trim().isEmpty()) {
                itemID = ItemSyntax.getItem(s.getLine(0).trim());
            }

            if (!s.getLine(2).trim().isEmpty())
                if (!CraftBookPlugin.inst().inGroup(player, s.getLine(2).trim())) {
                    lplayer.printError("mech.group");
                    return true;
                }

            boolean success = false;

            if (!ItemUtil.isStackValid(itemID)) {
                toggleSwitches(testBlock, eventFace.getOppositeFace());
                success = true;
            } else {
                if (ItemUtil.areItemsIdentical(player.getItemInHand(), itemID)) {
                    toggleSwitches(testBlock, eventFace.getOppositeFace());
                    success = true;
                } else
                    lplayer.printError("mech.hiddenswitch.key");
            }

            if(success)
                lplayer.print("mech.hiddenswitch.toggle");

            if (!lplayer.isSneaking()) return true;
        }

        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if(event.getClickedBlock().getType() != Material.WALL_SIGN) return;

        if(!SignUtil.doesSignHaveText(event.getClickedBlock(), "[X]", 1)) return;

        if (!(event.getBlockFace() == BlockFace.EAST || event.getBlockFace() == BlockFace.WEST
                || event.getBlockFace() == BlockFace.NORTH || event.getBlockFace() == BlockFace.SOUTH
                || event.getBlockFace() == BlockFace.UP || event.getBlockFace() == BlockFace.DOWN))
            return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!player.hasPermission("craftbook.mech.hiddenswitch.use"))
            return;

        if (!isValidWallSign(event.getClickedBlock().getRelative(1, 0, 0)) && !isValidWallSign(event.getClickedBlock().getRelative(-1, 0, 0)) && !isValidWallSign(event.getClickedBlock().getRelative(0, 0, 1)) && !isValidWallSign(event.getClickedBlock().getRelative(0, 0, -1)))
            return;

        if(testBlock(event.getClickedBlock(), event.getBlockFace(), event.getPlayer()))
            event.setCancelled(true);
    }

    private void toggleSwitches(Block sign, BlockFace direction) {

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
                checkBlock.setData((byte) (checkBlock.getData() ^ 0x8));
            } else if (checkBlock.getType() == Material.STONE_BUTTON || checkBlock.getType() == Material.WOOD_BUTTON) {
                checkBlock.setData((byte) (checkBlock.getData() | 0x8));

                Runnable turnOff = new Runnable() {
                    @Override
                    public void run() {
                        checkBlock.setData((byte) (checkBlock.getData() & ~0x8));
                    }
                };
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), turnOff, 1 * 20L);
            }
        }
    }
}