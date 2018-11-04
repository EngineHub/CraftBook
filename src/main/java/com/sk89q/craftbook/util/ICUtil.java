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

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.ICMechanic;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.pipe.PipeRequestEvent;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.SphereRegionSelector;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * IC utility functions.
 *
 * @author sk89q
 */
public final class ICUtil {

    /**
     * Set an IC's output state at a block.
     *
     * @param block
     * @param state
     *
     * @return whether something was changed
     */
    public static boolean setState(Block block, boolean state, Block source) {

        if (block.getType() != Material.LEVER) return false;

        // return if the lever is not attached to our IC block
        Switch lever = (Switch) block.getBlockData();

        if (!block.getRelative(lever.getFacing().getOppositeFace()).equals(source))
            return false;

        // check if the lever was toggled on
        boolean wasOn = lever.isPowered();

        // if the state changed lets apply physics to the source block and the lever itself
        if (wasOn != state) {
            // set the new data
            lever.setPowered(state);
            block.setBlockData(lever);
            // apply physics to the source block the lever is attached to
            source.setBlockData(source.getBlockData(), true);

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

    public static void parseSignFlags(CraftBookPlayer player, ChangedSign sign) {

        for(int i = 2; i < 4; i++) {

            if(sign.getLine(i).contains("[off]")) {

                if(CraftBookPlugin.plugins.getWorldEdit() == null) {
                    sign.setLine(i, StringUtils.replace(sign.getLine(i), "[off]", ""));
                    player.printError("worldedit.ic.notfound");
                } else {
                    RegionSelector selector = WorldEdit.getInstance().getSessionManager().get(player).getRegionSelector(player.getWorld());

                    try {
                        if(selector instanceof CuboidRegionSelector) {

                            BlockVector3 centre = selector.getRegion().getMaximumPoint().add(selector.getRegion().getMinimumPoint());

                            centre = centre.divide(2);

                            BlockVector3 offset = centre.subtract(BukkitAdapter.adapt(sign.getBlock().getLocation()).toVector().toBlockPoint());

                            String x,y,z;

                            x = Double.toString(offset.getX());
                            if (x.endsWith(".0"))
                                x = StringUtils.replace(x, ".0", "");

                            y = Double.toString(offset.getY());
                            if (y.endsWith(".0"))
                                y = StringUtils.replace(y, ".0", "");

                            z = Double.toString(offset.getZ());
                            if (z.endsWith(".0"))
                                z = StringUtils.replace(z, ".0", "");

                            sign.setLine(i, StringUtils.replace(sign.getLine(i), "[off]", "&" + x + ":" + y + ":" + z));
                        } else if (selector instanceof SphereRegionSelector) {
                            Vector3 centre = selector.getRegion().getCenter();
                            Vector3 offset = centre.subtract(BukkitAdapter.adapt(sign.getBlock().getLocation()).toVector());

                            String x,y,z;

                            x = Double.toString(offset.getX());
                            if (x.endsWith(".0"))
                                x = StringUtils.replace(x, ".0", "");

                            y = Double.toString(offset.getY());
                            if (y.endsWith(".0"))
                                y = StringUtils.replace(y, ".0", "");

                            z = Double.toString(offset.getZ());
                            if (z.endsWith(".0"))
                                z = StringUtils.replace(z, ".0", "");

                            sign.setLine(i, StringUtils.replace(sign.getLine(i), "[off]", "&" + x + ":" + y + ":" + z));
                        } else { // Unsupported.
                            sign.setLine(i, StringUtils.replace(sign.getLine(i), "[off]", ""));
                            player.printError("worldedit.ic.unsupported");
                        }
                    }
                    catch(IncompleteRegionException e) {
                        player.printError("worldedit.ic.noselection");
                    }
                }
            }

            if(sign.getLine(i).contains("[rad]")) {

                if(CraftBookPlugin.plugins.getWorldEdit() == null) {
                    sign.setLine(i, StringUtils.replace(sign.getLine(i), "[rad]", ""));
                    player.printError("worldedit.ic.notfound");
                } else {
                    RegionSelector selector = WorldEdit.getInstance().getSessionManager().get(player).getRegionSelector(player.getWorld());

                    try {
                        if(selector instanceof CuboidRegionSelector) {

                            String x,y,z;

                            x = Double.toString(Math.abs(selector.getRegion().getMaximumPoint().getX() - selector.getRegion().getMinimumPoint().getX())/2);
                            if (x.endsWith(".0"))
                                x = StringUtils.replace(x, ".0", "");

                            y = Double.toString(Math.abs(selector.getRegion().getMaximumPoint().getY() - selector.getRegion().getMinimumPoint().getY())/2);
                            if (y.endsWith(".0"))
                                y = StringUtils.replace(y, ".0", "");

                            z = Double.toString(Math.abs(selector.getRegion().getMaximumPoint().getZ() - selector.getRegion().getMinimumPoint().getZ())/2);
                            if (z.endsWith(".0"))
                                z = StringUtils.replace(z, ".0", "");

                            sign.setLine(i, StringUtils.replace(sign.getLine(i), "[rad]", x + "," + y + "," + z));
                        } else if (selector instanceof SphereRegionSelector) {

                            String x;

                            double amounts = ((EllipsoidRegion) selector.getRegion()).getRadius().getX();

                            x = Double.toString(amounts);
                            if (x.endsWith(".0"))
                                x = StringUtils.replace(x, ".0", "");

                            sign.setLine(i, StringUtils.replace(sign.getLine(i), "[rad]", x));
                        } else { // Unsupported.
                            sign.setLine(i, StringUtils.replace(sign.getLine(i), "[rad]", ""));
                            player.printError("worldedit.ic.unsupported");
                        }
                    }
                    catch(IncompleteRegionException e) {
                        player.printError("worldedit.ic.noselection");
                    }
                }
            }
        }

        sign.update(false);
    }

    public static Vector3 parseUnsafeBlockLocation(String line) throws NumberFormatException, ArrayIndexOutOfBoundsException {

        line = StringUtils.replace(StringUtils.replace(StringUtils.replace(line, "!", ""), "^", ""), "&", "");
        double offsetX = 0, offsetY = 0, offsetZ = 0;

        if (line.contains("="))
            line = RegexUtil.EQUALS_PATTERN.split(line)[1];
        String[] split = RegexUtil.COLON_PATTERN.split(line);
        if (split.length > 1) {
            offsetX = Double.parseDouble(split[0]);
            offsetY = Double.parseDouble(split[1]);
            offsetZ = Double.parseDouble(split[2]);
        } else
            offsetY = Double.parseDouble(line);

        return Vector3.at(offsetX, offsetY, offsetZ);
    }

    public static Block parseBlockLocation(ChangedSign sign, String line, LocationCheckType relative) {

        Block target = SignUtil.getBackBlock(CraftBookBukkitUtil.toSign(sign).getBlock());

        if (line.contains("!"))
            relative = LocationCheckType.getTypeFromChar('!');
        else if (line.contains("^"))
            relative = LocationCheckType.getTypeFromChar('^');
        else if (line.contains("&"))
            relative = LocationCheckType.getTypeFromChar('&');

        BlockVector3 offsets = BlockVector3.ZERO;

        try {
            offsets = parseUnsafeBlockLocation(line).toBlockPoint();
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
        }

        if(offsets.getBlockX() == 0 && offsets.getBlockY() == 0 && offsets.getBlockZ() == 0)
            return target;

        if (relative == LocationCheckType.RELATIVE)
            target = LocationUtil.getRelativeOffset(sign, offsets.getBlockX(), offsets.getBlockY(), offsets.getBlockZ());
        else if (relative == LocationCheckType.OFFSET)
            target = LocationUtil.getOffset(target, offsets.getBlockX(), offsets.getBlockY(), offsets.getBlockZ());
        else if (relative == LocationCheckType.ABSOLUTE)
            target = new Location(target.getWorld(), offsets.getBlockX(), offsets.getBlockY(), offsets.getBlockZ()).getBlock();
        return target;
    }

    public static Block parseBlockLocation(ChangedSign sign, int lPos, LocationCheckType relative) {

        return parseBlockLocation(sign, sign.getLine(lPos), relative);
    }

    public static Block parseBlockLocation(ChangedSign sign, int lPos) {

        return parseBlockLocation(sign, lPos, ICMechanic.instance.defaultCoordinates);
    }

    public static Block parseBlockLocation(ChangedSign sign) {

        return parseBlockLocation(sign, 2, ICMechanic.instance.defaultCoordinates);
    }

    public static void verifySignSyntax(ChangedSign sign) throws ICVerificationException {

        verifySignLocationSyntax(sign, 2);
    }

    public static void verifySignLocationSyntax(ChangedSign sign, int i) throws ICVerificationException {

        try {
            String line = sign.getLine(i);
            String[] strings;
            line = StringUtils.replace(StringUtils.replace(StringUtils.replace(line, "!", ""), "^", ""), "&", "");
            if (line.contains("=")) {
                String[] split = RegexUtil.EQUALS_PATTERN.split(line, 2);
                if(RegexUtil.COMMA_PATTERN.split(split[0]).length > 1) {

                    String[] rads = RegexUtil.COMMA_PATTERN.split(split[0]);
                    Double.parseDouble(rads[0]);
                    Double.parseDouble(rads[1]);
                    Double.parseDouble(rads[2]);
                } else
                    Double.parseDouble(split[0]);
                strings = RegexUtil.COLON_PATTERN.split(split[1], 3);
            } else
                strings = RegexUtil.COLON_PATTERN.split(line);
            if (strings.length > 1) {
                Double.parseDouble(strings[1]);
                Double.parseDouble(strings[2]);
            }
            Double.parseDouble(strings[0]);
        } catch (Exception e) {
            throw new ICVerificationException("Wrong syntax! Needs to be: radius=x:y:z or radius=y or y");
        }
    }

    public static Vector3 parseRadius(ChangedSign sign) {
        return parseRadius(sign, 2);
    }

    public static Vector3 parseRadius(ChangedSign sign, int lPos) {
        return parseRadius(sign.getLine(lPos));
    }

    public static Vector3 parseRadius(String line) {

        Vector3 radius = Vector3.at(10,10,10);
        try {
            radius = parseUnsafeRadius(line);
        } catch (NumberFormatException ignored) {
        }
        return radius;
    }

    public static Vector3 parseUnsafeRadius(String line) throws NumberFormatException {
        String[] radians = RegexUtil.COMMA_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(line, 2)[0]);
        if(radians.length > 1) {
            double x = VerifyUtil.verifyRadius(Double.parseDouble(radians[0]), ICMechanic.instance.maxRange);
            double y = VerifyUtil.verifyRadius(Double.parseDouble(radians[1]), ICMechanic.instance.maxRange);
            double z = VerifyUtil.verifyRadius(Double.parseDouble(radians[2]), ICMechanic.instance.maxRange);
            return Vector3.at(x,y,z);
        }
        else {
            double r = Double.parseDouble(radians[0]);
            r = VerifyUtil.verifyRadius(r, ICMechanic.instance.maxRange);
            return Vector3.at(r,r,r);
        }
    }

    public static void collectItem(AbstractIC ic, BlockVector3 offset, ItemStack... items) {
        Sign sign = CraftBookBukkitUtil.toSign(ic.getSign());
        Block backB = ic.getBackBlock();
        BlockFace back = SignUtil.getBack(sign.getBlock());

        Block pipe = backB.getRelative(back);

        // Handle the event
        PipeRequestEvent event = new PipeRequestEvent(pipe, new ArrayList<>(Arrays.asList(items)), backB);
        Bukkit.getPluginManager().callEvent(event);

        Collection<ItemStack> results = event.getItems();

        // If there is a chest add the results to the chest
        Block invHolder = backB.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
        if (InventoryUtil.doesBlockHaveInventory(invHolder)) {
            InventoryHolder c = (InventoryHolder) invHolder.getState();
            results = InventoryUtil.addItemsToInventory(c, results.toArray(new ItemStack[0]));
        }

        // Drop whatever results were not added to the chest
        for (ItemStack item : results) {
            backB.getWorld().dropItemNaturally(sign.getLocation().add(0.5, 0, 0.5), item);
        }
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