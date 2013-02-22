// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.circuits.ic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Mechanic wrapper for ICs. The mechanic manager dispatches events to this mechanic,
 * and then it is processed and passed onto the associated IC.
 *
 * @author sk89q
 */
public class ICMechanic extends PersistentMechanic {

    protected final String id;
    protected final ICFamily family;
    protected final IC ic;
    protected final BlockWorldVector pos;

    public ICMechanic(String id, IC ic, ICFamily family, BlockWorldVector pos) {

        super(pos);
        this.id = id;
        this.ic = ic;
        this.family = family;
        this.pos = pos;
    }

    @Override
    public void onBlockRedstoneChange(final SourcedBlockRedstoneEvent event) {

        BlockWorldVector pt = getTriggerPositions().get(0);
        final Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
        // abort if the current did not change
        if (event.getNewCurrent() == event.getOldCurrent()) return;

        if (block.getTypeId() == BlockID.WALL_SIGN) {
            final Block source = event.getSource();
            // abort if the sign is the source or the block the sign is attached to
            if (SignUtil.getBackBlock(block).equals(source) || block.equals(source)) return;

            Runnable runnable = new Runnable() {

                @Override
                public void run() {

                    if (block.getTypeId() != BlockID.WALL_SIGN) return;
                    ChipState chipState = family.detect(BukkitUtil.toWorldVector(source),
                            BukkitUtil.toChangedSign(block));
                    int cnt = 0;
                    for (int i = 0; i < chipState.getInputCount(); i++) {
                        if (chipState.isTriggered(i)) {
                            cnt++;
                        }
                    }
                    if (cnt > 0) {
                        ic.trigger(chipState);
                    }
                }
            };
            // FIXME: these should be registered with a global scheduler so we can end up with one runnable actually
            // running per set of inputs in a given time window.
            CraftBookPlugin.server().getScheduler().scheduleSyncDelayedTask(CraftBookPlugin.inst(), runnable, 2);
        }
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        ic.onRightClick(event.getPlayer());
    }

    @Override
    public void unload() {

        ic.unload();
    }

    @Override
    public boolean isActive() {

        BlockWorldVector pt = getTriggerPositions().get(0);
        Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));

        if (block.getTypeId() == BlockID.WALL_SIGN) {
            ChangedSign sign = BukkitUtil.toChangedSign(block);

            Matcher matcher = RegexUtil.IC_PATTERN.matcher(sign.getLine(1));

            return matcher.matches() && matcher.group(1).equalsIgnoreCase(id) && ic instanceof PersistentIC && ((PersistentIC) ic).isActive();
        }

        return false;
    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {
        // this seems a little strange; you'd think you'd be watching the input blocks, right?
        // nope. redstone events get reported to blocks adjacent to the redstone,
        // so we don't have to do that for any single-block IC.
        return new ArrayList<BlockWorldVector>();
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        // remove the ic from cache
        ICManager.removeCachedIC(pos);
    }

    public IC getIC() {

        return ic;
    }
}