package com.sk89q.craftbook.util;

import org.bukkit.Location;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

@SuppressWarnings("unused")
public class SearchArea {

    private Location center = null;
    private Vector radius = null;
    private ProtectedRegion region = null;
    //TODO use this class to cleanup the entire system of Radius, Offsets and search areas. This can also be used to extend the current system to broader search systems.
}