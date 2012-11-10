package com.sk89q.craftbook.bukkit.events;
import com.sk89q.craftbook.CraftItemStack;
import com.sk89q.craftbook.EventTrigger;

public class CraftBookItemChangeEvent extends CraftBookEvent {

    private CraftItemStack itemStack;
    private CraftItemStack toItemStack;

    public CraftBookItemChangeEvent(CraftItemStack itemStack, CraftItemStack toItemStack,
                                    EventTrigger trigger, boolean isDupeSafe) {

        super(trigger, isDupeSafe);
        this.itemStack = itemStack;
        this.toItemStack = toItemStack;
    }

    /**
     * Gets the original ItemStack returns null if this is a creation event
     *
     * @return the original ItemStack
     */
    public CraftItemStack getItemStack() {

        return itemStack;
    }

    /**
     * Gets the modified ItemStack returns null if this is a destruction event
     *
     * @return the modified ItemStack
     */
    public CraftItemStack getToItemStack() {

        return toItemStack;
    }
}
