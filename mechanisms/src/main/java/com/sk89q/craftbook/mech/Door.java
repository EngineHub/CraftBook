package com.sk89q.craftbook.mech;

// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.MechanismsConfiguration;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.SignUtil;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Door.
 *
 * @author turtle9598
 */
public class Door extends PersistentMechanic {

    public static class Factory extends AbstractMechanicFactory<Door> {
	public Factory(MechanismsPlugin plugin) {
	    this.plugin = plugin;
	}

	private MechanismsPlugin plugin;     

	/**
	 * Detect the mechanic at a placed sign.
	 * 
	 * @throws ProcessedMechanismException 
	 */
	@Override
	public Door detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
		throws InvalidMechanismException, ProcessedMechanismException {
	    if (sign.getLine(1).equalsIgnoreCase("[Door Down]")) {
		if (!player.hasPermission("craftbook.mech.door")) {
		    throw new InsufficientPermissionsException();
		}

		sign.setLine(1, "[Door Down]");
		player.print("Door created.");
	    } else if (sign.getLine(1).equalsIgnoreCase("[Door Up]")) {
		if (!player.hasPermission("craftbook.mech.door")) {
		    throw new InsufficientPermissionsException();
		}

		sign.setLine(1, "[Door Up]");
		player.print("Door created.");
	    } else if (sign.getLine(1).equalsIgnoreCase("[Door]")) {
		if (!player.hasPermission("craftbook.mech.door")) {
		    throw new InsufficientPermissionsException();
		}

		sign.setLine(1, "[Door]");
		player.print("Door created.");
	    } else {
		return null;
	    }

	    throw new ProcessedMechanismException();
	}

	/**
	 * Explore around the trigger to find a Door; throw if things look funny.
	 * 
	 * @param pt the trigger (should be a signpost)
	 * @return a Door if we could make a valid one, or null if this looked
	 *         nothing like a door.
	 * @throws InvalidMechanismException
	 *             if the area looked like it was intended to be a door, but
	 *             it failed.
	 */
	@Override
	public Door detect(BlockWorldVector pt) throws InvalidMechanismException {
	    Block block = BukkitUtil.toBlock(pt);
	    // check if this looks at all like something we're interested in first
	    if (block.getTypeId() != BlockID.SIGN_POST) return null;
	    if (!((Sign)block.getState()).getLine(1).contains("Door") || ((Sign)block.getState()).getLine(1).equalsIgnoreCase("[Door]")) return null;

	    // okay, now we can start doing exploration of surrounding blocks
	    // and if something goes wrong in here then we throw fits.
	    return new Door(block, plugin);
	}
    }

    /**
     * @param trigger
     *            if you didn't already check if this is a signpost with appropriate
     *            text, you're going on Santa's naughty list.
     * @param plugin
     * @throws InvalidMechanismException
     */
    private Door(Block trigger, MechanismsPlugin plugin) throws InvalidMechanismException {
	super();

	// check and set some properties
	if (!SignUtil.isCardinal(trigger)) throw new InvalidDirectionException();

	this.trigger = trigger;
	this.plugin = plugin;
	this.settings = plugin.getLocalConfiguration().doorSettings; 

	Sign s = (Sign)trigger.getState();

	Material mat;
	findBase: {
	    if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Up]")) {
		proximalBaseCenter = trigger.getRelative(BlockFace.UP);
	    } else if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Down]")) {
		proximalBaseCenter = trigger.getRelative(BlockFace.DOWN);
	    }    
	    mat = proximalBaseCenter.getType();
	    if (settings.canUseBlock(mat)) {
		if ((proximalBaseCenter.getRelative(SignUtil.getLeft(trigger)).getType() == mat
			&& proximalBaseCenter.getRelative(SignUtil.getRight(trigger)).getType() == mat)
			|| s.getLine(2).equalsIgnoreCase("1"))
		    break findBase;
		throw new InvalidConstructionException("Blocks adjacent to the door block must be of the same type.");
	    } else {
		throw new UnacceptableMaterialException();
	    }
	}
	// Find the other side
	if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Up]")) {
	    otherSide = trigger.getRelative(BlockFace.UP);
	} else if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Down]")) {
	    otherSide = trigger.getRelative(BlockFace.DOWN);
	}
	for (int i = 0; i <= settings.maxLength; i++) {
	    // about the loop index:
	    // i = 0 is the first block after the proximal base
	    // since we're allowed to have settings.maxLength toggle blocks,
	    // i = settings.maxLength is actually the farthest place we're 
	    // allowed to find the distal signpost

	    if (otherSide.getType() == Material.SIGN_POST) {
		String otherSignText = ((Sign)otherSide.getState()).getLines()[1];
		if ("[Door Down]".equalsIgnoreCase(otherSignText)) break;
		if ("[Door Up]".equalsIgnoreCase(otherSignText)) break;
		if ("[Door]".equalsIgnoreCase(otherSignText)) break;
	    }

	    if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Up]")) {
		otherSide = otherSide.getRelative(BlockFace.UP);
	    } else if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Down]")) {
		otherSide = otherSide.getRelative(BlockFace.DOWN);
	    }    
	}

	if (otherSide.getType() != Material.SIGN_POST)
	    throw new InvalidConstructionException("Door sign required on other side (or it was too far away).");
	// Check the other side's base blocks for matching type
	Block distalBaseCenter = null;

	if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Up]")) {
	    distalBaseCenter = otherSide.getRelative(BlockFace.DOWN);
	} else if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Down]")) {
	    distalBaseCenter = otherSide.getRelative(BlockFace.UP);
	}

	if ((distalBaseCenter.getType() != mat && distalBaseCenter.getData() != proximalBaseCenter.getData())
		|| ((distalBaseCenter.getRelative(SignUtil.getLeft(trigger)).getType() != mat && distalBaseCenter.getRelative(SignUtil.getLeft(trigger)).getData() != proximalBaseCenter.getData())
			|| (distalBaseCenter.getRelative(SignUtil.getRight(trigger)).getType() != mat && distalBaseCenter.getRelative(SignUtil.getRight(trigger)).getData() != proximalBaseCenter.getData())) 
			&& (s.getLine(2).equalsIgnoreCase("1") && ((Sign) otherSide.getState()).getLine(2).equalsIgnoreCase("1")))
	    throw new InvalidConstructionException("The other side must be made with the same blocks.");

	// Select the togglable region

	toggle = new CuboidRegion(BukkitUtil.toVector(proximalBaseCenter),BukkitUtil.toVector(distalBaseCenter));
	if(!s.getLine(2).equalsIgnoreCase("1"))
	    toggle.expand(BukkitUtil.toVector(SignUtil.getLeft(trigger)), 
		    BukkitUtil.toVector(SignUtil.getRight(trigger)));
	toggle.contract(BukkitUtil.toVector(BlockFace.UP), BukkitUtil.toVector(BlockFace.DOWN));
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
	if (!plugin.getLocalConfiguration().doorSettings.enable) return;

	if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger))) 
	    return; 

	BukkitPlayer player = new BukkitPlayer(plugin, event.getPlayer());
	if ( !player.hasPermission("craftbook.mech.door.use")) {
	    player.printError("You don't have permission to use doors.");
	    return;
	}

	flipState();

	event.setCancelled(true);
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
	if (!plugin.getLocalConfiguration().doorSettings.enableRedstone) return;

	if (!BukkitUtil.toWorldVector(event.getBlock()).equals(BukkitUtil.toWorldVector(trigger))) return;
	if (event.getNewCurrent() == event.getOldCurrent()) return;

	if (event.getNewCurrent() == 0) {
	    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ToggleRegionOpen(), 2);
	} else {
	    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ToggleRegionClosed(), 2);
	}
    }

    private void flipState() {
	// this is kinda funky, but we only check one position 
	// to see if the door is open and/or closable.
	// efficiency choice :/
	Block hinge = null;

	if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Up]")) {
	    hinge = proximalBaseCenter.getRelative(BlockFace.UP);
	} else {
	    hinge = proximalBaseCenter.getRelative(BlockFace.DOWN);
	}

	// aaand we also only check if it's something we can 
	// smosh or not when deciding if we're open or closed.
	// there are no errors reported upon weird blocks like 
	// obsidian in the middle of a wooden door, just weird
	// results.
	if (canPassThrough(hinge.getTypeId())) {
	    new ToggleRegionClosed().run();
	} else {
	    new ToggleRegionOpen().run();
	}
    }

    private class ToggleRegionOpen implements Runnable {
	public void run() {
	    for (com.sk89q.worldedit.BlockVector bv : toggle) {     // this package specification is something that needs to be fixed in the overall scheme
		Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
		if (b.getType() == getDoorMaterial() || canPassThrough(b.getTypeId()))
		    b.setType(Material.AIR);
	    }
	}
    }
    private class ToggleRegionClosed implements Runnable {
	public void run() {
	    for (com.sk89q.worldedit.BlockVector bv : toggle) {     // this package specification is something that needs to be fixed in the overall scheme
		Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
		if (canPassThrough(b.getTypeId())) {
		    b.setType(getDoorMaterial());
		    b.setData(getDoorData());
		}
	    }
	}
    }

    @Override
    public void unload() {
	/* we're not persistent */
    }

    @Override
    public boolean isActive() {
	/* we're not persistent */
	return false;
    }

    private Material getDoorMaterial() {
	return proximalBaseCenter.getType();
    }

    private byte getDoorData() {
	return proximalBaseCenter.getData();
    }


    private MechanismsPlugin plugin;
    private MechanismsConfiguration.DoorSettings settings;

    /** The signpost we came from. */
    private Block trigger;
    /** The block that determines door type. */
    private Block proximalBaseCenter;
    /** The signpost on the other end. */
    private Block otherSide;
    /** The rectangle that we toggle. */
    private CuboidRegion toggle;
    // we don't store anything about the blocks on the ends because 
    // we never poke them; just check that they're sane when we're building
    // the door.  if this were a PersistentMechanic, those six blocks
    // would be considered defining blocks, though.

    /**
     * @return whether the door can pass through this BlockType (and displace it
     *         if needed).
     */
    private static boolean canPassThrough(int t) {
	if (t != BlockID.WATER
		&& t != BlockID.STATIONARY_WATER
		&& t != BlockID.LAVA
		&& t != BlockID.STATIONARY_LAVA
		&& t != BlockID.FENCE
		&& t != BlockID.SNOW
		&& t != BlockID.LONG_GRASS
		&& t != BlockID.VINE
		&& t != BlockID.DEAD_BUSH
		&& t != 0) {
	    return false;
	} else {
	    return true;
	}
    }

    /**
     * Thrown when the sign is an invalid direction.
     */
    private static class InvalidDirectionException extends InvalidMechanismException {
	private static final long serialVersionUID = -3183606604247616362L;
    }

    /**
     * Thrown when the door type is unacceptable.
     */
    private static class UnacceptableMaterialException extends InvalidMechanismException {
	private static final long serialVersionUID = 8340723004466483212L;
    }

    /**
     * Thrown when the door type is not constructed correctly.
     */
    private static class InvalidConstructionException extends InvalidMechanismException {
	private static final long serialVersionUID = 4943494589521864491L;

	/**
	 * Construct the object.
	 * 
	 * @param msg
	 */
	public InvalidConstructionException(String msg) {
	    super(msg);
	}
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
	if(event.getBlock().getState() instanceof Sign) 
	    new ToggleRegionOpen().run();
	else
	    event.setCancelled(true);
    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {
	List<BlockWorldVector> bwv = new ArrayList<BlockWorldVector>();
	Iterator<BlockVector> it = toggle.iterator();
	while(it.hasNext())
	{
	    BlockVector vec = it.next();
	    if(vec == null) continue;
	    bwv.add(BukkitUtil.toWorldVector(trigger.getWorld().getBlockAt(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ())));
	}
	return bwv;
    }

    @Override
    public void onWatchBlockNotification(BlockEvent evt) {
	if(evt instanceof BlockBreakEvent)
	    if(!(evt.getBlock().getState() instanceof Sign))
		((BlockBreakEvent) evt).setCancelled(true);
	    else
		new ToggleRegionOpen().run();
    }
}