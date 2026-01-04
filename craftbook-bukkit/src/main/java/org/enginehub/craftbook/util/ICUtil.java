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

package org.enginehub.craftbook.util;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.SphereRegionSelector;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.ICMechanic;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.pipe.PipeRequestEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * IC utility functions.
 *
 * @author sk89q
 */
public final class ICUtil {

    private ICUtil() {
    }

    /**
     * Set an IC's output state at a block.
     *
     * @param block
     * @param state
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

    private static final TextReplacementConfig OFF_REMOVER = TextReplacementConfig.builder().matchLiteral("[off]").replacement("").build();
    private static final TextReplacementConfig RAD_REMOVER = TextReplacementConfig.builder().matchLiteral("[rad]").replacement("").build();

    public static void parseSignFlags(CraftBookPlayer player, BukkitChangedSign sign) {

        for (int i = 2; i < 4; i++) {
            String line = PlainTextComponentSerializer.plainText().serialize(sign.getLine(i));
            if (line.contains("[off]")) {

                if (CraftBookPlugin.plugins.getWorldEdit() == null) {
                    sign.setLine(i, sign.getLine(i).replaceText(OFF_REMOVER));
                    player.printError("worldedit.ic.notfound");
                } else {
                    RegionSelector selector = WorldEdit.getInstance().getSessionManager().get(player).getRegionSelector(player.getWorld());

                    try {
                        if (selector instanceof CuboidRegionSelector) {

                            BlockVector3 centre = selector.getRegion().getMaximumPoint().add(selector.getRegion().getMinimumPoint());

                            centre = centre.divide(2);

                            BlockVector3 offset = centre.subtract(BukkitAdapter.adapt(sign.getBlock().getLocation()).toVector().toBlockPoint());

                            String x, y, z;

                            x = Double.toString(offset.x());
                            if (x.endsWith(".0"))
                                x = x.replace(".0", "");

                            y = Double.toString(offset.y());
                            if (y.endsWith(".0"))
                                y = y.replace(".0", "");

                            z = Double.toString(offset.z());
                            if (z.endsWith(".0"))
                                z = z.replace(".0", "");

                            TextReplacementConfig offsetReplacer = TextReplacementConfig.builder().matchLiteral("[off]").replacement("&" + x + ":" + y + ":" + z).build();
                            sign.setLine(i, sign.getLine(i).replaceText(offsetReplacer));
                        } else if (selector instanceof SphereRegionSelector) {
                            Vector3 centre = selector.getRegion().getCenter();
                            Vector3 offset = centre.subtract(BukkitAdapter.adapt(sign.getBlock().getLocation()).toVector());

                            String x, y, z;

                            x = Double.toString(offset.x());
                            if (x.endsWith(".0"))
                                x = x.replace(".0", "");

                            y = Double.toString(offset.y());
                            if (y.endsWith(".0"))
                                y = y.replace(".0", "");

                            z = Double.toString(offset.z());
                            if (z.endsWith(".0"))
                                z = z.replace(".0", "");

                            TextReplacementConfig offsetReplacer = TextReplacementConfig.builder().matchLiteral("[off]").replacement("&" + x + ":" + y + ":" + z).build();
                            sign.setLine(i, sign.getLine(i).replaceText(offsetReplacer));
                        } else { // Unsupported.
                            sign.setLine(i, sign.getLine(i).replaceText(OFF_REMOVER));
                            player.printError("worldedit.ic.unsupported");
                        }
                    } catch (IncompleteRegionException e) {
                        player.printError("worldedit.ic.noselection");
                    }
                }
            }

            if (line.contains("[rad]")) {

                if (CraftBookPlugin.plugins.getWorldEdit() == null) {
                    sign.setLine(i, sign.getLine(i).replaceText(RAD_REMOVER));
                    player.printError("worldedit.ic.notfound");
                } else {
                    RegionSelector selector = WorldEdit.getInstance().getSessionManager().get(player).getRegionSelector(player.getWorld());

                    try {
                        if (selector instanceof CuboidRegionSelector) {

                            String x, y, z;

                            x = Double.toString(Math.abs(selector.getRegion().getMaximumPoint().x() - selector.getRegion().getMinimumPoint().x()) / 2);
                            if (x.endsWith(".0"))
                                x = x.replace(".0", "");

                            y = Double.toString(Math.abs(selector.getRegion().getMaximumPoint().y() - selector.getRegion().getMinimumPoint().y()) / 2);
                            if (y.endsWith(".0"))
                                y = y.replace(".0", "");

                            z = Double.toString(Math.abs(selector.getRegion().getMaximumPoint().z() - selector.getRegion().getMinimumPoint().z()) / 2);
                            if (z.endsWith(".0"))
                                z = z.replace(".0", "");

                            TextReplacementConfig radReplacer = TextReplacementConfig.builder().matchLiteral("[rad]").replacement(x + "," + y + "," + z).build();
                            sign.setLine(i, sign.getLine(i).replaceText(radReplacer));
                        } else if (selector instanceof SphereRegionSelector) {

                            String x;

                            double amounts = ((EllipsoidRegion) selector.getRegion()).getRadius().x();

                            x = Double.toString(amounts);
                            if (x.endsWith(".0"))
                                x = x.replace(".0", "");

                            TextReplacementConfig radReplacer = TextReplacementConfig.builder().matchLiteral("[rad]").replacement(x).build();
                            sign.setLine(i, sign.getLine(i).replaceText(radReplacer));
                        } else { // Unsupported.
                            sign.setLine(i, sign.getLine(i).replaceText(RAD_REMOVER));
                            player.printError("worldedit.ic.unsupported");
                        }
                    } catch (IncompleteRegionException e) {
                        player.printError("worldedit.ic.noselection");
                    }
                }
            }
        }

        sign.update(false);
    }

    public static Vector3 parseUnsafeBlockLocation(String line) throws NumberFormatException, ArrayIndexOutOfBoundsException {

        line = line.replace("!", "").replace("^", "").replace("&", "");
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

    public static Block parseBlockLocation(Block sign, String line, LocationCheckType relative) {
        Block target = SignUtil.getBackBlock(sign);

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

        if (offsets.x() == 0 && offsets.y() == 0 && offsets.z() == 0)
            return target;

        if (relative == LocationCheckType.RELATIVE)
            target = LocationUtil.getRelativeOffset(sign, offsets.x(), offsets.y(), offsets.z());
        else if (relative == LocationCheckType.OFFSET)
            target = target.getRelative(offsets.x(), offsets.y(), offsets.z());
        else if (relative == LocationCheckType.ABSOLUTE)
            target = new Location(target.getWorld(), offsets.x(), offsets.y(), offsets.z()).getBlock();
        return target;
    }

    public static Block parseBlockLocation(BukkitChangedSign sign, int lPos, LocationCheckType relative) {

        return parseBlockLocation(sign.getBlock(), PlainTextComponentSerializer.plainText().serialize(sign.getLine(lPos)), relative);
    }

    public static Block parseBlockLocation(BukkitChangedSign sign, int lPos) {

        return parseBlockLocation(sign, lPos, ICMechanic.instance.defaultCoordinates);
    }

    public static Block parseBlockLocation(BukkitChangedSign sign) {

        return parseBlockLocation(sign, 2, ICMechanic.instance.defaultCoordinates);
    }

    public static void verifySignSyntax(BukkitChangedSign sign) throws ICVerificationException {

        verifySignLocationSyntax(sign, 2);
    }

    public static void verifySignLocationSyntax(BukkitChangedSign sign, int i) throws ICVerificationException {

        try {
            String line = PlainTextComponentSerializer.plainText().serialize(sign.getLine(i));
            String[] strings;
            line = line.replace("!", "").replace("^", "").replace("&", "");
            if (line.contains("=")) {
                String[] split = RegexUtil.EQUALS_PATTERN.split(line, 2);
                if (RegexUtil.COMMA_PATTERN.split(split[0]).length > 1) {

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

    public static Vector3 parseRadius(BukkitChangedSign sign) {
        return parseRadius(sign, 2);
    }

    public static Vector3 parseRadius(BukkitChangedSign sign, int lPos) {
        return parseRadius(PlainTextComponentSerializer.plainText().serialize(sign.getLine(lPos)));
    }

    public static Vector3 parseRadius(String line) {

        Vector3 radius = Vector3.at(10, 10, 10);
        try {
            radius = parseUnsafeRadius(line);
        } catch (NumberFormatException ignored) {
        }
        return radius;
    }

    public static Vector3 parseUnsafeRadius(String line) throws NumberFormatException {
        String[] radians = RegexUtil.COMMA_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(line, 2)[0]);
        if (radians.length > 1) {
            double x = VerifyUtil.verifyRadius(Double.parseDouble(radians[0]), ICMechanic.instance.maxRange);
            double y = VerifyUtil.verifyRadius(Double.parseDouble(radians[1]), ICMechanic.instance.maxRange);
            double z = VerifyUtil.verifyRadius(Double.parseDouble(radians[2]), ICMechanic.instance.maxRange);
            return Vector3.at(x, y, z);
        } else {
            double r = Double.parseDouble(radians[0]);
            r = VerifyUtil.verifyRadius(r, ICMechanic.instance.maxRange);
            return Vector3.at(r, r, r);
        }
    }

    public static void collectItem(AbstractIC ic, BlockVector3 offset, ItemStack... items) {
        Sign sign = ic.getSign().getSign();
        Block backB = ic.getBackBlock();
        BlockFace back = SignUtil.getBack(sign.getBlock());

        Block pipe = backB.getRelative(back);

        // Handle the event
        PipeRequestEvent event = new PipeRequestEvent(pipe, new ArrayList<>(Arrays.asList(items)), backB);
        Bukkit.getPluginManager().callEvent(event);

        Collection<ItemStack> results = event.getItems();

        // If there is a chest add the results to the chest
        Block invHolder = backB.getRelative(offset.x(), offset.y(), offset.z());
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

            for (LocationCheckType t : values())
                if (t.c == c)
                    return t;

            return RELATIVE;
        }

        public static LocationCheckType getTypeFromName(String name) {

            if (name.length() == 1)
                return getTypeFromChar(name.charAt(0));
            for (LocationCheckType t : values())
                if (t.name().equalsIgnoreCase(name))
                    return t;

            return RELATIVE;
        }
    }
}