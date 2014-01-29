package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SearchArea {

    private Location center = null;
    private Vector radius = null;

    private ProtectedRegion region = null;
    private World world = null;

    /**
     * Creates an invalid SearchArea that can not be used to search.
     */
    private SearchArea() {
    }

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
    private SearchArea(ProtectedRegion region, World world) {
        this.region = region;
        this.world = world;
    }

    public static SearchArea createEmptyArea() {

        return new SearchArea();
    }

    /**
     * Parses a line and creates the appropriate system of parsing for this Area.
     * 
     * @param block The block to measure the offsets off. (Must be a sign)
     * @param line The line to parse
     */
    public static SearchArea createArea(Block block, String line) {

        if(line.startsWith("r:")) {

            if(CraftBookPlugin.plugins.getWorldGuard() == null)
                return new SearchArea();

            ProtectedRegion reg = CraftBookPlugin.plugins.getWorldGuard().getRegionManager(block.getWorld()).getRegion(line.replace("r:", ""));
            if(reg == null)
                return new SearchArea();

            return new SearchArea(reg, block.getWorld());
        } else {

            String[] locationParts = RegexUtil.EQUALS_PATTERN.split(line);
            Location offset = SignUtil.getBackBlock(block).getLocation();
            Vector radius = ICUtil.parseRadius(locationParts[0]);
            if(locationParts.length > 1)
                offset = ICUtil.parseBlockLocation(BukkitUtil.toChangedSign(block), locationParts[1], CraftBookPlugin.inst().getConfiguration().ICdefaultCoordinate).getLocation();

            return new SearchArea(offset, radius);
        }
    }

    public static boolean isValidArea(Block block, String line) {

        if(line.startsWith("r:")) {

            if(CraftBookPlugin.plugins.getWorldGuard() == null)
                return false;

            ProtectedRegion reg = CraftBookPlugin.plugins.getWorldGuard().getRegionManager(block.getWorld()).getRegion(line.replace("r:", ""));
            if(reg == null)
                return false;

            return true;
        } else {

            String[] locationParts = RegexUtil.EQUALS_PATTERN.split(line);
            SignUtil.getBackBlock(block).getLocation();
            try {
                ICUtil.parseUnsafeRadius(locationParts[0]);
                if(locationParts.length > 1)
                    ICUtil.parseUnsafeBlockLocation(locationParts[1]);

                return true;
            } catch(Exception e){
                return false;
            }
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
     * Gets a list of entities in the area that are of specific types.
     * 
     * @param types The list of types.
     * @return The entities.
     */
    public List<Entity> getEntitiesInArea(Collection<EntityType> types) {

        List<Entity> entities = new ArrayList<Entity>();

        for(Chunk chunk : getChunksInArea())
            for(Entity ent : chunk.getEntities()) {
                if(!ent.isValid() || !isWithinArea(ent.getLocation())) continue;

                boolean isType = false;
                for(EntityType type : types) {
                    if(type.is(ent)) {
                        isType = true;
                        break;
                    }
                }
                if(!isType) continue;

                entities.add(ent);
            }

        return entities;
    }

    @SuppressWarnings("serial")
    public List<Entity> getEntitiesInArea() {

        return getEntitiesInArea(new ArrayList<EntityType>(1){{add(EntityType.ANY);}});
    }

    /**
     * Check if a certain location is within the bounds of this SearchArea.
     * 
     * @param location The location to check.
     * @return If it is inside.
     */
    public boolean isWithinArea(Location location) {

        if(hasRegion()) {
            if(getRegion().contains(BukkitUtil.toVector(location)) && location.getWorld().equals(world))
                return true;
        } else if(hasRadiusAndCenter()) {
            if(LocationUtil.isWithinRadius(location, getCenter(), getRadius()))
                return true;
        } else
            return true;

        return false;
    }

    /**
     * Get a set of chunks inside this SearchArea.
     * 
     * @return the set of chunks.
     */
    public Set<Chunk> getChunksInArea() {

        Set<Chunk> chunks = new HashSet<Chunk>();

        if(hasRegion()) {

            Chunk c1 = getWorld().getChunkAt(getRegion().getMinimumPoint().getBlockX() >> 4, getRegion().getMinimumPoint().getBlockZ() >> 4);
            Chunk c2 = getWorld().getChunkAt(getRegion().getMaximumPoint().getBlockX() >> 4, getRegion().getMaximumPoint().getBlockZ() >> 4);
            int xMin = Math.min(c1.getX(), c2.getX());
            int xMax = Math.max(c1.getX(), c2.getX());
            int zMin = Math.min(c1.getZ(), c2.getZ());
            int zMax = Math.max(c1.getZ(), c2.getZ());

            for(int x = xMin; x <= xMax; x++)
                for(int z = zMin; z <= zMax; z++)
                    chunks.add(getWorld().getChunkAt(x,z));
        } else if (hasRadiusAndCenter()) {

            int chunkRadiusX = getRadius().getBlockX() < 16 ? 1 : getRadius().getBlockX() / 16;
            int chunkRadiusZ = getRadius().getBlockZ() < 16 ? 1 : getRadius().getBlockZ() / 16;
            for (int chX = 0 - chunkRadiusX; chX <= chunkRadiusX; chX++) {
                for (int chZ = 0 - chunkRadiusZ; chZ <= chunkRadiusZ; chZ++) {
                    chunks.add(new Location(getCenter().getWorld(), getCenter().getBlockX() + chX * 16, getCenter().getBlockY(), getCenter().getBlockZ() + chZ * 16).getChunk());
                }
            }
        }

        return chunks;
    }

    /**
     * Get a random block from within the area.
     * 
     * @return the block.
     */
    public Block getRandomBlockInArea() {

        int xMin=0,xMax=0,yMin=0,yMax=0,zMin=0,zMax=0;

        if(hasRegion()) {
            xMin = getRegion().getMinimumPoint().getBlockX();
            xMax = getRegion().getMaximumPoint().getBlockX();
            yMin = getRegion().getMinimumPoint().getBlockY();
            yMax = getRegion().getMaximumPoint().getBlockY();
            zMin = getRegion().getMinimumPoint().getBlockZ();
            zMax = getRegion().getMaximumPoint().getBlockZ();
        } else if(hasRadiusAndCenter()) {

            xMin = Math.min(getCenter().getBlockX() - getRadius().getBlockX(), getCenter().getBlockX() + getRadius().getBlockX());
            xMax = Math.max(getCenter().getBlockX() - getRadius().getBlockX(), getCenter().getBlockX() + getRadius().getBlockX());
            yMin = Math.min(getCenter().getBlockY() - getRadius().getBlockY(), getCenter().getBlockY() + getRadius().getBlockY());
            yMax = Math.max(getCenter().getBlockY() - getRadius().getBlockY(), getCenter().getBlockY() + getRadius().getBlockY());
            zMin = Math.min(getCenter().getBlockZ() - getRadius().getBlockZ(), getCenter().getBlockZ() + getRadius().getBlockZ());
            zMax = Math.max(getCenter().getBlockZ() - getRadius().getBlockZ(), getCenter().getBlockZ() + getRadius().getBlockZ());
        } else
            return null;

        int x = xMin + (int)(CraftBookPlugin.inst().getRandom().nextDouble() * (xMax - xMin + 1));
        int y = yMin + (int)(CraftBookPlugin.inst().getRandom().nextDouble() * (yMax - yMin + 1));
        int z = zMin + (int)(CraftBookPlugin.inst().getRandom().nextDouble() * (zMax - zMin + 1));
        Location loc = new Location(getWorld(), x, y, z);
        if(!isWithinArea(loc))
            return null;
        return loc.getBlock();
    }

    /**
     * Checks if this SearchArea is a Region type, compared to other types.
     * 
     * @return If it is a Region type.
     */
    public boolean hasRegion() {

        return region != null && world != null && CraftBookPlugin.plugins.getWorldGuard() != null;
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

    /**
     * Get the world the WorldGuard region exists in.
     * 
     * @return the world
     */
    public World getWorld() {

        if(world == null && getCenter() != null)
            return getCenter().getWorld();
        return world;
    }

    /**
     * Checks whether this SearchArea has a valid search type.
     * 
     * @return if the area has a valid search type.
     */
    public boolean isValid() {

        return hasRadiusAndCenter() || hasRegion();
    }
}