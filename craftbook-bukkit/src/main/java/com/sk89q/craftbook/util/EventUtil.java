/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package com.sk89q.craftbook.util;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public final class EventUtil {

    private EventUtil() {
    }

    private static final Map<Event, Long> ignoredEvents = new WeakHashMap<>();

    private static final Set<Class<?>> ignoredEventTypes = new HashSet<>();

    public static boolean shouldIgnoreEvent(Event ev) {

        if(!shouldIgnoreEventType(ev.getClass())) return false;

        if(CraftBookPlugin.inst() == null || !CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) return false;

        Long time = ignoredEvents.get(ev);

        if(time == null) return false;

        if(System.currentTimeMillis() - time > 1000*3)
            ignoredEvents.remove(ev);

        return true;
    }

    private static int lastGarbageCollect = 0;

    public static void ignoreEvent(Event ev) {

        if (!CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) return;

        if (!shouldIgnoreEventType(ev.getClass())) {
            ignoredEventTypes.add(ev.getClass());
        }

        if (++lastGarbageCollect > 100) garbageCollectEvents();
        ignoredEvents.put(ev, System.currentTimeMillis());
    }

    private static boolean shouldIgnoreEventType(Class<?> type) {
        return ignoredEventTypes.contains(type);
    }

    public static void garbageCollectEvents() {

        lastGarbageCollect = 0;
        ignoredEvents.entrySet().removeIf(bit -> System.currentTimeMillis() - bit.getValue() > 1000 * 5);
    }


    /**
     * Used to filter events for processing. This allows for short circuiting code so that code isn't checked
     * unnecessarily.
     *
     * @param event
     *
     * @return true if the event should be processed by this manager; false otherwise.
     */
    public static boolean passesFilter(Event event) {

        if(CraftBookPlugin.inst() != null && CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) {
            if(event instanceof Cancellable && ((Cancellable) event).isCancelled())
                if(!(event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getClickedBlock() == null))
                    return false;
        }

        return !EventUtil.shouldIgnoreEvent(event);
    }
}