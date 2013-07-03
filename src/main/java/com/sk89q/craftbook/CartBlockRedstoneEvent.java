package com.sk89q.craftbook;

import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.HandlerList;

import com.sk89q.craftbook.vehicles.cart.CartMechanismBlocks;

public class CartBlockRedstoneEvent extends SourcedBlockRedstoneEvent {

    private static final HandlerList handlers = new HandlerList();

    protected final CartMechanismBlocks blocks;
    protected final Minecart cart;

    public CartBlockRedstoneEvent (Block source, Block block, int old, int n, CartMechanismBlocks blocks, Minecart cart) {
        super(source, block, old, n);

        this.blocks = blocks;
        this.cart = cart;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public CartMechanismBlocks getBlocks() {

        return blocks;
    }

    /**
     * The minecart at this mechanic (If there is one)
     * 
     * @return the minecart (Can be null)
     */
    public Minecart getMinecart() {

        return cart;
    }
}