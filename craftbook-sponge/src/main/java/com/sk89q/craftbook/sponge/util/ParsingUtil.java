/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.util;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.RegexUtil;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.ics.ICSocket;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class ParsingUtil {

    public static Vector3d parseUnsafeBlockLocation(String line) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        line = StringUtils.replace(StringUtils.replace(StringUtils.replace(line, "!", ""), "^", ""), "&", "");
        double offsetX = 0, offsetY, offsetZ = 0;

        if (line.contains("="))
            line = RegexUtil.EQUALS_PATTERN.split(line)[1];
        String[] split = RegexUtil.COLON_PATTERN.split(line);
        if (split.length > 1) {
            offsetX = Double.parseDouble(split[0]);
            offsetY = Double.parseDouble(split[1]);
            offsetZ = Double.parseDouble(split[2]);
        } else
            offsetY = Double.parseDouble(line);

        return new Vector3d(offsetX, offsetY, offsetZ);
    }

    public static Location<World> parseBlockLocation(Sign sign, String line, LocationCheckType relative) {
        Location<World> target = SignUtil.getBackBlock(sign.getLocation());

        if (line.contains("!"))
            relative = LocationCheckType.getTypeFromChar('!');
        else if (line.contains("^"))
            relative = LocationCheckType.getTypeFromChar('^');
        else if (line.contains("&"))
            relative = LocationCheckType.getTypeFromChar('&');

        Vector3d offsets = new Vector3d(0,0,0);

        try {
            offsets = parseUnsafeBlockLocation(line);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
        }

        if(offsets.getFloorX() == 0 && offsets.getFloorY() == 0 && offsets.getFloorZ() == 0)
            return target;

        if (relative == LocationCheckType.RELATIVE)
            target = LocationUtil.getRelativeOffset(sign.getLocation(), offsets.getFloorX(), offsets.getFloorY(), offsets.getFloorZ());
        else if (relative == LocationCheckType.OFFSET)
            target = LocationUtil.getOffset(target, offsets.getFloorX(), offsets.getFloorY(), offsets.getFloorZ());
        else if (relative == LocationCheckType.ABSOLUTE)
            target = target.getExtent().getLocation(offsets.getX(), offsets.getY(), offsets.getZ());
        return target;
    }

    public static Location<World> parseBlockLocation(Sign sign, int lPos, LocationCheckType relative) {
        return parseBlockLocation(sign, SignUtil.getTextRaw(sign, lPos), relative);
    }

    public static Location<World> parseBlockLocation(Sign sign, int lPos) {
        return parseBlockLocation(sign, lPos, LocationCheckType.OFFSET);
    }

    public static Location<World> parseBlockLocation(Sign sign) {
        return parseBlockLocation(sign, 2, LocationCheckType.OFFSET);
    }

    public static void verifySignSyntax(Sign sign) throws CraftBookException {
        verifySignLocationSyntax(sign, 2);
    }

    public static void verifySignLocationSyntax(Sign sign, int i) throws CraftBookException {
        try {
            String line = SignUtil.getTextRaw(sign, i);
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
            throw new CraftBookException("Wrong syntax! Needs to be: radius=x:y:z or radius=y or y");
        }
    }

    // Radius

    public static Vector3d parseRadius(Sign sign) {
        return parseRadius(sign, 2);
    }

    public static Vector3d parseRadius(Sign sign, int lPos) {
        return parseRadius(SignUtil.getTextRaw(sign, lPos));
    }

    public static Vector3d parseRadius(String line) {
        Vector3d radius = new Vector3d(10,10,10);
        try {
            radius = parseUnsafeRadius(line);
        } catch (NumberFormatException ignored) {
        }
        return radius;
    }

    public static Vector3d parseUnsafeRadius(String line) throws NumberFormatException {
        String[] radians = RegexUtil.COMMA_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(line, 2)[0]);
        if(radians.length > 1) {
            double x = verifyRadius(Double.parseDouble(radians[0]), CraftBookPlugin.spongeInst().moduleController.getModule(ICSocket.class).get().getModule().get().maxRadius.getValue());
            double y = verifyRadius(Double.parseDouble(radians[1]), CraftBookPlugin.spongeInst().moduleController.getModule(ICSocket.class).get().getModule().get().maxRadius.getValue());
            double z = verifyRadius(Double.parseDouble(radians[2]), CraftBookPlugin.spongeInst().moduleController.getModule(ICSocket.class).get().getModule().get().maxRadius.getValue());
            return new Vector3d(x,y,z);
        }
        else {
            double r = Double.parseDouble(radians[0]);
            r = verifyRadius(r, CraftBookPlugin.spongeInst().moduleController.getModule(ICSocket.class).get().getModule().get().maxRadius.getValue());
            return new Vector3d(r,r,r);
        }
    }

    /**
     * Verify that a radius is within the maximum.
     *
     * @param radius The radius to check
     * @param maxradius The maximum possible radius
     * @return The new fixed radius.
     */
    public static double verifyRadius(double radius, double maxradius) {
        return Math.max(0, Math.min(maxradius, radius));
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
