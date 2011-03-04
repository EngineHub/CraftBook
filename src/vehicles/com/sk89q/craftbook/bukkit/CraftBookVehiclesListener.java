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

package com.sk89q.craftbook.bukkit;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;


import com.sk89q.craftbook.VehiclesConfiguration;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;

public class CraftBookVehiclesListener extends VehicleListener {

	protected VehiclesPlugin plugin;

	public CraftBookVehiclesListener(VehiclesPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Called when a vehicle is created.
	 */
	@Override
	public void onVehicleCreate(VehicleCreateEvent event) {
		Vehicle vehicle = event.getVehicle();

		// Only working with minecarts
		if (!(vehicle instanceof Minecart)) {
			return;
		}

		VehiclesConfiguration config = plugin.getLocalConfiguration();

		Minecart minecart = (Minecart) vehicle;

		minecart.setSlowWhenEmpty(config.minecartSlowWhenEmpty);
		minecart.setMaxSpeed(minecart.getMaxSpeed() * config.minecartMaxSpeedModifier);
	}

	/**
	 * Called when an vehicle moves.
	 */
	@Override
	public void onVehicleMove(VehicleMoveEvent event) {
		Vehicle vehicle = event.getVehicle();

		// Only working with minecarts
		if (!(vehicle instanceof Minecart)) {
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();

		if (from.getBlockX() != to.getBlockX()
				|| from.getBlockY() != to.getBlockY()
				|| from.getBlockZ() != to.getBlockZ()) {
			handleMinecartBlockChange(event);
		}
	}

	protected void handleMinecartBlockChange(VehicleMoveEvent event) {
		Minecart minecart = (Minecart) event.getVehicle();
		Location to = event.getTo();
		Location current = minecart.getLocation();

		Block under = to.getBlock().getRelative(0, -1, 0);
		int underType = under.getTypeId();

		VehiclesConfiguration config = plugin.getLocalConfiguration();

		if (underType == config.maxBoostBlock) {
			minecart.setVelocity(minecart.getVelocity().normalize().multiply(100));
		} else if (underType == config.boost25xBlock) {
			minecart.setVelocity(minecart.getVelocity().multiply(1.25));
		} else if (underType == config.slow20xBlock) {
			minecart.setVelocity(minecart.getVelocity().multiply(0.8));
		} else if (underType == config.slow50xBlock) {
			minecart.setVelocity(minecart.getVelocity().multiply(0.5));
		} else if (underType == config.reverseBlock) {
			minecart.setVelocity((minecart.getVelocity().multiply(-1)));
		} else if (underType == config.stationBlock) {
			//TODO
		} else if (underType == config.sortBlock) {
			Block test = under.getRelative(0, -1, 0);
			if(test != null && test.getType() == Material.SIGN_POST) {
				Sign sign = (Sign) test.getState();
				if((sign).getLine(1).equalsIgnoreCase("[Sort]")) {
					String dir = "";
					if(isValidSortSign((sign).getLine(2), minecart)) {dir = "Left";}
					else if(isValidSortSign((sign).getLine(3), minecart)) { dir = "Right";}
					
					int signData = minecart.getWorld().getBlockAt(test.getLocation()).getData();
					int trackData = 0;
					Vector targetTrack = null;
					
					if (SignUtil.getFacing(test) == BlockFace.WEST) { // West
						if (dir.equals("Left")) {
							trackData = 9;
						} else if (dir.equals("Right")) {
							trackData = 8;
						} else {
							trackData = 0;
						}
						targetTrack = new Vector(current.getBlockX(), current.getBlockY(), current.getBlockZ() + 1);
					} else if (SignUtil.getFacing(test) == BlockFace.EAST) { // East
						if (dir.equals("Left")) {
							trackData = 7;
						} else if (dir.equals("Right")) {
							trackData = 6;
						} else {
							trackData = 0;
						}
						targetTrack = new Vector(current.getBlockX(), current.getBlockY(), current.getBlockZ() - 1);
					} else if (SignUtil.getFacing(test) == BlockFace.NORTH) { // North
						if (dir.equals("Left")) {
							trackData = 6;
						} else if (dir.equals("Right")) {
							trackData = 9;
						} else {
							trackData = 1;
						}
						targetTrack = new Vector(current.getBlockX() - 1, current.getBlockY(), current.getBlockZ());
					} else if (SignUtil.getFacing(test) == BlockFace.SOUTH) { // South
						if (dir.equals("Left")) {
							trackData = 8;
						} else if (dir.equals("Right")) {
							trackData = 7;
						} else {
							trackData = 1;
						}
						targetTrack = new Vector(current.getBlockX() + 1, current.getBlockY(), current.getBlockZ());
					}
					if (targetTrack != null
							&& minecart.getWorld().getBlockTypeIdAt(targetTrack.getBlockX(), targetTrack.getBlockY(), targetTrack.getBlockZ()) == BlockID.MINECART_TRACKS) {
						minecart.getWorld().getBlockAt(targetTrack.getBlockX(), targetTrack.getBlockY(), targetTrack.getBlockZ()).setData((byte) trackData);
					}
				}
			}
		}
	}
	public boolean isValidSortSign(String line, Minecart minecart) {
		Entity test = minecart.getPassenger();
		Player player = null;
		if(test instanceof Player)
			player = (Player) test;
		if (line.equalsIgnoreCase("All")) {
			return true;
		}

		if ((line.equalsIgnoreCase("Unoccupied")
				|| line.equalsIgnoreCase("Empty"))
				&& minecart.getPassenger() == null) {
			return true;
		}

		if (line.equalsIgnoreCase("Storage")
				&& minecart instanceof StorageMinecart) {
			return true;
		}
		else if (line.equalsIgnoreCase("Powered")
				&& minecart instanceof PoweredMinecart) {
			return true;
		}
		else if (line.equalsIgnoreCase("Minecart")
				&& minecart instanceof Minecart) {
			return true;
		}

		if ((line.equalsIgnoreCase("Occupied")
				|| line.equalsIgnoreCase("Full"))
				&& minecart.getPassenger() != null) {
			return true;
		}

		if (line.equalsIgnoreCase("Animal")
				&& test instanceof Animals) {
			return true;
		}

		if (line.equalsIgnoreCase("Mob")
				&& test instanceof Monster) {
			return true;
		}

		if ((line.equalsIgnoreCase("Player")
				|| line.equalsIgnoreCase("Ply"))
				&& player != null) {
			return true;
		}

		String[] parts = line.split(":");

		if (parts.length >= 2) {
			if (player != null && parts[0].equalsIgnoreCase("Held")) {
				try {
					int item = Integer.parseInt(parts[1]);
					if (player.getItemInHand().getTypeId() == item) {
						return true;
					}
				} catch (NumberFormatException e) {
				}
			} else if (player != null && parts[0].equalsIgnoreCase("Ply")) {
				if (parts[1].equalsIgnoreCase(player.getName())) {
					return true;
				}
			} else if (parts[0].equalsIgnoreCase("Mob")) {
				String testMob = parts[1];
				test.toString().toLowerCase().equalsIgnoreCase(testMob);
			}
		}

		return false;
	}
}
