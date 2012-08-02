package com.sk89q.craftbook.gates.world;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;

/**
 * @author Silthus
 */
public class EntitySensor extends AbstractIC {

    private enum Type {
	PLAYER,
	ITEM,
	MOB_HOSTILE,
	MOB_PEACEFUL,
	MOB_ANY,
	ANY,
	CART,
	CART_STORAGE,
	CART_POWERED;

	public boolean is(Entity entity) {

	    switch (this) {
	    case PLAYER:
		return entity instanceof Player;
	    case ITEM:
		return entity instanceof Item;
	    case MOB_HOSTILE:
		return entity instanceof Monster;
	    case MOB_PEACEFUL:
		return entity instanceof Animals;
	    case MOB_ANY:
		return entity instanceof Creature;
	    case CART:
		return entity instanceof Minecart;
	    case CART_STORAGE:
		return entity instanceof StorageMinecart;
	    case CART_POWERED:
		return entity instanceof PoweredMinecart;
	    case ANY:
		return true;
	    }
	    return false;
	}

	@SuppressWarnings("unused")
	public static Type fromString(String name) {

	    return EnumUtil.getEnumFromString(EntitySensor.Type.class, name);
	}
    }

    private HashSet<Type> types;

    private Location center;
    private Set<Chunk> chunks;
    private int radius;

    public EntitySensor(Server server, Sign block) {

	super(server, block);
	load();
    }

    private void load() {

	Sign sign = getSign();
	Block block = SignUtil.getBackBlock(sign.getBlock());

	// lets get the types to detect first
	types = getDetected(sign.getLine(3).trim());

	// Add all if no params are specified
	if (types.size() == 0) types.add(Type.ANY);

	sign.setLine(3, sign.getLine(3).toUpperCase());
	sign.update();

	// if the line contains a = the offset is given
	// the given string should look something like that:
	// radius=x:y:z or radius, e.g. 1=-2:5:11
	radius = ICUtil.parseRadius(getSign());
	center = ICUtil.parseBlockLocation(getSign()).getLocation();
	chunks = LocationUtil.getSurroundingChunks(block, radius);
    }

    private HashSet<Type> getDetected(String line) {

	char[] characters = line.toCharArray();

	HashSet<Type> types = new HashSet<Type>();

	for (char aCharacter : characters) {
	    switch (aCharacter) {
	    case 'p':
		types.add(Type.PLAYER);
		break;
	    case 'i':
		types.add(Type.ITEM);
		break;
	    case 'h':
		types.add(Type.MOB_HOSTILE);
		break;
	    case 'a':
		types.add(Type.MOB_PEACEFUL);
		break;
	    case 'm':
		types.add(Type.MOB_ANY);
		break;
	    case 'l':
		types.add(Type.ANY);
		break;
	    case 'c':
		types.add(Type.CART);
		break;
	    case 's':
		types.add(Type.CART_STORAGE);
		break;
	    case 'e':
		types.add(Type.CART_POWERED);
		break;
	    default:
		break;
	    }
	}

	return types;
    }

    @Override
    public String getTitle() {

	return "Entity Sensor";
    }

    @Override
    public String getSignTitle() {

	return "ENTITY SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

	if (chip.getInput(0)) {
	    chip.setOutput(0, isDetected());
	}
    }

    protected boolean isDetected() {

	for (Chunk chunk : chunks) {
	    if (chunk.isLoaded()) {
		// Get all entites from the chunks in the defined radius
		for (Entity entity : chunk.getEntities()) {
		    if (!entity.isDead() && entity.isValid()) {
			for (Type type : types) {
			    // Check Type
			    if (type.is(entity)) {
				// Check Radius
				if (LocationUtil.getGreatestDistance(entity.getLocation(), center) <= radius) {
				    return true;
				}
				break;
			    }
			}
		    }
		}
	    }
	}
	return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

	public Factory(Server server) {

	    super(server);
	}

	@Override
	public IC create(Sign sign) {

	    return new EntitySensor(getServer(), sign);
	}

	@Override
	public void verify(Sign sign) throws ICVerificationException {

	    ICUtil.verifySignSyntax(sign);
	}
    }
}
