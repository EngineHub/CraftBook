package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class HiddenSwitch extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<HiddenSwitch> {

        public Factory(MechanismsPlugin plugin) {

            this.plugin = plugin;
        }

        final MechanismsPlugin plugin;

        @Override
        public HiddenSwitch detect(BlockWorldVector pos, LocalPlayer player, Sign sign)
                throws InvalidMechanismException {
            // int myBlock = BukkitUtil.toWorld(pos).getBlockTypeIdAt(BukkitUtil.toLocation(pos));
            //FIXME In the future add a check here to test if you can actually build wall signs on this block.
            //World wrd = BukkitUtil.toWorld(pos);
            if (sign.getLine(1).equalsIgnoreCase("[X]")) {

                player.checkPermission("craftbook.mech.hiddenswitch");
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

        @Override
        public HiddenSwitch detect(BlockWorldVector pos) throws InvalidMechanismException {

            World wrd = BukkitUtil.toWorld(pos);
            if (isValidWallSign(wrd, pos.add(1, 0, 0))
                    || isValidWallSign(wrd, pos.add(-1, 0, 0))
                    || isValidWallSign(wrd, pos.add(0, 0, 1))
                    || isValidWallSign(wrd, pos.add(0, 0, -1)))
                return new HiddenSwitch(BukkitUtil.toBlock(pos), plugin);
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
    public void onRightClick(PlayerInteractEvent event) {

        LocalPlayer player = plugin.wrap(event.getPlayer());

        try {
            player.checkPermission("craftbook.mech.hiddenswitch.use");
        } catch (InsufficientPermissionsException e) {
            return;
        }
        if (!(event.getBlockFace() == BlockFace.EAST
                || event.getBlockFace() == BlockFace.WEST
                || event.getBlockFace() == BlockFace.NORTH
                || event.getBlockFace() == BlockFace.SOUTH
                || event.getBlockFace() == BlockFace.UP
                || event.getBlockFace() == BlockFace.DOWN)) return;
        BlockFace face = event.getBlockFace().getOppositeFace();
        Block testBlock = switchBlock.getRelative(face);
        boolean passed = false;

        while (true) {
            if (testBlock.getType() == Material.WALL_SIGN) {
                Sign s = (Sign) testBlock.getState();
                if (s.getLine(1).equalsIgnoreCase("[X]")) {

                    int itemID = -1;

                    if (!s.getLine(0).trim().equalsIgnoreCase("")) {
                        try {
                            itemID = Integer.parseInt(s.getLine(0).trim());
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    if (!s.getLine(2).trim().equalsIgnoreCase(""))
                        if (!plugin.isInGroup(event.getPlayer().getName(), s.getLine(2).trim())) {
                            player.printError("mech.group");
                            return;
                        }

                    if (itemID == -1) {
                        toggleSwitches(testBlock, event.getBlockFace().getOppositeFace());
                    } else if (event.getPlayer().getItemInHand() != null || itemID == 0) {
                        if (itemID == 0 && event.getPlayer().getItemInHand() == null
                                || event.getPlayer().getItemInHand().getTypeId() == itemID) {
                            toggleSwitches(testBlock, event.getBlockFace().getOppositeFace());
                        } else {
                            player.printError("mech.hiddenswitch.key");
                        }
                    } else {
                        player.printError("mech.hiddenswitch.key");
                    }

                    break;
                }
            } else if (plugin.getLocalConfiguration().hiddenSwitchSettings.anyside) {
                if (face == event.getBlockFace().getOppositeFace() && passed) {
                    break;
                }
                passed = true;

                if (face == BlockFace.WEST) {
                    face = BlockFace.NORTH;
                }
                else if (face == BlockFace.NORTH) {
                    face = BlockFace.EAST;
                }
                else if (face == BlockFace.EAST) {
                    face = BlockFace.SOUTH;
                }
                else if (face == BlockFace.SOUTH) {
                    face = BlockFace.UP;
                }
                else if (face == BlockFace.UP) {
                    face = BlockFace.DOWN;
                }
                else if (face == BlockFace.DOWN) {
                    face = BlockFace.WEST;
                }

                testBlock = switchBlock.getRelative(face);
            }
            else {
                break;
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
            final Block checkBlock = sign.getRelative(blockFace);

            if (checkBlock.getTypeId() == BlockID.LEVER) {
                checkBlock.setData((byte) (checkBlock.getData() ^ 0x8));
            } else if (checkBlock.getTypeId() == BlockID.STONE_BUTTON) {
                checkBlock.setData((byte) (checkBlock.getData() | 0x8));
                Runnable turnOff = new Runnable() {

                    @Override
                    public void run() {

                        checkBlock.setData((byte) (checkBlock.getData() & ~0x8));
                    }
                };
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, turnOff, 1 * 20L);
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