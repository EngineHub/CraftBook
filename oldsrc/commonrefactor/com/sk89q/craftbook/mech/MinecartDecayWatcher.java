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

package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.access.MinecartInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.HistoryHashMap;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Keeps track of empty minecarts and removes them after they have exceeded
 * a period of emptiness. All calls should occur in the main thread
 * as most calls are not thread-safe!
 *
 * @author sk89q
 */
public class MinecartDecayWatcher {

    /**
     * Timer to schedule tasks on.
     */
    private Timer timer = new Timer();
    /**
     * Stores a list of minecarts.
     */
    private HashMap<WorldInterface, HistoryHashMap<Integer, Long>> minecarts
            = new HashMap<WorldInterface, HistoryHashMap<Integer, Long>>();

    /**
     * Maximum age of minecarts.
     */
    private long delay = 0;

    /**
     * Construct the object and start the timer.
     *
     * @param delay maximum age of empty minecarts in seconds
     */
    public MinecartDecayWatcher(int delay) {

        this.delay = delay * 1000;

        timer.scheduleAtFixedRate(new TimerTask() {

            /**
             * Runs the check.
             */
            @Override
            public void run() {

                WorldInterface[] worlds = minecarts.keySet().toArray(new WorldInterface[0]);
                for (final WorldInterface world : worlds)
                    world.enqueAction(new Runnable() {

                        /**
                         * Performs the check.
                         */
                        @Override
                        public void run() {

                            performCheck(world);
                        }
                    });
            }
        }, 0, 3000);
    }

    /**
     * Performs the check. This must be run in the same thread as the server
     * or bad things may happen. It is already run automatically by the
     * TimerTask at a periodic interval.
     */
    private void performCheck(WorldInterface world) {

        long now = System.currentTimeMillis();

        HistoryHashMap<Integer, Long> minecarts = getCarts(world);

        for (MinecartInterface minecart : world.getMinecartList()) {
            if (minecart.hasPassenger()) {
                // We don't need to update the hash map because a player
                // existing the minecart will update the hash map,
                // and until the minecart is empty it won't be considered
                // be deletion
            } else {
                Long then = minecarts.get(minecart.getEntityId());

                if (then == null) {
                    minecarts.put(minecart.getEntityId(), System.currentTimeMillis());
                } else if (now - then > delay) {
                    minecart.remove();
                    forgetMinecart(minecart);
                }
            }
        }
    }

    /**
     * Track a player entering or exiting a minecart.
     *
     * @param minecart
     */
    public void trackEnter(WorldInterface world, MinecartInterface minecart) {

        getCarts(world).put(minecart.getEntityId(), System.currentTimeMillis());
    }

    /**
     * Forget about a minecart.
     *
     * @param minecart
     */
    public void forgetMinecart(MinecartInterface minecart) {

        minecarts.remove(minecart.getEntityId());
    }

    /**
     * Stops watching. After this is called, this watcher instance can no
     * longer be used.
     */
    public void disable() {

        timer.cancel();
    }

    public synchronized void addWorld(WorldInterface w) {

        minecarts.put(w, new HistoryHashMap<Integer, Long>(500));
    }

    public synchronized void removeWorld(WorldInterface w) {

        minecarts.remove(w);
    }

    protected synchronized HistoryHashMap<Integer, Long> getCarts(WorldInterface w) {

        if (!minecarts.containsKey(w)) return minecarts.get(w);
        else {
            HistoryHashMap<Integer, Long> ret;
            minecarts.put(w, ret = new HistoryHashMap<Integer, Long>(500));
            return ret;
        }
    }
}
