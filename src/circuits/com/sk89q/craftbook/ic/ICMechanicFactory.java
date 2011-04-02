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

import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.MechanicFactory;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

public class ICMechanicFactory implements MechanicFactory<ICMechanic> {
    
    /**
     * The pattern used to match an IC on a sign.
     */
    public static final Pattern codePattern =
            Pattern.compile("^\\[(MC[^\\]]+)\\]$", Pattern.CASE_INSENSITIVE);
    
    /**
     * Manager of ICs.
     */
    protected ICManager manager;
    
    /**
     * Holds the reference to the plugin.
     */
    protected CircuitsPlugin plugin;
    
    /**
     * Construct the object.
     * 
     * @param plugin
     * @param manager
     */
    public ICMechanicFactory(CircuitsPlugin plugin, ICManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }
    
    @Override
    public ICMechanic detect(BlockWorldVector pt) throws InvalidMechanismException {        
        Block block = pt.getWorld().getBlockAt(BukkitUtil.toLocation(pt));
        
        // if we're not looking at a wall sign, it can't be an IC.
        if (block.getTypeId() != BlockID.WALL_SIGN) return null;
        Sign sign = (Sign)block.getState();
        
        // detect the text on the sign to see if it's any kind of IC at all.
        Matcher matcher = codePattern.matcher(sign.getLine(1));
        if (!matcher.matches()) return null;
        String id = matcher.group(1);
        // after this point, we don't return null if we can't make an IC: we throw shit,
        //  because it SHOULD be an IC and can't possibly be any other kind of mechanic.
        
        // now actually try to pull up an IC of that id number.
        RegisteredICFactory registration = manager.get(id);
        if (registration == null) throw new InvalidMechanismException("\""+sign.getLine(1)+"\" should be an IC ID, but no IC registered under that ID could be found.");
        
        // check if it's a valid configuration (deferring details to the IC itself).
        // part of this was probably done when the blocks definig it were placed, too,
        //   but bukkit doesn't provide all the events we would need to know if things might have changed to make it invalid, so here we go.
        registration.getFactory().verify(sign);
        
        // okay, everything checked out.  we can finally make it.
        return new ICMechanic(
                plugin,
                id,
                registration.getFactory().create(sign),
                registration.getFamily(),
                pt
       );
    }
    
    //TODO: check this sometime during sign place events.
    private boolean canBuild(Player player, ICFactory pattern) {
        if (pattern.getPermissionName() == null) return true;
        String perm = "craftbook.ic.restricted." + pattern.getPermissionName();
        
        return plugin.getPermissionsResolver().hasPermission(player.getName(), perm);
    }
}
