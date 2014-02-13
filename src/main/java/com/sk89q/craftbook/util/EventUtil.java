package com.sk89q.craftbook.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class EventUtil {

    private static final Map<String, Long> ignoredEvents = new HashMap<String, Long>();

    public static boolean shouldIgnoreEvent(Event ev) {

        if(!ignoredEvents.containsKey(ev.toString())) return false;

        Long time = ignoredEvents.get(ev.toString());
        if(System.currentTimeMillis() - time.longValue() > 1000*3)
            ignoredEvents.remove(ev.toString());

        return true;
    }

    private static int lastGarbageCollect = 0;

    public static void ignoreEvent(Event ev) {

        if(++lastGarbageCollect > 100)
            garbageCollectEvents();
        ignoredEvents.put(ev.toString(), System.currentTimeMillis());
    }

    public static void garbageCollectEvents() {

        lastGarbageCollect = 0;
        Iterator<Entry<String, Long>> iter = ignoredEvents.entrySet().iterator();

        while(iter.hasNext()) {

            Entry<String, Long> bit = iter.next();

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

        if(CraftBookPlugin.inst() != null && CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) {
            if(event instanceof Cancellable && ((Cancellable) event).isCancelled())
                return false;
        }

        if(EventUtil.shouldIgnoreEvent(event)) return false;

        return true;
    }
}