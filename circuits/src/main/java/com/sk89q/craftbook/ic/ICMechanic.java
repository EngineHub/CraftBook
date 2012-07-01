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

package com.sk89q.craftbook.ic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * Mechanic wrapper for ICs. The mechanic manager dispatches events to this
 * mechanic, and then it is processed and passed onto the associated IC.
 * 
 * @author sk89q
 */
public class ICMechanic extends PersistentMechanic {

    protected CircuitsPlugin plugin;
    protected String id;
    protected ICFamily family;
    protected IC ic;

    public ICMechanic(CircuitsPlugin plugin, String id, IC ic,
            ICFamily family, BlockWorldVector pos) {
        super(pos);
        this.plugin = plugin;
        this.id = id;
        this.ic = ic;
        this.family = family;
    }

    @Override
    public void onBlockRedstoneChange(final SourcedBlockRedstoneEvent event) {
        BlockWorldVector pt = getTriggerPositions().get(0);
        Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));

        if (block.getTypeId() == BlockID.WALL_SIGN) {
            final BlockState state = block.getState();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // Assuming that the plugin host isn't going wonky here
                    ChipState chipState = family.detect(
                            BukkitUtil.toWorldVector(event.getSource()), (Sign) state);
                    ic.trigger(chipState);
                }
            };
            //FIXME: these should be registered with a global scheduler so we can end up with one runnable actually running per set of inputs in a given time window.
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                    plugin, runnable, 2);
        }
    }

    @Override
    public void unload() {
        ic.unload();
        //FIXME plugin.getServer().getScheduler().cancelTasks(plugin);
    }

    @Override
    public boolean isActive() {
        BlockWorldVector pt = getTriggerPositions().get(0);
        Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));

        if (block.getTypeId() == BlockID.WALL_SIGN) {
            BlockState state = block.getState();

            if (state instanceof Sign) {
                Sign sign = (Sign) state;

                Matcher matcher = ICMechanicFactory.codePattern.matcher(sign.getLine(1));

                if (!matcher.matches()) {
                    return false;
                } else if (!matcher.group(1).equalsIgnoreCase(id)) {
                    return false;
                } else if (ic instanceof PersistentIC) {
                    return ((PersistentIC) ic).isActive();
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {
        // this seems a little strange; you'd think you'd be watching the input blocks, right?
        // nope.  redstone events get reported to blocks adjacent to the redstone,
        // so we don't have to do that for any single-block IC.
        return new ArrayList<BlockWorldVector>();
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

}
