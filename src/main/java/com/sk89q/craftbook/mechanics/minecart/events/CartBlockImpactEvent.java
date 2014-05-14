package com.sk89q.craftbook.mechanics.minecart.events;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.event.HandlerList;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import com.sk89q.craftbook.mechanics.minecart.blocks.CartMechanismBlocks;

public class CartBlockImpactEvent extends VehicleMoveEvent {

    private static final HandlerList handlers = new HandlerList();

    protected final CartMechanismBlocks blocks;
    protected final boolean minor;

    public CartBlockImpactEvent(Minecart minecart, Location from, Location to, CartMechanismBlocks blocks, boolean minor) {
        super(minecart, from, to);

        this.blocks = blocks;
        this.minor = minor;
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

    public boolean isMinor() {

        return minor;
    }

    public Minecart getMinecart() {

        return (Minecart) getVehicle();
    }
}