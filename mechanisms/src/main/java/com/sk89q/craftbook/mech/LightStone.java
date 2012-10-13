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

package com.sk89q.craftbook.mech;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * This allows users to Right-click to check the light level.
 */
public class LightStone extends AbstractMechanic {

    protected final MechanismsPlugin plugin;

    public LightStone(MechanismsPlugin plugin) {

        super();
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        LocalPlayer player = plugin.wrap(event.getPlayer());

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!player.hasPermission("craftbook.mech.lightstone.use")) return;

        Block block = event.getClickedBlock().getRelative(event.getBlockFace());
        if (event.getPlayer().getItemInHand().getTypeId() == plugin.getLocalConfiguration().lightStoneSettings.id) {
            String lightLevelLine = getLightLine(block.getLightLevel());
            player.print(
                    ChatColor.YELLOW + "LightStone: [" + lightLevelLine
                    + ChatColor.YELLOW + "] " + block.getLightLevel() + " L");
        }
    }

    private String getLightLine(int data) {

        String line = "";
        if (data >= 9) {
            line = line + ChatColor.GREEN;
        }
        else {
            line = line + ChatColor.DARK_RED;
        }
        for (int i = 0; i < data; i++) {
            line = line + "|";
        }
        line = line + ChatColor.BLACK;
        for (int i = data; i < 15; i++) {
            line = line + "|";
        }
        return line;
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isActive() {

        return false; // this isn't a persistent mechanic, so the manager will
        // never keep it around long enough to even check this.
    }

    public static class Factory extends AbstractMechanicFactory<LightStone> {

        protected final MechanismsPlugin plugin;

        public Factory(MechanismsPlugin plugin) {

            this.plugin = plugin;
        }

        @Override
        public LightStone detect(BlockWorldVector pt) {

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (block != null) return new LightStone(plugin);

            return null;
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {

    }
}