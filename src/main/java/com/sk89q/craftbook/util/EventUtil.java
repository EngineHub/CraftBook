package com.sk89q.craftbook.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class EventUtil {

    private static final Map<Event, Long> ignoredEvents = new WeakHashMap<Event, Long>();

    public static boolean shouldIgnoreEvent(Event ev) {

        if(!ignoredEvents.containsKey(ev)) return false;

        Long time = ignoredEvents.get(ev);
        if(System.currentTimeMillis() - time.longValue() > 1000*5)
            ignoredEvents.remove(ev);

        return true;
    }

    private static int lastGarbageCollect = 0;

    public static void ignoreEvent(Event ev) {

        if(++lastGarbageCollect > 100)
            garbageCollectEvents();
        ignoredEvents.put(ev, System.currentTimeMillis());
    }

    public static void garbageCollectEvents() {

        lastGarbageCollect = 0;
        Iterator<Entry<Event, Long>> iter = ignoredEvents.entrySet().iterator();

        while(iter.hasNext()) {

            Entry<Event, Long> bit = iter.next();

            if(System.currentTimeMillis() - bit.getValue().longValue() > 1000*5)
                iter.remove();
        }
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

        if(event instanceof Cancellable && ((Cancellable) event).isCancelled() && CraftBookPlugin.inst().getConfiguration().advancedBlockChecks)
            return false;

        if(EventUtil.shouldIgnoreEvent(event)) return false;

        return true;
    }
}