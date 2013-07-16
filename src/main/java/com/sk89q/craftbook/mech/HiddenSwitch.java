package com.sk89q.craftbook.mech;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;

public class HiddenSwitch extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<HiddenSwitch> {

        @Override
        public HiddenSwitch detect(BlockWorldVector pos, LocalPlayer player, ChangedSign sign) throws InvalidMechanismException {
            // int myBlock = BukkitUtil.toWorld(pos).getBlockTypeIdAt(BukkitUtil.toLocation(pos));
            // FIXME In the future add a check here to test if you can actually build wall signs on this block.
            // World wrd = BukkitUtil.toWorld(pos);
            if (sign.getLine(1).equalsIgnoreCase("[X]")) {

                player.checkPermission("craftbook.mech.hiddenswitch");
                return new HiddenSwitch(BukkitUtil.toBlock(pos));
            }
            return null;
        }

        private boolean isValidWallSign(World world, Vector v) {

            Block b = world.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ());

            // Must be Wall Sign
            if (b == null || b.getTypeId() != BlockID.WALL_SIGN) return false;
            if (b.getState() == null || !(b.getState() instanceof Sign)) return false;
            Sign s = (Sign) b.getState();

            return s.getLine(1).equalsIgnoreCase("[X]");
        }

        @Override
        public HiddenSwitch detect(BlockWorldVector pos) throws InvalidMechanismException {

            World wrd = BukkitUtil.toWorld(pos);
            if (isValidWallSign(wrd, pos.add(1, 0, 0)) || isValidWallSign(wrd, pos.add(-1, 0,
                    0)) || isValidWallSign(wrd, pos.add(0, 0, 1))
                    || isValidWallSign(wrd, pos.add(0, 0, -1)))
                return new HiddenSwitch(BukkitUtil.toBlock(pos));
            return null;
        }
    }

    final Block switchBlock;

    public HiddenSwitch(Block block) {

        switchBlock = block;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!CraftBookPlugin.inst().getConfiguration().hiddenSwitchEnabled) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!player.hasPermission("craftbook.mech.hiddenswitch.use"))
            return;

        if (!(event.getBlockFace() == BlockFace.EAST || event.getBlockFace() == BlockFace.WEST
                || event.getBlockFace() == BlockFace.NORTH || event.getBlockFace() == BlockFace.SOUTH
                || event.getBlockFace() == BlockFace.UP || event.getBlockFace() == BlockFace.DOWN))
            return;


        ChangedSign s = null;
        Block testBlock = null;
        if(CraftBookPlugin.inst().getConfiguration().hiddenSwitchAnyside) {

            for(BlockFace face : LocationUtil.getDirectFaces()) {
                testBlock = switchBlock.getRelative(face);
                if(testBlock.getTypeId() == BlockID.WALL_SIGN) {
                    s = BukkitUtil.toChangedSign(testBlock);
                    break;
                }
            }
        } else {
            BlockFace face = event.getBlockFace().getOppositeFace();
            testBlock = switchBlock.getRelative(face);
            if(testBlock.getTypeId() == BlockID.WALL_SIGN)
                s = BukkitUtil.toChangedSign(testBlock);
        }

        if(s == null)
            return;

        if (s.getLine(1).equalsIgnoreCase("[X]")) {

            int itemID = -1;

            if (!s.getLine(0).trim().isEmpty()) {
                try {
                    itemID = Integer.parseInt(s.getLine(0).trim());
                } catch (NumberFormatException ignored) {
                }
            }

            if (!s.getLine(2).trim().isEmpty())
                if (!CraftBookPlugin.inst().inGroup(event.getPlayer(), s.getLine(2).trim())) {
                    player.printError("mech.group");
                    return;
                }

            if (itemID == -1) {
                toggleSwitches(testBlock, event.getBlockFace().getOppositeFace());
            } else if (itemID >= 0) {
                if (player.getHeldItemType() == itemID)
                    toggleSwitches(testBlock, event.getBlockFace().getOppositeFace());
                else
                    player.printError("mech.hiddenswitch.key");
            } else
                player.printError("mech.hiddenswitch.key");

            player.print("mech.hiddenswitch.toggle");

            if (!event.getPlayer().isSneaking()) event.setCancelled(true);
        }


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

            if (checkBlock.getTypeId() == BlockID.LEVER) {
                checkBlock.setData((byte) (checkBlock.getData() ^ 0x8));
            } else if (checkBlock.getTypeId() == BlockID.STONE_BUTTON || checkBlock.getTypeId() == BlockID
                    .WOODEN_BUTTON) {
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