// $Id$
/*
 * CraftBook Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook;

/**
 * Mechanics with this interface indicate that they think outside of event handling. Self-triggered ICs used this,
 * for example.
 *
 * @author sk89q
 */
public interface SelfTriggeringMechanic extends Mechanic {

    /**
     * Think. This is called every 2 ticks if this mechanic is self-triggering. Implementation should be aware that
     * the mechanic may no longer exist
     * in the world.
     */
    public void think();
}
