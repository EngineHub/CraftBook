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

import java.util.List;

import com.sk89q.craftbook.BlockBagException;
import com.sk89q.craftbook.OutOfBlocksException;
import com.sk89q.craftbook.OutOfSpaceException;
import com.sk89q.craftbook.Vector;

public class CompoundBlockBag extends BlockBag {
    private List<BlockBag> bags;
    public CompoundBlockBag(List<BlockBag> bags) {
        this.bags = bags;
    }
    
    public void storeBlock(int id) throws BlockBagException {
        for(BlockBag b:bags) try {b.storeBlock(id);return;} catch (OutOfSpaceException e) {}
        throw new OutOfSpaceException(id);
    }
    public void fetchBlock(int id) throws BlockBagException {
        for(BlockBag b:bags) try {b.fetchBlock(id);return;} catch (OutOfBlocksException e) {}
        throw new OutOfBlocksException(id);
    }

    public void addSingleSourcePosition(Vector pos) {
        for(BlockBag b:bags) b.addSingleSourcePosition(pos);
    }

    public void addSourcePosition(Vector pos) {
        for(BlockBag b:bags) b.addSourcePosition(pos);
    }

    public void flushChanges() {
        for(BlockBag b:bags) b.flushChanges();
    }
}
