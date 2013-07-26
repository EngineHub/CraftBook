package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SearchArea {

    private Location center = null;
    private Vector radius = null;
    private ProtectedRegion region = null;

    /**
     * Creates a standard SearchArea using a radius and a center point.
     * 
     * @param center
     * @param radius
     */
    private SearchArea(Location center, Vector radius) {
        this.center = center;
        this.radius = radius;
    }

    /**
     * Creates a SearchArea using a WorldGuard region.
     * 
     * @param region
     */
    private SearchArea(ProtectedRegion region) {
        this.region = region;
    }

    /**
     * Parses a line and creates the appropriate system of parsing for this Area.
     * 
     * @param block The block to measure the offsets off. (Must be a sign)
     * @param line The line to parse
     */
    public static SearchArea createArea(Block block, String line) {

        if(line.startsWith("r:")) {

            if(CraftBookPlugin.inst().getWorldGuard() == null)
                return null;

            ProtectedRegion reg = CraftBookPlugin.inst().getWorldGuard().getRegionManager(block.getWorld()).getRegion(line.replace("r:", ""));
            if(reg == null)
                return null;

            return new SearchArea(reg);
        } else {

            String[] locationParts = RegexUtil.EQUALS_PATTERN.split(line);
            Location offset = SignUtil.getBackBlock(block).getLocation();
            Vector radius = ICUtil.parseRadius(locationParts[0]);
            if(locationParts.length > 1)
                offset = ICUtil.parseBlockLocation(BukkitUtil.toChangedSign(block), locationParts[1], CraftBookPlugin.inst().getConfiguration().ICdefaultCoordinate).getLocation();

            return new SearchArea(offset, radius);
        }
    }

    /**
     * Gets a list of all the players within this SearchArea.
     * 
     * @return The list of players.
     */
    public List<Player> getPlayersInArea() {

        List<Player> players = new ArrayList<Player>();

        for(Player player : Bukkit.getOnlinePlayers())
            if(isWithinArea(player.getLocation()))
                players.add(player);

        return players;
    }

    /**
     * Check if a certain location is within the bounds of this SearchArea.
     * 
     * @param location The location to check.
     * @return If it is inside.
     */
    public boolean isWithinArea(Location location) {

        if(hasRegion()) {
            if(getRegion().contains(BukkitUtil.toVector(location)))
                return true;
        } else if(hasRadiusAndCenter()) {
            if(LocationUtil.isWithinRadius(location, getCenter(), getRadius()))
                return true;
        }

        return false;
    }

    /**
     * Checks if this SearchArea is a Region type, compared to other types.
     * 
     * @return If it is a Region type.
     */
    public boolean hasRegion() {

        return region != null;
    }

    /**
     * Checks if this SearchArea is a Radius & Center type, compared to other types.
     * 
     * @return If it is a Radius&Center type.
     */
    public boolean hasRadiusAndCenter() {

        return radius != null && center != null;
    }

    /**
     * Get the center point of the radius.
     * 
     * @return The center point.
     */
    public Location getCenter() {

        return center;
    }

    /**
     * Get the Radius this area contains.
     * 
     * @return The radius.
     */
    public Vector getRadius() {

        return radius;
    }

    /**
     * Get the WorldGuard region that this area contains.
     * 
     * @return The region.
     */
    public ProtectedRegion getRegion() {

        return region;
    }
}