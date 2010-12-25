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

import com.sk89q.craftbook.SignText;

/**
 * A version of SignText that gets its sign text from a hMod Sign object.
 * 
 * @author sk89q
 */
public class HmodSignText extends SignText {
    /**
     * Sign instance.
     */
    private Sign sign;
    
    /**
     * Construct an instance. SignText will be constructed with the 4 lines
     * of text from the sign.
     * 
     * @param sign
     */
    public HmodSignText(Sign sign) {
        super(sign.getText(0), sign.getText(1),
                sign.getText(2), sign.getText(3));
        this.sign = sign;
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
            sign.update();
        }
    }
}
