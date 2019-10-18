package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class HiddenSwitch extends AbstractCraftBookMechanic {

    private static boolean isValidWallSign(Block b) {

        // Must be Wall Sign
        if (b == null || !SignUtil.isWallSign(b)) return false;
        ChangedSign s = CraftBookBukkitUtil.toChangedSign(b);

        return s.getLine(1).equalsIgnoreCase("[X]");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[x]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.hiddenswitch")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        event.setLine(1, "[X]");
    }

    public boolean testBlock(Block switchBlock, BlockFace eventFace, Player player) {

        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(player);
        ChangedSign s = null;
        Block testBlock = null;
        if(anyside) {
            for(BlockFace face : LocationUtil.getDirectFaces()) {
                testBlock = switchBlock.getRelative(face);
                if(SignUtil.isWallSign(testBlock) && ((WallSign) testBlock.getBlockData()).getFacing() == face) {
                    s = CraftBookBukkitUtil.toChangedSign(testBlock);
                    break;
                }
            }
        } else {
            BlockFace face = eventFace.getOppositeFace();
            testBlock = switchBlock.getRelative(face);
            if(SignUtil.isWallSign(testBlock) && ((WallSign) testBlock.getBlockData()).getFacing() == face) {
                s = CraftBookBukkitUtil.toChangedSign(testBlock);
            }
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
                if (ItemUtil.areItemsIdentical(player.getInventory().getItemInMainHand(), itemID)
                        || ItemUtil.areItemsIdentical(player.getInventory().getItemInOffHand(), itemID)) {
                    toggleSwitches(testBlock, eventFace.getOppositeFace());
                    success = true;
                } else
                    lplayer.printError("mech.hiddenswitch.key");
            }

            if(success)
                lplayer.print("mech.hiddenswitch.toggle");

            return !lplayer.isSneaking();
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;

        if (!(event.getBlockFace() == BlockFace.EAST || event.getBlockFace() == BlockFace.WEST
                || event.getBlockFace() == BlockFace.NORTH || event.getBlockFace() == BlockFace.SOUTH
                || event.getBlockFace() == BlockFace.UP || event.getBlockFace() == BlockFace.DOWN))
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!player.hasPermission("craftbook.mech.hiddenswitch.use"))
            return;

        if (!isValidWallSign(event.getClickedBlock().getRelative(1, 0, 0))
                && !isValidWallSign(event.getClickedBlock().getRelative(-1, 0, 0))
                && !isValidWallSign(event.getClickedBlock().getRelative(0, 0, 1))
                && !isValidWallSign(event.getClickedBlock().getRelative(0, 0, -1)))
            return;

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction()))
            return;

        if(testBlock(event.getClickedBlock(), event.getBlockFace(), event.getPlayer()))
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
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "any-side", "Allows the Hidden Switch to be activated from any side of the block.");
        anyside = config.getBoolean(path + "any-side", true);
    }
}