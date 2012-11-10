package com.sk89q.craftbook.bukkit.events;
import com.sk89q.craftbook.EventTrigger;
import org.bukkit.inventory.ItemStack;

/**
 * Author: Turtle9598
 */
public class CraftBookItemChangeEvent extends CraftBookEvent {

    private ItemStack itemStack;
    private ItemStack toItemStack;

    public CraftBookItemChangeEvent(ItemStack itemStack, ItemStack toItemStack,
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
    public ItemStack getItemStack() {

        return itemStack;
    }

    /**
     * Gets the modified ItemStack returns null if this is a destruction event
     *
     * @return the modified ItemStack
     */
    public ItemStack getToItemStack() {

        return toItemStack;
    }
}
