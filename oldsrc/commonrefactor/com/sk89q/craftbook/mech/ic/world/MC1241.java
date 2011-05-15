package com.sk89q.craftbook.mech.ic.world;
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

import com.sk89q.craftbook.mech.ic.ChipState;
import com.sk89q.craftbook.util.Vector;

/**
 * Shoots arrows.
 *
 * @author sk89q
 */
public class MC1241 extends MC1240 {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "ARROW BARRAGE";
    }
    
    /**
     * Shoot the arrow.
     * 
     * @param chip
     * @param speed
     * @param spread
     * @param vertVel
     */
    protected void shoot(ChipState chip, float speed, float spread, float vertVel) {
        for (int i = 0; i < 5; i++) {
            Vector backDir = chip.getBlockPosition().subtract(
                    chip.getPosition());
            Vector firePos = chip.getBlockPosition().add(backDir);
            chip.getWorld().shootArrow(firePos.getBlockX() + 0.5, firePos.getBlockY() + 0.5, firePos.getBlockZ() + 0.5, 
                                       backDir.getBlockX(), vertVel, backDir.getBlockZ(), 
                                       speed, spread);
        }
    }
}
