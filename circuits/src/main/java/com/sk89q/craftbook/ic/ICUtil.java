// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.ic;

import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.material.Lever;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.RedstoneTorch;

import java.util.HashMap;

/**
 * IC utility functions.
 *
 * @author sk89q
 */
public class ICUtil {

    private static BlockFace[] REDSTONE_CONTACT_FACES =
            {BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP};

    public ICUtil() {

    }

    private static HashMap<Location, Boolean> torchStatus = new HashMap<Location, Boolean>();

    public class ICListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockPhysics(BlockPhysicsEvent event) {

            if (event.getBlock().getType() == Material.REDSTONE_TORCH_ON || event.getBlock().getType() == Material
                    .REDSTONE_TORCH_OFF)
                if (ICUtil.getTorchStatus(event.getBlock().getLocation()) != null) {
                    byte data = event.getBlock().getData();
                    if (ICUtil.getTorchStatus(event.getBlock().getLocation())) {
                        if (event.getBlock().getTypeId() != Material.REDSTONE_TORCH_OFF.getId()) {
                            event.getBlock().setTypeId(Material.REDSTONE_TORCH_OFF.getId());
                        }
                    } else if (event.getBlock().getTypeId() != Material.REDSTONE_TORCH_ON.getId()) {
                        event.getBlock().setTypeId(Material.REDSTONE_TORCH_ON.getId());
                    }
                    event.getBlock().setData(data, false);
                }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockBreak(BlockBreakEvent event) {

            if (event.getBlock().getType() == Material.REDSTONE_TORCH_ON || event.getBlock().getType() == Material
                    .REDSTONE_TORCH_OFF)
                if (ICUtil.getTorchStatus(event.getBlock().getLocation()) != null) {
                    ICUtil.removeTorch(event.getBlock().getLocation());
                }
        }
    }

    public static Boolean getTorchStatus(Location loc) {

        return torchStatus.get(loc);
    }

    public static void removeTorch(Location loc) {

        torchStatus.remove(loc);
    }

    public static void setTorch(Location loc, Boolean value) {

        torchStatus.put(loc, value);
    }

    /**
     * Set an IC's output state at a block.
     *
     * @param block
     * @param state
     *
     * @return whether something was changed
     */
    public static boolean setState(Block block, boolean state) {

        if (block.getType() != Material.LEVER && block.getType() != Material.REDSTONE_TORCH_OFF &&
                block.getType() != Material.REDSTONE_TORCH_ON)
            return false;

        boolean wasOn = (block.getData() & 0x8) > 0;
        // # START legacy code for fallback if CraftBukkit does not work
        byte data = block.getData();
        int newData;
        // first update the lever
        Lever lever = null;
        RedstoneTorch torch = null;
        if (block.getState().getData() instanceof Lever) {
            lever = (Lever) block.getState().getData();
        } else {
            torch = (RedstoneTorch) block.getState().getData();
        }

        if (!state) {
            newData = data & 0x7;
        } else {
            newData = data | 0x8;
        }
        // #END legacy code
        if (block.getType() == Material.REDSTONE_TORCH_OFF || block.getType() == Material.REDSTONE_TORCH_ON) {
            byte oldData = block.getData();
            setTorch(block.getLocation(), state);
            if (state) {
                if (block.getTypeId() != Material.REDSTONE_TORCH_OFF.getId()) {
                    block.setTypeId(Material.REDSTONE_TORCH_OFF.getId());
                }
            } else if (block.getTypeId() != Material.REDSTONE_TORCH_ON.getId()) {
                block.setTypeId(Material.REDSTONE_TORCH_ON.getId());
            }
            block.setData(oldData, false);
        }
        if (wasOn != state) {
            try {
                if (block.getType() == Material.LEVER) {
                    net.minecraft.server.Block nmsBlock = net.minecraft.server.Block.byId[Material.LEVER.getId()];
                    net.minecraft.server.World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();

                    // Note: The player argument isn't actually used by the method in BlockLever so we can pass null.
                    // This method takes care of all the necessary block updates and redstone events.
                    // I dont know what the params at the back mean, but the method works perfectly without them.
                    nmsBlock.interact(nmsWorld, block.getX(), block.getY(), block.getZ(), null, 0, 0, 0, 0);
                    return true;
                }
            } catch (Throwable e) {
                Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
                // lets catch the exception if the method is not supported
                block.setData((byte) newData, true);
                // get the block the lever is attached to:
                Block source;
                if (block.getState().getData() instanceof Lever) {
                    source = block.getRelative(lever.getAttachedFace());
                } else {
                    source = block.getRelative(torch.getAttachedFace());
                }
                // then iterate over all blocks around the block the lever is attached to
                for (BlockFace face : REDSTONE_CONTACT_FACES) {
                    Block relative = source.getRelative(face);
                    Material type = relative.getType();

                    data = relative.getData();
                    if (state) {
                        newData = data | 0x8;
                    } else {
                        newData = data & ~0x8;
                    }

                    if (type == Material.REDSTONE_WIRE || type == Material.POWERED_RAIL) {
                        relative.setData((byte) newData, true);
                    } else if (type == Material.REDSTONE_LAMP_OFF || type == Material.REDSTONE_LAMP_ON) {
                        if (state) {
                            relative.setType(Material.REDSTONE_LAMP_ON);
                        } else {
                            relative.setType(Material.REDSTONE_LAMP_OFF);
                        }
                    } else if (type == Material.REDSTONE_TORCH_ON || type == Material.REDSTONE_TORCH_OFF) {
                        if (state) {
                            byte oldData = relative.getData();
                            relative.setTypeId(Material.REDSTONE_TORCH_OFF.getId(), false);
                            relative.setData(oldData, false);
                        } else {
                            byte oldData = relative.getData();
                            relative.setTypeId(Material.REDSTONE_TORCH_ON.getId(), false);
                            relative.setData(oldData, false);
                        }
                    } else if (type == Material.DIODE_BLOCK_ON || type == Material.DIODE_BLOCK_OFF) {
                        if (state) {
                            byte oldData = relative.getData();
                            relative.setType(Material.DIODE_BLOCK_OFF);
                            relative.setData(oldData);
                        } else {
                            byte oldData = relative.getData();
                            relative.setType(Material.DIODE_BLOCK_ON);
                            relative.setData(oldData);
                        }
                    } else if (type == Material.PISTON_BASE || type == Material.PISTON_STICKY_BASE) {
                        ((PistonBaseMaterial) relative.getState().getData()).setPowered(state);
                        relative.getState().update();
                    } else if (type == Material.LEVER) {
                        relative.setData((byte) newData, true);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static Block parseBlockLocation(Sign sign, int lPos, boolean relative) {

        Block target = SignUtil.getBackBlock(sign.getBlock());
        String line = sign.getLine(lPos);
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;
        if (line.contains("=")) {
            String[] split = line.split("=");
            line = split[1];
        }
        try {
            String[] split = line.split(":");
            if (split.length > 1) {
                offsetX = Integer.parseInt(split[0]);
                offsetY = Integer.parseInt(split[1]);
                offsetZ = Integer.parseInt(split[2]);
            } else {
                offsetY = Integer.parseInt(line);
            }
        } catch (NumberFormatException e) {
            // do nothing and use defaults
        } catch (ArrayIndexOutOfBoundsException e) {
            // do nothing and use defaults
        }
        if (relative) {
            target = LocationUtil.getRelativeOffset(sign, offsetX, offsetY, offsetZ);
        } else {
            target = LocationUtil.getOffset(target, offsetX, offsetY, offsetZ);
        }
        return target;
    }

    public static Block parseBlockLocation(Sign sign, int lPos) {

        return parseBlockLocation(sign, lPos, true);
    }

    public static Block parseBlockLocation(Sign sign) {

        return parseBlockLocation(sign, 2, true);
    }

    public static void verifySignSyntax(Sign sign) throws ICVerificationException {

        verifySignSyntax(sign, 2);
    }

    public static void verifySignSyntax(Sign sign, int i) throws ICVerificationException {

        try {
            String line = sign.getLine(i);
            String[] strings = line.split(":");
            if (line.contains("=")) {
                String[] split = line.split("=");
                Integer.parseInt(split[0]);
                strings = split[1].split(":");
            }
            if (strings.length > 1) {
                Integer.parseInt(strings[1]);
                Integer.parseInt(strings[2]);
            }
            Integer.parseInt(strings[0]);
        } catch (Exception e) {
            throw new ICVerificationException("Wrong syntax! Needs to be: radius=x:y:z or radius=y or y");
        }
    }

    public static int parseRadius(Sign sign) {

        return parseRadius(sign, 2);
    }

    public static int parseRadius(Sign sign, int lPos) {

        String line = sign.getLine(lPos);
        int radius = 10; //default radius is 10.
        try {
            return Integer.parseInt(line.split("=")[0]);
        } catch (NumberFormatException e) {
            // do nothing and use default radius
        }
        return radius;
    }
}
