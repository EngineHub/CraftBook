package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class PlanterST extends Planter implements SelfTriggeredIC{

    public PlanterST(Server server, Sign block) {
	super(server, block);
    }

    @Override
    public boolean isActive() {
	return true;
    }
    
    @Override
    public String getTitle() {
	return "Self-Triggered Planter";
    }

    @Override
    public String getSignTitle() {
	return "PLANTER ST";
    }


    @Override
    public void think(ChipState state) {
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
    
    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new PlanterST(getServer(), sign);
        }
    }
}
