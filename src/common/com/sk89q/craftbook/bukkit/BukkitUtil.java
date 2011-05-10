// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.bukkit;

import java.util.List;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Server;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.craftbook.util.WorldVector;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;

public class BukkitUtil {
    private BukkitUtil()  {
    }
    
    public static BlockVector toVector(Block block) {
        return new BlockVector(block.getX(), block.getY(), block.getZ());
    }
    
    public static BlockVector toVector(BlockFace face) {
        return new BlockVector(face.getModX(), face.getModY(), face.getModZ());
    }
    
    public static BlockWorldVector toWorldVector(Block block) {
        return new BlockWorldVector(block.getWorld(), block.getX(),
                block.getY(), block.getZ());
    }
    
    public static Vector toVector(Location loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }
    
    public static Vector toVector(org.bukkit.util.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }
    
    public static Location toLocation(WorldVector pt) {
        return new Location(pt.getWorld(), pt.getX(), pt.getY(), pt.getZ());
    }
    
    public static Player matchSinglePlayer(Server server, String name) {
        List<Player> players = server.matchPlayer(name);
        if (players.size() == 0) {
            return null;
        }
        return players.get(0);
    }
}
