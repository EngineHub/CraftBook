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

import com.sk89q.craftbook.access.SignInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.BlockVector;
import com.sk89q.craftbook.util.Vector;

/**
 * A version of SignText that gets its sign text from a hMod Sign object.
 *
 * @author sk89q
 */
public class HmodSignImpl extends SignInterface {

    /**
     * Sign instance.
     */
    private Sign sign;

    private final WorldInterface w;
    private final BlockVector pos;

    /**
     * Construct an instance. SignText will be constructed with the 4 lines
     * of text from the sign.
     *
     * @param sign
     */
    public HmodSignImpl(WorldInterface w, BlockVector pos, Sign sign) {

        super(sign.getText(0), sign.getText(1),
                sign.getText(2), sign.getText(3));
        this.sign = sign;
        this.pos = pos;
        this.w = w;
    }

    public int getX() {

        return sign.getX();
    }

    public int getY() {

        return sign.getY();
    }

    public int getZ() {

        return sign.getZ();
    }

    /**
     * Flush changes to world.
     */
    public void flushChanges() {

        if (isChanged()) {
            sign.setText(0, getLine1());
            sign.setText(1, getLine2());
            sign.setText(2, getLine3());
            sign.setText(3, getLine4());
            if (shouldUpdate()) sign.update();
        }
    }

    public boolean equals(Object other) {

        if (other instanceof HmodSignImpl) {
            HmodSignImpl sign = ((HmodSignImpl) other);
            return sign.pos.equals(pos) && sign.w.equals(w);
        } else {
            return false;
        }
    }

    public int hashCode() {

        return pos.hashCode() * 31 + w.hashCode();
    }

    public Vector getPosition() {

        return pos;
    }

    public WorldInterface getWorld() {

        return w;
    }
}
