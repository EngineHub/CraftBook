// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.gates.world;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.worldedit.blocks.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class EntitySpawner extends AbstractIC {

	public EntitySpawner(Server server, Sign sign) {
		super(server, sign);
	}

	@Override
	public String getTitle() {
		return "Creature Spawner";
	}

	@Override
	public String getSignTitle() {
		return "CREATURE SPAWNER";
	}

	@Override
	public void trigger(ChipState chip) {
		if (chip.getInput(0)) {
			String type = getSign().getLine(2).trim();
			String rider = getSign().getLine(3).trim();
			if (EntityType.fromName(type) != null) {
				Location loc = getSign().getBlock().getLocation();
				int maxY = Math.min(getSign().getWorld().getMaxHeight(), loc.getBlockY() + 10);
				int x = loc.getBlockX();
				int z = loc.getBlockZ();

				for (int y = loc.getBlockY() + 1; y <= maxY; y++) {
					if (BlockType.canPassThrough(getSign().getWorld()
							.getBlockTypeIdAt(x, y, z))) {
						if (rider.length() != 0
						&& EntityType.fromName(rider) != null) {
							LivingEntity ent = getSign().getWorld()
							.spawnCreature(
									new Location(getSign().getWorld(),
											x, y, z),
											EntityType.fromName(type));
							LivingEntity ent2 = getSign().getWorld()
									.spawnCreature(
											new Location(getSign().getWorld(),
													x, y, z),
													EntityType.fromName(rider));
							ent.setPassenger(ent2);
						} else {
							getSign().getWorld()
							.spawnCreature(
									new Location(getSign().getWorld(),
											x, y, z),
											EntityType.fromName(type));
						}
						return;
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
			return new EntitySpawner(getServer(), sign);
		}
	}
}
