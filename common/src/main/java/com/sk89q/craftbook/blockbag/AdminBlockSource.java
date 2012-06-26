// $Id$
/*
 * CraftBook
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

package com.sk89q.craftbook.blockbag;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bags.BlockBagException;
import com.sk89q.worldedit.bags.OutOfBlocksException;
import com.sk89q.worldedit.bags.OutOfSpaceException;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * Sign based block source system.
 *
 * @author Lymia
 */
public class AdminBlockSource extends BlockBag {
    private boolean fetch = true;
    private boolean store = true;
    private boolean canFetch = false;
    private boolean canStore = false;
    
    /**
     * Construct with the ability to both fetch and store.
     */
    public AdminBlockSource() {
    }
    
    /**
     * Construct the source with fetch/store options.
     * 
     * @param fetch
     * @param store
     */
    public AdminBlockSource(boolean fetch, boolean store) {
        this.fetch = fetch;
        this.store = store;
    }
    
    /**
     * Gets a block.
     *
     * @param pos
     * @param id
     * @return
     * @throws OutOfBlocksException
     */
    public void fetchBlock(int id) throws BlockBagException {
        if (!canFetch) {
            throw new OutOfBlocksException();
        }
    }

    /**
     * Stores a block.
     *
     * @param pos
     * @param id
     * @return
     * @throws OutOfSpaceException
     */
    public void storeBlock(int id) throws BlockBagException {
        if (!canStore) {
            throw new OutOfSpaceException(id);
        }
    }

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSourcePosition(WorldVector arg0) {
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) { 
                    addSingleSourcePosition(new WorldVector(arg0.getWorld(),arg0.add(x, y, z)));
                }
            }
        }
    }
    
    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSingleSourcePosition(WorldVector arg0) {
        Block e = BukkitUtil.toWorld(arg0.getWorld()).getBlockAt(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ()); 
        if(e.getState() instanceof Sign) {
            Sign s = (Sign) e.getState();
            
            if (store && s.getLine(2).equalsIgnoreCase("[Black Hole]")) {
                canStore = true;
            }
            
            if (fetch && s.getLine(2).equalsIgnoreCase("[Block Source]")) {
                canFetch = true;
            }
        }
    }

    /**
     * Flush changes.
     */
    public void flushChanges() {
    }
    
    /**
     * Discards all given blocks.
     * 
     * @author sk89q
     */
    public static class BlackHoleFactory implements BlockBagFactory {
        public BlockBag createBlockSource(World w, Vector v) {
            return new AdminBlockSource(false, true);
        }
    }
    
    /**
     * Provides unlimited blocks.
     * 
     * @author sk89q
     */
    public static class UnlimitedSourceFactory implements BlockBagFactory {
        public BlockBag createBlockSource(World w, Vector v) {
            return new AdminBlockSource(true, false);
        }
    }
}
