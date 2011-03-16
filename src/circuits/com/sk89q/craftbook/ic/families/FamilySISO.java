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

package com.sk89q.craftbook.ic.families;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractICFamily;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.util.SignUtil;

/**
 * Handles detection for the single input single output family.
 *  
 * @author sk89q
 */
public class FamilySISO extends AbstractICFamily {

    @Override
    public ChipState detect(Sign sign) {
        return new ChipStateSISO(sign);
    }
    
    public static class ChipStateSISO implements ChipState {
        
        protected Sign sign;
        
        public ChipStateSISO(Sign sign) {
            this.sign = sign;
        }

        @Override
        public boolean get(int pin) {
            Block front = SignUtil.getFrontBlock(sign.getBlock());
            return front.isBlockIndirectlyPowered();
        }

        @Override
        public void set(int pin, boolean value) {
            BlockFace face = SignUtil.getBack(sign.getBlock());
            ICUtil.setState(sign.getBlock().getRelative(face).getRelative(face), value);
        }
        
    }
    
}
