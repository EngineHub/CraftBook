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

import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.Vector;

/**
 * For the uninitiated.
 *
 * @author sk89q
 */
public class DummyBlockBag extends BlockBag {
    private boolean fetch = true, store = true;
    
    public DummyBlockBag() {}
    public DummyBlockBag(boolean fetch, boolean store) {
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
        if(!fetch) throw new OutOfBlocksException(id);
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
        if(!store) throw new OutOfSpaceException(id);
    }

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSourcePosition(WorldInterface w, Vector pos) {
    }
    
    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSingleSourcePosition(WorldInterface w, Vector pos) {
    }

    /**
     * Flush changes.
     */
    public void flushChanges() {
    }
    
    /**
     * Unlimited black hole bag that will provided unlimited blocks and
     * discard any accepted blocks.
     * 
     * @author sk89q
     */
    public static class UnlimitedBlackHoleFactory implements BlockBagFactory {
        public BlockBag createBlockSource(WorldInterface w, Vector v) {
            return new DummyBlockBag();
        }
    }
    
    /**
     * Discards all given blocks.
     * 
     * @author sk89q
     */
    public static class BlackHoleFactory implements BlockBagFactory {
        public BlockBag createBlockSource(WorldInterface w, Vector v) {
            return new DummyBlockBag(false, true);
        }
    }
    
    /**
     * Provides unlimited blocks.
     * 
     * @author sk89q
     */
    public static class UnlimitedSourceFactory implements BlockBagFactory {
        public BlockBag createBlockSource(WorldInterface w, Vector v) {
            return new DummyBlockBag(true, false);
        }
    }
}
