package com.sk89q.craftbook.bukkit;

import com.sk89q.craftbook.Vehicle;
import com.sk89q.worldedit.Location;

public class BukkitVehicle implements Vehicle {

    protected final org.bukkit.entity.Vehicle vehicle;

    public org.bukkit.entity.Vehicle getVehicle() {
        return vehicle;
    }

    public BukkitVehicle(org.bukkit.entity.Vehicle vehicle) {

        this.vehicle = vehicle;
    }

    @Override
    public Location getLocation() {
        return BukkitUtil.toLocation(vehicle.getLocation());
    }

    @Override
    public void teleport(Location location) {
        vehicle.teleport(BukkitUtil.toLocation(location));
    }
}
