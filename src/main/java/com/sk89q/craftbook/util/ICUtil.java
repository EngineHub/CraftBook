// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Lever;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.SphereRegionSelector;

/**
 * IC utility functions.
 *
 * @author sk89q
 */
public class ICUtil {

    /**
     * Set an IC's output state at a block.
     *
     * @param block
     * @param state
     *
     * @return whether something was changed
     */
    public static boolean setState(Block block, boolean state, Block source) {

        if (block.getTypeId() != BlockID.LEVER) return false;

        // return if the lever is not attached to our IC block
        Lever lever = (Lever) block.getState().getData();

        if (!block.getRelative(lever.getAttachedFace()).equals(source))
            return false;

        // check if the lever was toggled on
        boolean wasOn = (block.getData() & 0x8) > 0;

        byte data = block.getData();
        int newData;
        // check if the state changed and set the data value
        if (!state) {
            newData = data & 0x7;
        } else {
            newData = data | 0x8;
        }

        // if the state changed lets apply physics to the source block and the lever itself
        if (wasOn != state) {

            // set the new data
            block.setData((byte) newData, true);
            // apply physics to the source block the lever is attached to
            byte sData = source.getData();
            source.setData((byte) (sData - 1), true);
            source.setData(sData, true);

            // lets call blockredstone events on the source block and the lever
            // in order to correctly update all surrounding blocks
            BlockRedstoneEvent leverEvent = new BlockRedstoneEvent(block, wasOn ? 15 : 0, state ? 15 : 0);
            BlockRedstoneEvent sourceEvent = new BlockRedstoneEvent(source, wasOn ? 15 : 0, state ? 15 : 0);
            CraftBookPlugin.inst().getServer().getPluginManager().callEvent(leverEvent);
            CraftBookPlugin.inst().getServer().getPluginManager().callEvent(sourceEvent);
            return true;
        }

        return false;
    }

    public static void parseSignFlags(LocalPlayer player, ChangedSign sign) {

        for(int i = 2; i < 4; i++) {

            if(sign.getLine(i).contains("[off]")) {

                if(CraftBookPlugin.inst().getWorldEdit() == null) {
                    sign.setLine(i, sign.getLine(i).replace("[off]", ""));
                    player.printError("worldedit.ic.notfound");
                } else {
                    if(CraftBookPlugin.inst().getWorldEdit().getSelection(((BukkitPlayer) player).getPlayer()) != null && CraftBookPlugin.inst().getWorldEdit().getSelection(((BukkitPlayer) player).getPlayer()).getRegionSelector() != null) {

                        RegionSelector selector = CraftBookPlugin.inst().getWorldEdit().getSelection(((BukkitPlayer) player).getPlayer()).getRegionSelector();

                        try {
                            if(selector instanceof CuboidRegionSelector) {

                                Vector centre = selector.getRegion().getMaximumPoint().add(selector.getRegion().getMinimumPoint());

                                centre = centre.divide(2);

                                Vector offset = centre.subtract(sign.getBlockVector());

                                String x,y,z;

                                x = Double.toString(offset.getX());
                                if (x.endsWith(".0"))
                                    x = x.replace(".0", "");

                                y = Double.toString(offset.getY());
                                if (y.endsWith(".0"))
                                    y = y.replace(".0", "");

                                z = Double.toString(offset.getZ());
                                if (z.endsWith(".0"))
                                    z = z.replace(".0", "");

                                sign.setLine(i, sign.getLine(i).replace("[off]", "&" + x + ":" + y + ":" + z));
                            } else if (selector instanceof SphereRegionSelector) {

                                Vector centre = ((SphereRegionSelector)selector).getRegion().getCenter();

                                Vector offset = centre.subtract(sign.getBlockVector());

                                String x,y,z;

                                x = Double.toString(offset.getX());
                                if (x.endsWith(".0"))
                                    x = x.replace(".0", "");

                                y = Double.toString(offset.getY());
                                if (y.endsWith(".0"))
                                    y = y.replace(".0", "");

                                z = Double.toString(offset.getZ());
                                if (z.endsWith(".0"))
                                    z = z.replace(".0", "");

                                sign.setLine(i, sign.getLine(i).replace("[off]", "&" + x + ":" + y + ":" + z));
                            } else { // Unsupported.
                                sign.setLine(i, sign.getLine(i).replace("[off]", ""));
                                player.printError("worldedit.ic.unsupported");
                            }
                        }
                        catch(IncompleteRegionException e) {
                            player.printError("worldedit.ic.noselection");
                        }
                    } else {
                        player.printError("worldedit.ic.noselection");
                    }
                }
            }

            if(sign.getLine(i).contains("[rad]")) {

                if(CraftBookPlugin.inst().getWorldEdit() == null) {
                    sign.setLine(i, sign.getLine(i).replace("[rad]", ""));
                    player.printError("worldedit.ic.notfound");
                } else {

                    if(CraftBookPlugin.inst().getWorldEdit().getSelection(((BukkitPlayer) player).getPlayer()) != null && CraftBookPlugin.inst().getWorldEdit().getSelection(((BukkitPlayer) player).getPlayer()).getRegionSelector() != null) {

                        RegionSelector selector = CraftBookPlugin.inst().getWorldEdit().getSelection(((BukkitPlayer) player).getPlayer()).getRegionSelector();

                        try {
                            if(selector instanceof CuboidRegionSelector) {

                                String x,y,z;

                                x = Double.toString(Math.abs(selector.getRegion().getMaximumPoint().getX() - selector.getRegion().getMinimumPoint().getX())/2);
                                if (x.endsWith(".0"))
                                    x = x.replace(".0", "");

                                y = Double.toString(Math.abs(selector.getRegion().getMaximumPoint().getY() - selector.getRegion().getMinimumPoint().getY())/2);
                                if (y.endsWith(".0"))
                                    y = y.replace(".0", "");

                                z = Double.toString(Math.abs(selector.getRegion().getMaximumPoint().getZ() - selector.getRegion().getMinimumPoint().getZ())/2);
                                if (z.endsWith(".0"))
                                    z = z.replace(".0", "");

                                sign.setLine(i, sign.getLine(i).replace("[rad]", x + "," + y + "," + z));
                            } else if (selector instanceof SphereRegionSelector) {

                                String x;

                                double amounts = ((EllipsoidRegion) selector.getRegion()).getRadius().getX();

                                x = Double.toString(amounts);
                                if (x.endsWith(".0"))
                                    x = x.replace(".0", "");

                                sign.setLine(i, sign.getLine(i).replace("[rad]", x));
                            } else { // Unsupported.
                                sign.setLine(i, sign.getLine(i).replace("[rad]", ""));
                                player.printError("worldedit.ic.unsupported");
                            }
                        }
                        catch(IncompleteRegionException e) {
                            player.printError("worldedit.ic.noselection");
                        }
                    } else {
                        player.printError("worldedit.ic.noselection");
                    }
                }
            }
        }

        sign.update(false);
    }

    public static Block parseBlockLocation(ChangedSign sign, int lPos, LocationCheckType relative) {

        Block target = SignUtil.getBackBlock(BukkitUtil.toSign(sign).getBlock());
        String line = sign.getLine(lPos);
        if (line.contains("!")) {
            relative = LocationCheckType.getTypeFromChar('!');
            line = line.replace("!", "");
        } else if (line.contains("^")) {
            relative = LocationCheckType.getTypeFromChar('^');
            line = line.replace("^", "");
        } else if (line.contains("&")) {
            relative = LocationCheckType.getTypeFromChar('&');
            line = line.replace("&", "");
        }
        line = line.replace("!", "").replace("^", "").replace("&", ""); //in case it had multiples.
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;
        if (line.contains("=")) {
            String[] split = RegexUtil.EQUALS_PATTERN.split(line);
            line = split[1];
        }
        try {
            String[] split = RegexUtil.COLON_PATTERN.split(line);
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

        if(offsetX == 0 && offsetY == 0 && offsetZ == 0)
            return target;

        if (relative == LocationCheckType.RELATIVE) {
            target = LocationUtil.getRelativeOffset(sign, offsetX, offsetY, offsetZ);
        } else if (relative == LocationCheckType.OFFSET){
            target = LocationUtil.getOffset(target, offsetX, offsetY, offsetZ);
        } else {
            target = new Location(target.getWorld(), offsetX, offsetY, offsetZ).getBlock();
        }
        return target;
    }

    public static Block parseBlockLocation(ChangedSign sign, int lPos) {

        return parseBlockLocation(sign, lPos, CraftBookPlugin.inst().getConfiguration().ICdefaultCoordinate);
    }

    public static Block parseBlockLocation(ChangedSign sign) {

        return parseBlockLocation(sign, 2, CraftBookPlugin.inst().getConfiguration().ICdefaultCoordinate);
    }

    public static void verifySignSyntax(ChangedSign sign) throws ICVerificationException {

        verifySignLocationSyntax(sign, 2);
    }

    public static void verifySignLocationSyntax(ChangedSign sign, int i) throws ICVerificationException {

        try {
            String line = sign.getLine(i);
            String[] strings;
            line = line.replace("!", "").replace("^", "").replace("&", "");
            if (line.contains("=")) {
                String[] split = RegexUtil.EQUALS_PATTERN.split(line, 2);
                if(RegexUtil.COMMA_PATTERN.split(split[0]).length > 1) {

                    String[] rads = RegexUtil.COMMA_PATTERN.split(split[0]);
                    Integer.parseInt(rads[0]);
                    Integer.parseInt(rads[1]);
                    Integer.parseInt(rads[2]);
                }
                else
                    Integer.parseInt(split[0]);
                strings = RegexUtil.COLON_PATTERN.split(split[1], 3);
            } else {
                strings = RegexUtil.COLON_PATTERN.split(line);
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

    public static Vector parseRadius(ChangedSign sign) {

        return parseRadius(sign, 2);
    }

    public static Vector parseRadius(ChangedSign sign, int lPos) {

        return parseRadius(sign.getLine(lPos));
    }

    public static Vector parseRadius(String line) {

        Vector radius = new Vector(10,10,10); // default radius is 10.
        try {
            String[] radians = RegexUtil.COMMA_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(line, 2)[0]);
            if(radians.length > 1) {
                int x = VerifyUtil.verifyRadius(Integer.parseInt(radians[0]), CraftBookPlugin.inst().getConfiguration().ICMaxRange);
                int y = VerifyUtil.verifyRadius(Integer.parseInt(radians[1]), CraftBookPlugin.inst().getConfiguration().ICMaxRange);
                int z = VerifyUtil.verifyRadius(Integer.parseInt(radians[2]), CraftBookPlugin.inst().getConfiguration().ICMaxRange);
                return new Vector(x,y,z);
            }
            else {
                int r = VerifyUtil.verifyRadius(Integer.parseInt(radians[0]), CraftBookPlugin.inst().getConfiguration().ICMaxRange);
                return new Vector(r,r,r);
            }
        } catch (NumberFormatException e) {
            // do nothing and use default radius
        }
        return radius;
    }

    public enum LocationCheckType {

        RELATIVE('^'),
        OFFSET('&'),
        ABSOLUTE('!');

        char c;

        LocationCheckType(char c) {

            this.c = c;
        }

        public static LocationCheckType getTypeFromChar(char c) {

            for(LocationCheckType t : values())
                if(t.c == c)
                    return t;

            return RELATIVE;
        }

        public static LocationCheckType getTypeFromName(String name) {

            if(name.length() == 1)
                return getTypeFromChar(name.charAt(0));
            for(LocationCheckType t : values())
                if(t.name().equalsIgnoreCase(name))
                    return t;

            return RELATIVE;
        }
    }
}