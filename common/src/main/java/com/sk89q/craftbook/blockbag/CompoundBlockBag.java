package com.sk89q.craftbook.blockbag;
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

import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bags.BlockBagException;
import com.sk89q.worldedit.bags.OutOfBlocksException;
import com.sk89q.worldedit.bags.OutOfSpaceException;

import java.util.List;

/**
 * A collection of block bags.
 *
 * @author Lymia
 */
public class CompoundBlockBag extends BlockBag {

    private final List<BlockBag> sources;

    /**
     * Construct the instance.
     *
     * @param bags
     */
    public CompoundBlockBag(List<BlockBag> bags) {

        sources = bags;
    }

    /**
     * Store a block.
     *
     * @param id
     */
    @Override
    public void storeBlock(int id) throws BlockBagException {

        for (BlockBag b : sources) {
            try {
                b.storeBlock(id);
                return;
            } catch (OutOfSpaceException ignored) {
            }
        }
        throw new OutOfSpaceException(id);
    }

    /**
     * Get a block.
     *
     * @param id
     */
    @Override
    public void fetchBlock(int id) throws BlockBagException {

        for (BlockBag b : sources) {
            try {
                b.fetchBlock(id);
                return;
            } catch (OutOfBlocksException ignored) {
            }
        }
        throw new OutOfBlocksException();
    }

    /**
     * Return the list of missing blocks.
     *
     * @return
     */
    @Override
    public void flushChanges() {

        for (BlockBag b : sources) {
            b.flushChanges();
        }
    }

    @Override
    public void addSingleSourcePosition(WorldVector arg0) {

        for (BlockBag b : sources) {
            b.addSingleSourcePosition(arg0);
        }
    }

    @Override
    public void addSourcePosition(WorldVector arg0) {

        for (BlockBag b : sources) {
            b.addSourcePosition(arg0);
        }
    }
}
