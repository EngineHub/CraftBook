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

import com.sk89q.worldedit.BlockWorldVector;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.*;
import org.bukkit.block.Block;
import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.AbstractMechanicFactory;

/**
* This allows users to Right-click to check the light level.
*/
public class LightStone extends AbstractMechanic {

    protected MechanismsPlugin plugin;

    public LightStone (MechanismsPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.wrap(event.getPlayer()).hasPermission("craftbook.mech.lightstone.use")) {
            return;
        }

        Block block = event.getClickedBlock();
        if (event.getPlayer().getItemInHand().getType() == Material.GLOWSTONE_DUST) {
            int data = getLightLevel(block);
            String line = getLightLevel(data);
            int light = Integer.valueOf(data);
            event.getPlayer().sendMessage(
                    ChatColor.YELLOW + "LightStone: " + line + ChatColor.WHITE +
                    " " + light + " A");
        }
    }
    
    private int getLightLevel(Block block) {
        int light = block.getLightLevel();
        return light;
    }
    
    private String getLightLevel(int data) {
        String line = ChatColor.YELLOW + "[";
        if (data > 10) {
            line = line + ChatColor.DARK_GREEN;
        } else if (data > 5) {
            line = line + ChatColor.GOLD;
        } else if (data > 0) {
            line = line + ChatColor.DARK_RED;
        }
        for (int i = 0; i < data; i++) {
            line = line + "|";
        }
        line = line + ChatColor.BLACK;
        for (int i = data; i < 15; i++) {
            line = line + "|";
        }
        line = line + ChatColor.YELLOW + "]";
        return line;
    }

    public void unload() {
    }

    public boolean isActive() {
        return false; // this isn't a persistent mechanic, so the manager will
        // never keep it around long enough to even check this.
    }


    public static class Factory extends AbstractMechanicFactory<LightStone> {

        protected MechanismsPlugin plugin;

        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
         }

        public LightStone detect(BlockWorldVector bwv){
            return new LightStone(plugin);
        }
    }
}
