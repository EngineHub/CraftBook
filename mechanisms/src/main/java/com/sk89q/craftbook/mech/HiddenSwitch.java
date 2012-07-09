package com.sk89q.craftbook.mech;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class HiddenSwitch extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<HiddenSwitch> {
        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }

        MechanismsPlugin plugin;

        @Override
        public HiddenSwitch detect(BlockWorldVector pos,LocalPlayer Player,Sign sign)
                throws InvalidMechanismException {
            // int myBlock = BukkitUtil.toWorld(pos).getBlockTypeIdAt(BukkitUtil.toLocation(pos));
            //FIXME In the future add a check here to test if you can actually build wall signs on this block.
            //World wrd = BukkitUtil.toWorld(pos);
            if(sign.getLine(1).equalsIgnoreCase("[x]")) {

                return new HiddenSwitch(BukkitUtil.toBlock(pos), plugin);
            }
            return null;
        }

        private boolean isValidWallsign(World world, Vector pos) {
            Block b = world.getBlockAt((int)pos.getX(), (int)pos.getY(), (int)pos.getZ());
            if(b == null) return false;
            if(b.getTypeId() != BlockID.WALL_SIGN) // instead of SIGN_POST
                return false;
            if(!(b.getState() instanceof Sign)) return false;
            Sign s = (Sign)b.getState();

            return (s.getLine(1).equalsIgnoreCase("[x]"));
        }

        @SuppressWarnings("unused")
        private boolean isValidWallsign(Block b) {
            if(b.getType() != Material.WALL_SIGN) // instead of SIGN_POST
                return false;
            Sign s = (Sign)b.getState();
            //System.out.println(s.getLine(1));
            return (s.getLine(1).equalsIgnoreCase("[x]"));
        }

        @Override
        public HiddenSwitch detect(BlockWorldVector pos) throws InvalidMechanismException {
            //System.out.println("detect?");
            // int myBlock = BukkitUtil.toWorld(pos).getBlockTypeIdAt(BukkitUti$
            //FIXME In the future add a check here to test if you can actually $
            World wrd = BukkitUtil.toWorld(pos);
            if(isValidWallsign(wrd, pos.add(1,0,0))
                    || isValidWallsign(wrd, pos.add(-1,0,0))
                    || isValidWallsign(wrd, pos.add(0,0,1))
                    || isValidWallsign(wrd, pos.add(0,0,-1))) {
                //System.out.println("hi");
                return new HiddenSwitch(BukkitUtil.toBlock(pos), plugin);
            }
            return null;
        }
    }

    Block switchBlock;
    MechanismsPlugin plugin;

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
            if (s.getLine(1).equalsIgnoreCase("[x]")) {
                toggleSwitches(testBlock, event.getBlockFace().getOppositeFace());
            }
        }
    }

    private void toggleSwitches(Block sign, BlockFace direction) {
        Block up = sign.getRelative(BlockFace.UP);
        Block down = sign.getRelative(BlockFace.DOWN);
        Block adj1, adj2;

        // Get perpendicular plane
        if (direction == BlockFace.EAST || direction == BlockFace.WEST) {
            adj1 = sign.getRelative(BlockFace.NORTH);
            adj2 = sign.getRelative(BlockFace.SOUTH);
        } else {
            adj1 = sign.getRelative(BlockFace.EAST);
            adj2 = sign.getRelative(BlockFace.WEST);
        }
        if (up != null && up.getType() == Material.LEVER) {
            up.setData((byte)(up.getData() ^ 0x8));
        }
        if (down != null && down.getType() == Material.LEVER) {
            down.setData((byte)(down.getData() ^ 0x8));
        }
        if (adj1 != null && adj1.getType() == Material.LEVER) {
            adj1.setData((byte)(adj1.getData() ^ 0x8));
        }
        if (adj2 != null && adj2.getType() == Material.LEVER) {
            adj2.setData((byte)(adj2.getData() ^ 0x8));
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

}
