// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 Shaun (sturmeh)
 * Copyright (C) 2010 sk89q
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

package com.sk89q.craftbook.ic.logic;

import java.util.*;

import org.bukkit.block.*;

import com.sk89q.craftbook.ic.*;
import com.sk89q.worldedit.*;

/**
 * LogicChipState can be used to describe any IC that has clearly defined input
 * and output wires.
 * 
 * @author hash
 */
public interface LogicChipState extends ChipState {
    /**
     * @param n
     * @return the state of the n'th input.
     */
    public boolean getIn(int n);
    
    /**
     * @param n
     * @return the state of the n'th output.
     */
    public boolean getOut(int n);
    
    /**
     * @param n
     * @param value the state to set the n'th output to.
     */
    public void setOut(int n, boolean value);
}
