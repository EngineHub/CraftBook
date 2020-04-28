/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package com.sk89q.craftbook.bukkit.util;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

// $Id$
/*
 * WorldEdit Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

public final class CraftBookBukkitUtil {

    private CraftBookBukkitUtil() {
    }

    public static void printStacktrace(Throwable e) {
        CraftBookPlugin.inst().getLogger().severe(CraftBookPlugin.getStackTrace(e));
    }

    public static ChangedSign toChangedSign(Block sign) {
        return toChangedSign(sign, null);
    }

    public static ChangedSign toChangedSign(Block block, String[] lines) {
        return toChangedSign(block, lines, null);
    }

    public static ChangedSign toChangedSign(Block block, String[] lines, CraftBookPlayer player) {
        if (!SignUtil.isSign(block)) return null;
        return new ChangedSign(block, lines, player);
    }

    public static Block toBlock(ChangedSign sign) {
        return sign.getBlock();
    }

    public static Sign toSign(ChangedSign sign) {
        try {
            if (sign.hasChanged()) sign.update(false);
            return sign.getSign();
        } catch (NullPointerException ex) {
            return null;
        }
    }

    public static BlockVector3 toVector(Block block) {
        return BlockVector3.at(block.getX(), block.getY(), block.getZ());
    }

    public static BlockVector3 toVector(BlockFace face) {
        return BlockVector3.at(face.getModX(), face.getModY(), face.getModZ());
    }

    public static Vector3 toVector(org.bukkit.Location loc) {
        return Vector3.at(loc.getX(), loc.getY(), loc.getZ());
    }

    public static Vector3 toVector(org.bukkit.util.Vector vector) {
        return Vector3.at(vector.getX(), vector.getY(), vector.getZ());
    }

    public static org.bukkit.Location toLocation(World world, Vector3 pt) {
        return new org.bukkit.Location(world, pt.getX(), pt.getY(), pt.getZ());
    }

    public static org.bukkit.Location center(org.bukkit.Location loc) {

        return new org.bukkit.Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() + 0.5,
                loc.getBlockZ() + 0.5, loc.getPitch(),
                loc.getYaw());
    }

    /**
     * Bukkit's Location class has serious problems with floating point precision.
     */
    public static boolean equals(org.bukkit.Location a, org.bukkit.Location b) {

        return Math.abs(a.getX() - b.getX()) <= EQUALS_PRECISION && Math.abs(a.getY() - b.getY()) <= EQUALS_PRECISION
                && Math.abs(a.getZ() - b.getZ()) <= EQUALS_PRECISION;
    }

    public static final double EQUALS_PRECISION = 0.0001;

}
