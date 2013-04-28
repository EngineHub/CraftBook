// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.mech;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * This allows users to Right-click to check the light level.
 */
public class LightStone extends AbstractMechanic {

    public LightStone() {

        super();
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        Block block = event.getClickedBlock().getRelative(event.getBlockFace());
        if (event.getPlayer().getItemInHand().getTypeId() == CraftBookPlugin.inst().getConfiguration().lightstoneItem) {
            String lightLevelLine = getLightLine(block.getLightLevel());
            player.print(ChatColor.YELLOW + "LightStone: [" + lightLevelLine + ChatColor.YELLOW + "] " + block
                    .getLightLevel() + " L");
        }
    }

    private String getLightLine(int data) {

        StringBuilder line = new StringBuilder(25);
        if (data >= 9) {
            line.append(ChatColor.GREEN);
        } else {
            line.append(ChatColor.DARK_RED);
        }
        for (int i = 0; i < data; i++) {
            line.append("|");
        }
        line.append(ChatColor.BLACK);
        for (int i = data; i < 15; i++) {
            line.append("|");
        }
        return line.toString();
    }

    public static class Factory extends AbstractMechanicFactory<LightStone> {

        @Override
        public LightStone detect (BlockWorldVector pt, LocalPlayer player) throws InvalidMechanismException {

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (block != null && player.getHeldItemType() == CraftBookPlugin.inst().getConfiguration().lightstoneItem && player.hasPermission("craftbook.mech.lightstone.use")) return new LightStone();

            return null;
        }
    }
}