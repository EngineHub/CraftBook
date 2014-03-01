package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class EventUtil {

    private static final Map<Event, Long> ignoredEvents = new WeakHashMap<Event, Long>();

    static String[] ignoredEventTypes = new String[0];

    public static boolean shouldIgnoreEvent(Event ev) {

        if(!shouldIgnoreEventType(ev.getClass().getSimpleName())) return false;

        if(CraftBookPlugin.inst() == null || !CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) return false;

        Long time = ignoredEvents.get(ev);

        if(time == null) return false;

        if(System.currentTimeMillis() - time.longValue() > 1000*3)
            ignoredEvents.remove(ev);

        return true;
    }

    private static int lastGarbageCollect = 0;

    public static void ignoreEvent(Event ev) {

        if(!CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) return;

        if(!shouldIgnoreEventType(ev.getClass().getSimpleName())) {
            List<String> evs = new ArrayList<String>();
            if(ignoredEventTypes.length > 0)
                evs.addAll(Arrays.asList(ignoredEventTypes));
            evs.add(ev.getClass().getSimpleName());
            ignoredEventTypes = evs.toArray(new String[evs.size()]);
        }

        if(++lastGarbageCollect > 100)
            garbageCollectEvents();
        ignoredEvents.put(ev, System.currentTimeMillis());
    }

    private static boolean shouldIgnoreEventType(String type) {

        for(String ev : ignoredEventTypes)
            if(ev.equalsIgnoreCase(type))
                return true;
        return false;
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

        if(CraftBookPlugin.inst() != null && CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) {
            if(event instanceof Cancellable && ((Cancellable) event).isCancelled())
                if(!(event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getClickedBlock() == null))
                    return false;
        }

        if(EventUtil.shouldIgnoreEvent(event)) return false;

        return true;
    }
}