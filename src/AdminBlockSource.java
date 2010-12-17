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

import com.sk89q.craftbook.*;

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
    public void fetchBlock(int id) throws BlockSourceException {
        if (!canFetch) {
        	throw new OutOfBlocksException(id);
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
    public void storeBlock(int id) throws BlockSourceException {
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
    public void addSourcePosition(Vector pos) {
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) { 
                    addSingleSourcePosition(pos.add(x, y, z));
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
    public void addSingleSourcePosition(Vector pos) {
        if (CraftBook.getBlockID(pos) == BlockType.WALL_SIGN
        		|| CraftBook.getBlockID(pos) == BlockType.SIGN_POST) {
        	
            Sign s = (Sign)etc.getServer().getComplexBlock(
            		pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
            
            if (store && s.getText(1).equalsIgnoreCase("[Black Hole]")) {
            	canStore = true;
            }
            
            if (fetch && s.getText(1).equalsIgnoreCase("[Block Source]")) {
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
    public static class BlackHoleFactory implements BlockSourceFactory {
        public BlockBag createBlockSource(Vector v) {
            return new AdminBlockSource(false, true);
        }
    }
    
    /**
     * Provides unlimited blocks.
     * 
     * @author sk89q
     */
    public static class UnlimitedSourceFactory implements BlockSourceFactory {
        public BlockBag createBlockSource(Vector v) {
            return new AdminBlockSource(true, false);
        }
    }
}
