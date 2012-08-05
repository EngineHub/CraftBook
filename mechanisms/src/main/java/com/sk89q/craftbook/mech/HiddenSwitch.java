package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class HiddenSwitch extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<HiddenSwitch> {

        public Factory(MechanismsPlugin plugin) {

            this.plugin = plugin;
        }

        final MechanismsPlugin plugin;

        @Override
        public HiddenSwitch detect(BlockWorldVector pos, LocalPlayer Player, Sign sign)
                throws InvalidMechanismException {
            // int myBlock = BukkitUtil.toWorld(pos).getBlockTypeIdAt(BukkitUtil.toLocation(pos));
            //FIXME In the future add a check here to test if you can actually build wall signs on this block.
            //World wrd = BukkitUtil.toWorld(pos);
            if (sign.getLine(1).equalsIgnoreCase("[X]")) {

                return new HiddenSwitch(BukkitUtil.toBlock(pos), plugin);
            }
            return null;
        }

        private boolean isValidWallSign(World world, Vector v) {

            Block b = world.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ());

            // Must be Wall Sign
            if (b == null || b.getTypeId() != BlockID.WALL_SIGN) return false;
            if (!(b.getState() instanceof Sign)) return false;
            Sign s = (Sign) b.getState();

            return s.getLine(1).equalsIgnoreCase("[X]");
        }

        //private boolean isValidWallSign(Block b) {
        //
        //    return isValidWallSign(b.getWorld(), new BlockVector(b.getX(), b.getY(), b.getZ()));
        // }

        @Override
        public HiddenSwitch detect(BlockWorldVector pos) throws InvalidMechanismException {

            World wrd = BukkitUtil.toWorld(pos);
            if (isValidWallSign(wrd, pos.add(1, 0, 0))
                    || isValidWallSign(wrd, pos.add(-1, 0, 0))
                    || isValidWallSign(wrd, pos.add(0, 0, 1))
                    || isValidWallSign(wrd, pos.add(0, 0, -1))) {
                return new HiddenSwitch(BukkitUtil.toBlock(pos), plugin);
            }
            return null;
        }
    }

    final Block switchBlock;
    final MechanismsPlugin plugin;

    public HiddenSwitch(Block block, MechanismsPlugin plugin) {

        switchBlock = block;
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(org.bukkit.event.player.PlayerInteractEvent event) {

        if (!(event.getBlockFace() == BlockFace.EAST
                || event.getBlockFace() == BlockFace.WEST
                || event.getBlockFace() == BlockFace.NORTH
                || event.getBlockFace() == BlockFace.SOUTH)) {
            return;
        }
        Block testBlock = switchBlock.getRelative(event.getBlockFace().getOppositeFace());

        if (testBlock.getType() == Material.WALL_SIGN) {
            Sign s = (Sign) testBlock.getState();
            if (s.getLine(1).equalsIgnoreCase("[X]")) {
                toggleSwitches(testBlock, event.getBlockFace().getOppositeFace());
            }
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
            Block checkBlock = sign.getRelative(blockFace);

            if (checkBlock.getTypeId() == BlockID.LEVER) {
                checkBlock.setData((byte) (checkBlock.getData() ^ 0x8));
            }
        }
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isActive() {

        return false;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {

    }
}