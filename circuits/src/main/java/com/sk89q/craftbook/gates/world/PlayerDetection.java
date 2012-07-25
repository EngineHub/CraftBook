package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;

import java.util.Set;

/**
 * @author Silthus
 */
public class PlayerDetection extends AbstractIC {

	private Location center;
	private Set<Chunk> chunks;
    private int radius;
	private String player = "";
	private String group = "";
	private boolean detectPlayer;

    public PlayerDetection(Server server, Sign block) {
        super(server, block);
        // lets set some defaults
        radius = 0;
        load();
    }

    private void load() {
        Sign sign = getSign();
	    Block block = SignUtil.getBackBlock(sign.getBlock());
	    // now check the third line for the radius and offset
	    String line = sign.getLine(2).trim();
	    boolean relativeOffset = line.contains("!") ? false : true;
	    if (!relativeOffset) line.replace("!", "");
	    // if the line contains a = the offset is given
	    // the given string should look something like that:
	    // radius=x:y:z or radius, e.g. 1=-2:5:11
	    if (line.contains("=")) {
	        try {
	            String[] split = line.split("=");
	            this.radius = Integer.parseInt(split[0]);
	            // parse the offset
	            String[] offsetSplit = split[1].split(":");
                int offsetX = Integer.parseInt(offsetSplit[0]);
                int offsetY = Integer.parseInt(offsetSplit[1]);
                int offsetZ = Integer.parseInt(offsetSplit[2]);
		        if (relativeOffset) {
			        block = LocationUtil.getRelativeOffset(sign, offsetX, offsetY, offsetZ);
		        } else {
			        block = LocationUtil.getOffset(block, offsetX, offsetY, offsetZ);
		        }
            } catch (NumberFormatException e) {
                // do nothing and use the defaults
            } catch (IndexOutOfBoundsException e) {
                // do nothing and use the defaults
            }
        } else {
            this.radius = Integer.parseInt(line);
        }
	    // parse the group or player name
	    line = sign.getLine(3).trim();
	    detectPlayer = line.contains("p:");
	    try {
		    if (detectPlayer) {
				player = line.split(":")[1];
		    } else {
			    group = line.split(":")[1];
		    }
	    } catch (Exception e) {
		    // do nothing and use the defaults
	    }
	    this.center = block.getLocation();
	    this.chunks = LocationUtil.getSurroundingChunks(block, radius);
    }

    @Override
    public String getTitle() {
        return "Player Detection";
    }

    @Override
    public String getSignTitle() {
        return "P-DETECTION";
    }

    @Override
    public void trigger(ChipState chip) {
        if (chip.getInput(0)) {
            chip.setOutput(0, isDetected());
        }
    }

    protected boolean isDetected() {
        for (Chunk chunk : this.chunks) {
            if (chunk.isLoaded()) {
                // get all entites from the chunks in the defined radius
                for (Entity entity : chunk.getEntities()) {
                    if (!entity.isDead()) {
                        if (entity instanceof Player) {
	                        // at last check if the entity is within the radius
	                        if (LocationUtil.getGreatestDistance(entity.getLocation(), center) <= radius) {
		                        if (detectPlayer) {
			                        return ((Player) entity).getName().equals(player);
		                        } else {
			                        return CircuitsPlugin.getInst().isInGroup(((Player) entity).getName(), group);
		                        }
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
	        return new PlayerDetection(getServer(), sign);
        }
    }
}
