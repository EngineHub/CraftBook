/*
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

package org.enginehub.craftbook.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.enginehub.craftbook.CraftBook;

import java.util.HashSet;
import java.util.Set;

public final class EventUtil {

    private EventUtil() {
    }

    private static final Set<Event> ignoredEvents = new HashSet<>();

    public static void callEventSafely(Event event) {
        ignoredEvents.add(event);
        Bukkit.getPluginManager().callEvent(event);
        ignoredEvents.remove(event);
    }

    public static boolean shouldIgnoreEvent(Event ev) {
        return ignoredEvents.contains(ev);
    }

    /**
     * Used to filter events for processing. This allows for short circuiting code so that code
     * isn't checked
     * unnecessarily.
     *
     * @param event The event to check
     * @return true if the event should be processed by this manager; false otherwise.
     */
    public static boolean passesFilter(Event event) {
        if (CraftBook.getInstance().getPlatform().getConfiguration().obeyPluginProtections && event instanceof Cancellable cancellable && cancellable.isCancelled()) {
            return false;
        }

        return !EventUtil.shouldIgnoreEvent(event);
    }
}
