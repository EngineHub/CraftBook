package com.sk89q.craftbook.gates.world;

import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.SignUtil;

public class SentryGun extends AbstractIC{

    public SentryGun(Server server, Sign block) {
	super(server, block);
    }

    @Override
    public String getTitle() {
	return "Sentry Gun";
    }

    @Override
    public String getSignTitle() {
	return "SENTRY GUN";
    }

    @Override
    public void trigger(ChipState chip) {
	shoot();
    }

    public void shoot() {
	Type type = Type.fromString(getSign().getLine(2));
	int radius = 10;
	Location location = getSign().getBlock().getLocation().add(0, 1, 0);
	// add the offset to the location of the block connected to the sign
	for (Chunk chunk : getSurroundingChunks(location, radius)) {
	    if (chunk.isLoaded()) {
		// get all entites from the chunks in the defined radius
		for (Entity entity : chunk.getEntities()) {
		    if (!entity.isDead()) {
			if (type.is(entity)) {
			    // at last check if the entity is within the radius
			    if (getGreatestDistance(entity.getLocation(), location) <= radius) {
				Block signBlock = getSign().getBlock();
				BlockFace face = SignUtil.getBack(signBlock);
				Block targetDir =  signBlock.getRelative(face).getRelative(face);
				location.getWorld().spawnArrow(targetDir.getLocation(), entity.getLocation().subtract(targetDir.getLocation()).add(0.5,0.5,0.5).toVector(), 2.0f, 0.0f);
				break;
			    }
			}
		    }
		}
	    }
	}
    }

    private Set<Chunk> getSurroundingChunks(Location loc, int radius) {
	Set<Chunk> chunks = new LinkedHashSet<Chunk>();
	Chunk chunk = loc.getChunk();
	chunks.add(chunk);
	// get the block the furthest away
	loc.add(radius, 0, radius);
	// add the chunk
	Chunk chunk2 = loc.getChunk();
	chunks.add(loc.getChunk());
	// get the x, z difference between the two chunks then...
	int z = 0;
	// ...iterate over all chunks in between
	for (int x = chunk.getX() - chunk2.getX(); x > 0; x--) {
	    // add all surrounding chunks one by one
	    chunks.add(chunk.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z));
	    for (z = chunk.getZ() - chunk2.getZ(); z > 0; z--) {
		chunks.add(chunk.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z));
	    }
	}
	return chunks;
    }

    public static int getGreatestDistance(Location l1, Location l2) {
	int x = Math.abs(l1.getBlockX() - l2.getBlockX());
	int y = Math.abs(l1.getBlockY() - l2.getBlockY());
	int z = Math.abs(l1.getBlockZ() - l2.getBlockZ());
	if (x > y && x > z) {
	    return x;
	} else if (y > x && y > z) {
	    return y;
	} else if (z > x && z > y) {
	    return z;
	} else {
	    return x;
	}
    }

    private enum Type {
	PLAYER,
	MOBHOSTILE,
	MOBPEACEFUL,
	ANYMOB;

	public boolean is(Entity entity) {

	    switch (this) {
	    case PLAYER:
		return entity instanceof Player;
	    case MOBHOSTILE:
		return entity instanceof Monster;
	    case MOBPEACEFUL:
		return entity instanceof Animals;
	    case ANYMOB:
		return entity instanceof Creature;
	    default:
		return entity instanceof Monster;
	    }
	}

	public static Type fromString(String name) {
	    return EnumUtil.getEnumFromString(SentryGun.Type.class, name);
	}
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

	public Factory(Server server) {
	    super(server);
	}

	@Override
	public IC create(Sign sign) {
	    return new SentryGun(getServer(), sign);
	}
    }
}
