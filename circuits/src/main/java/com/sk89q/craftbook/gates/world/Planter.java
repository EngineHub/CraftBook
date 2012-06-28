package com.sk89q.craftbook.gates.world;

import java.util.Collection;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
* Sapling planter
* 
* Hybrid variant of MCX206 and MCX203 chest collector
* 	
* When there is a sapling or seed item drop in range it will auto plant it above the IC.
* 
* @author Drathus	
*
*/
public class Planter extends AbstractIC{

    public Planter(Server server, Sign block) {
	super(server, block);
    }

    @Override
    public String getTitle() {
	return "Planter";
    }

    @Override
    public String getSignTitle() {
	return "PLANTER";
    }

    @Override
    public void trigger(ChipState chip) {
	World world = getSign().getWorld();
	Vector onBlock = BukkitUtil.toVector(SignUtil.getBackBlock(
		getSign().getBlock()).getLocation());
	Vector target = null;
	int[] info = null;
	int yOffset = 0;

	if (getSign().getLine(2).length() != 0) {
	    String[] lineParts = getSign().getLine(2).split(":");
	    info = new int[] { Integer.parseInt(lineParts[0]),
		    Integer.parseInt(lineParts[1]) };
	}

	if (info == null || !plantableItem(info[0])) {
	    return;
	}

	try {
	    yOffset = Integer.parseInt(getSign().getLine(3));
	} catch (NumberFormatException e) {
	    return;
	}
	if (yOffset < 1) {
	    return;
	}

	target = onBlock.add(0, yOffset, 0);

	if (world.getBlockTypeIdAt(target.getBlockX(), target.getBlockY(),
		target.getBlockZ()) == 0
		&& itemPlantableOnBlock(
			info[0],
			world.getBlockTypeIdAt(target.getBlockX(),
				target.getBlockY() - 1, target.getBlockZ()))) {

	    saplingPlanter sp = new saplingPlanter(world, target, info[0],
		    info[1]);
	    sp.run();
	}
    }

    protected boolean plantableItem(int itemId) {
	boolean isPlantable = false;

	if (itemId == 6 || itemId == 295 || itemId == 372)
	    isPlantable = true;

	return isPlantable;
    }

    protected boolean itemPlantableOnBlock(int itemId, int blockId) {
	boolean isPlantable = false;

	if (itemId == 6 && (blockId == 2 || blockId == 3)) {
	    // Saplings can go on Dirt or Grass
	    isPlantable = true;
	} else if (itemId == 295 && blockId == 60) {
	    // Seeds can only go on farmland
	    isPlantable = true;
	} else if (itemId == 372 && blockId == 88) {
	    // Netherwart on soulsand
	    isPlantable = true;
	}

	return isPlantable;
    }

    protected class saplingPlanter implements Runnable {
	private final World world;
	private final Vector target;
	private final int itemId;
	private final int damVal;

	public saplingPlanter(World world, Vector target, int itemId, int damVal) {

	    this.world = world;
	    this.target = target;
	    this.itemId = itemId;
	    this.damVal = damVal;
	}

	@Override
	public void run() {

	    try {
		Collection<Item> items = world.getEntitiesByClass(Item.class);

		if (items == null)
		    return;

		for (Item itemEnt : items) {
		    if (!itemEnt.isDead()
			    && itemEnt.getItemStack().getAmount() > 0
			    && itemEnt.getItemStack().getTypeId() == this.itemId
			    && (this.damVal == -1 || (this.damVal == -1 || itemEnt
			    .getItemStack().getDurability() == this.damVal))) {
			double diffX = target.getBlockX()
				- itemEnt.getLocation().getX();
			double diffY = target.getBlockY()
				- itemEnt.getLocation().getY();
			double diffZ = target.getBlockZ()
				- itemEnt.getLocation().getZ();

			if ((diffX * diffX + diffY * diffY + diffZ * diffZ) < 6) {
			    itemEnt.remove();

			    world.getBlockAt(target.getBlockX(),
				    target.getBlockY(), target.getBlockZ())
				    .setTypeId(getBlockByItem(this.itemId));
			    world.getBlockAt(target.getBlockX(),
				    target.getBlockY(), target.getBlockZ())
				    .setData(
					    (byte) (this.damVal == -1 ? 0
						    : this.damVal));

			    break;
			}
		    }
		}
	    } catch (Exception e) {
	    }
	}

	private int getBlockByItem(int itemId) {

	    if (itemId == 295)
		return 59;
	    if (itemId == 6)
		return 6;
	    if (itemId == 372)
		return 115;

	    return 0;
	}
    }
    
    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new Planter(getServer(), sign);
        }
    }
}