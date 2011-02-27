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

package com.sk89q.craftbook.circuits;

import static com.sk89q.craftbook.bukkit.BukkitUtil.toLocation;
import com.sk89q.craftbook.MechanicFactory;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

public class JackOLanternFactory implements MechanicFactory<JackOLantern> {
    
    public JackOLanternFactory() {
    }

    @Override
    public JackOLantern detect(BlockWorldVector pt) {
        int type = pt.getWorld().getBlockTypeIdAt(toLocation(pt));
        
        if (type == BlockID.PUMPKIN || type == BlockID.JACKOLANTERN) {
            return new JackOLantern(pt);
        }
        
        return null;
    }

}
