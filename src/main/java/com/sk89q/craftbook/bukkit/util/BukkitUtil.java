package com.sk89q.craftbook.bukkit.util;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public final class BukkitUtil {

    public static void printStacktrace(Throwable e) {
        CraftBookPlugin.inst().getLogger().severe(CraftBookPlugin.getStackTrace(e));
    }

    public static ChangedSign toChangedSign(Block sign) {
        return toChangedSign(sign, null);
    }

    public static ChangedSign toChangedSign(Block block, String[] lines) {
        return toChangedSign(block, lines, null);
    }

    public static ChangedSign toChangedSign(Block block, String[] lines, LocalPlayer player) {
        return CraftBookPlugin.inst().getNmsAdapter().getChangedSign(block, lines, player);
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

    private static final Map<String, LocalWorld> wlw = new HashMap<>();

    public static LocalWorld getLocalWorld(World w) {
        return wlw.computeIfAbsent(w.getName(), k -> new BukkitWorld(w));
    }

    public static Vector toVector(Block block) {

        return new Vector(block.getX(), block.getY(), block.getZ());
    }

    public static Vector toVector(BlockFace face) {

        return new Vector(face.getModX(), face.getModY(), face.getModZ());
    }

    public static BlockWorldVector toWorldVector(Block block) {

        return new BlockWorldVector(getLocalWorld(block.getWorld()), block.getX(), block.getY(), block.getZ());
    }

    public static Vector toVector(org.bukkit.Location loc) {

        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }

    public static Location toLocation(org.bukkit.Location loc) {

        return new Location(getLocalWorld(loc.getWorld()), new Vector(loc.getX(), loc.getY(), loc.getZ()),
                loc.getYaw(), loc.getPitch());
    }

    public static Vector toVector(org.bukkit.util.Vector vector) {

        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static org.bukkit.Location toLocation(WorldVector pt) {

        return new org.bukkit.Location(toWorld(pt), pt.getX(), pt.getY(), pt.getZ());
    }

    public static org.bukkit.Location toLocation(World world, Vector pt) {

        return new org.bukkit.Location(world, pt.getX(), pt.getY(), pt.getZ());
    }

    public static org.bukkit.Location center(org.bukkit.Location loc) {

        return new org.bukkit.Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() + 0.5,
                loc.getBlockZ() + 0.5, loc.getPitch(),
                loc.getYaw());
    }

    public static Block toBlock(BlockWorldVector pt) {

        return toWorld(pt).getBlockAt(toLocation(pt));
    }

    public static World toWorld(WorldVector pt) {

        return ((BukkitWorld) pt.getWorld()).getWorld();
    }

    /**
     * Bukkit's Location class has serious problems with floating point precision.
     */
    public static boolean equals(org.bukkit.Location a, org.bukkit.Location b) {

        return Math.abs(a.getX() - b.getX()) <= EQUALS_PRECISION && Math.abs(a.getY() - b.getY()) <= EQUALS_PRECISION
                && Math.abs(a.getZ() - b.getZ()) <= EQUALS_PRECISION;
    }

    public static final double EQUALS_PRECISION = 0.0001;

    public static org.bukkit.Location toLocation(Location teleportLocation) {
        return new org.bukkit.Location(
                toWorld((com.sk89q.worldedit.world.World) teleportLocation.getExtent()),
                teleportLocation.getX(),
                teleportLocation.getY(),
                teleportLocation.getZ(),
                teleportLocation.getYaw(),
                teleportLocation.getPitch()
        );
    }

    public static World toWorld(final com.sk89q.worldedit.world.World world) {
        return ((BukkitWorld) world).getWorld();
    }
}
