package com.sk89q.craftbook.util;
// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 Lymia <lymiahugs@gmail.com>
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.sk89q.craftbook.access.Action;
import com.sk89q.craftbook.access.WorldInterface;

/**
 * Thread for delaying redstone inputs.
 * 
 * @author Lymia
 * @author sk89q
 */
public class TickDelayer implements Runnable {
    /**
     * List of actions to delay.
     */
    private LinkedHashSet<Action> delayedActions = new LinkedHashSet<Action>();

    private WorldInterface world;
    
    public TickDelayer(WorldInterface world) {
        this.world = world;
    }
    
    /**
     * Delay an action.
     * 
     * @param action
     */
    public void delayAction(Action action) {
        delayedActions.add(action);
    }

    /**
     * Run thread.
     */
    public void run() {
        long currentTick = world.getTime();
        
        ArrayList<Action> actionQueue = new ArrayList<Action>();
        
        for (Iterator<Action> it = delayedActions.iterator(); it.hasNext(); ) {
            Action action = it.next();
            if (action.getRunAt() <= currentTick) {
                it.remove();
                actionQueue.add(action);
            }
        }
        
        for (Action action : actionQueue) {
            try {
                action.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
