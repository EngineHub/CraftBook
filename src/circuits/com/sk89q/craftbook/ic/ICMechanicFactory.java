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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

public class ICMechanicFactory implements MechanicFactory<ICMechanic> {
    
    /**
     * The pattern used to match an IC on a sign.
     */
    public static final Pattern codePattern =
            Pattern.compile("^\\[(MC[^\\]]+\\)]$", Pattern.CASE_INSENSITIVE);
    
    /**
     * Map of IC ID to ICFactory.
     */
    protected Map<String, ICFactory> factories;
    
    /**
     * Holds the reference to the plugin.
     */
    protected MechanismsPlugin plugin;
    
    /**
     * Construct the object.
     * 
     * @param plugin
     */
    public ICMechanicFactory(MechanismsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void register(ICFactory factory) {
        factories.put(factory.getID(), factory);
    }
    
    

    @Override
    public ICMechanic detect(BlockWorldVector pt) throws InvalidMechanismException {
        Block block = pt.getWorld().getBlockAt(BukkitUtil.toLocation(pt));
        
        if (block.getTypeId() == BlockID.WALL_SIGN) {
            Sign sign = (Sign)block.getState();
            
            // Attempt to detect the text on the sign to see if it's an IC
            Matcher matcher = codePattern.matcher(sign.getLine(1));
            if (matcher.matches()) {
                return setup(block, matcher.group(1));
            }
        }
        
        return null;
    }

    /**
     * Sets up an IC at the specified location.
     * 
     * @param block
     *            the sign that defines the IC
     * @param id
     * @return a new ICMechanic wrapping a new IC at the location, or null if
     *         the id string didn't match any known ICFactory.
     */
    protected ICMechanic setup(Block block, String id) {
        ICFactory factory = factories.get(id);
        
        // No registration! No IC! Abort
        if (factory == null) return null;
        
        IC ic = factory.create(block);
        return new ICMechanic(plugin, id, ic);
    }

}
