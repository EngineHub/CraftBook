package com.sk89q.craftbook.mechanics.minecart.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.HandlerList;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import com.sk89q.craftbook.mechanics.minecart.blocks.CartMechanismBlocks;

public class CartBlockEnterEvent extends VehicleEnterEvent {

    private static final HandlerList handlers = new HandlerList();

    protected final CartMechanismBlocks blocks;

    public CartBlockEnterEvent (Vehicle vehicle, Entity entered, CartMechanismBlocks blocks) {
        super(vehicle, entered);

        this.blocks = blocks;
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

    public Minecart getMinecart() {

        return (Minecart) getVehicle();
    }
}