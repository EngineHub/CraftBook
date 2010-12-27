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
 * Used to wrap chests as Chest is woefully insufficient.
 *
 * @author sk89q
 */
public class ComparableComplexBlock<T extends ComplexBlock>
    implements PointBasedEntity {
    /**
     * Chest location.
     */
    private BlockVector pos;
    /**
     * Chest.
     */
    private T block;

    /**
     * Construct the object.
     * 
     * @param pos
     * @param block
     */
    public ComparableComplexBlock(Vector pos, T block) {
        this.pos = pos.toBlockVector();
        this.block = block;
    }

    /**
     * @return
     */
    public BlockVector getPosition() {
        return pos;
    }

    /**
     * @return
     */
    public T getChest() {
        return block;
    }

    /**
     * Equals check.
     * 
     * @param other
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object other) {
        if (other instanceof ComparableComplexBlock) {
            return ((ComparableComplexBlock)other).pos.equals(pos);
        } else {
            return false;
        }
    }

    /**
     * Get the hash code.
     * 
     * @return
     */
    @Override
    public int hashCode() {
        return pos.hashCode();
    }
}
