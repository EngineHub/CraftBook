package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;

public class SentryGun extends AbstractIC {

	/**
	 * @author Me4502
	 */
	private enum Type {
		PLAYER,
		MOB_HOSTILE,
		MOB_PEACEFUL,
		MOB_ANY;

		public boolean is(Entity entity) {

			switch (this) {
				case PLAYER:
					return entity instanceof Player;
				case MOB_HOSTILE:
					return entity instanceof Monster;
				case MOB_PEACEFUL:
					return entity instanceof Animals;
				case MOB_ANY:
					return entity instanceof Creature;
				default:
					return entity instanceof Monster;
			}
		}

		public static Type fromString(String name) {

			return EnumUtil.getEnumFromString(SentryGun.Type.class, name);
		}
	}

	private Type type;
	private Block center;
	private int radius = 10;

    public SentryGun(Server server, Sign block) {
        super(server, block);
	    load();
    }

	private void load() {
		type = Type.fromString(getSign().getLine(2));
		center = SignUtil.getBackBlock(getSign().getBlock());
		radius = Integer.parseInt(getSign().getLine(3));
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

        // add the offset to the location of the block connected to the sign
        for (Chunk chunk : LocationUtil.getSurroundingChunks(center, radius)) {
            if (chunk.isLoaded()) {
                // get all entites from the chunks in the defined radius
                for (Entity entity : chunk.getEntities()) {
                    if (!entity.isDead()) {
                        if (type.is(entity)) {
                            // at last check if the entity is within the radius
                            if (LocationUtil.getGreatestDistance(entity.getLocation(), center.getLocation()) <= radius) {
                                Block signBlock = getSign().getBlock();
                                BlockFace face = SignUtil.getBack(signBlock);
                                Block targetDir = signBlock.getRelative(face).getRelative(face);
                                chunk.getWorld().spawnArrow(targetDir.getLocation(),
                                        entity.getLocation().subtract(targetDir.getLocation()).add(0.5, 0.5,
                                                0.5).toVector(), 2.0f, 0.0f);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new SentryGun(getServer(), sign);
        }

	    @Override
	    public void verify(Sign sign) throws ICVerificationException {
		    try {
			    String line = sign.getLine(3);
			    if (line != null && !line.contains("")) {
				    Integer.parseInt(line);
			    }
		    } catch (Exception e) {
			    throw new ICVerificationException("You need to give a radius in line four.");
		    }
	    }
    }
}
