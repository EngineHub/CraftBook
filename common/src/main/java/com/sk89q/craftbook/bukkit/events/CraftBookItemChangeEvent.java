package com.sk89q.craftbook.bukkit.events;
import com.sk89q.craftbook.EventTrigger;
import com.sk89q.worldedit.blocks.BaseItemStack;

public class CraftBookItemChangeEvent extends CraftBookEvent {

    private BaseItemStack itemStack;
    private BaseItemStack toItemStack;

    public CraftBookItemChangeEvent(BaseItemStack itemStack, BaseItemStack toItemStack,
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
    public BaseItemStack getItemStack() {

        return itemStack;
    }

    /**
     * Gets the modified ItemStack returns null if this is a destruction event
     *
     * @return the modified ItemStack
     */
    public BaseItemStack getToItemStack() {

        return toItemStack;
    }
}
