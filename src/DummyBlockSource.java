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
 * For the uninitiated.
 *
 * @author sk89q
 */
public class DummyBlockSource extends BlockBag {
    private boolean fetch = true, store = true;
    
    public DummyBlockSource() {}
    public DummyBlockSource(boolean fetch, boolean store) {
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
    public void storeBlock(int id) throws BlockSourceException {
        if(!store) throw new OutOfSpaceException(id);
    }

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSourcePosition(Vector pos) {
    }
    
    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSingleSourcePosition(Vector pos) {
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
        public BlockBag createBlockSource(Vector v) {
            return new DummyBlockSource();
        }
    }
    
    /**
     * Discards all given blocks.
     * 
     * @author sk89q
     */
    public static class BlackHoleFactory implements BlockBagFactory {
        public BlockBag createBlockSource(Vector v) {
            return new DummyBlockSource(false, true);
        }
    }
    
    /**
     * Provides unlimited blocks.
     * 
     * @author sk89q
     */
    public static class UnlimitedSourceFactory implements BlockBagFactory {
        public BlockBag createBlockSource(Vector v) {
            return new DummyBlockSource(true, false);
        }
    }
}
