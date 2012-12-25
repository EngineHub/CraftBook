package com.sk89q.craftbook;

import com.sk89q.worldedit.Location;

public interface Vehicle {

    public Location getLocation ();

    public void teleport (Location location);
}
