package com.sk89q.craftbook.bukkit.events;
import com.sk89q.craftbook.EventTrigger;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Author: Turtle9598
 */
public class CraftBookEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final boolean isDupeSafe;
    private final EventTrigger trigger;

    public CraftBookEvent(EventTrigger trigger, boolean isDupeSafe) {

        this.trigger = trigger;
        this.isDupeSafe = isDupeSafe;
    }

    /**
     * @return true if this event is protect from duplication
     */
    public boolean isDupeSafe() {

        return isDupeSafe;
    }

    /**
     * Gets the cause of this event
     *
     * @return the trigger
     */
    public EventTrigger getTrigger() {

        return trigger;
    }

    public boolean isCancelled() {

        return cancelled;
    }

    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
