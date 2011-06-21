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

import com.sk89q.craftbook.MechanismsConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.*;

/**
 * This allows users to Right-click to check the power level of redstone.
 */
public class Ammeter extends AbstractMechanic {

    protected MechanismsPlugin plugin;

    public Ammeter(MechanismsPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.wrap(event.getPlayer()).hasPermission("craftbook.mech.ammeter.use")) {
            return;
        }

        if (event.getPlayer().getItemInHand().getType() == Material.COAL
                && event.getClickedBlock().getType() == Material.REDSTONE_WIRE) {
            String power = CalculatePowerLevel(event.getClickedBlock());
            String line = getCurrentLine(event.getClickedBlock().getData());
            event.getPlayer().sendMessage(
                    ChatColor.YELLOW
                            + "Ammeter: " + line
                            + ChatColor.WHITE + " "
                            + power + " A");
        }
    }

    private String CalculatePowerLevel(Block clickedBlock) {
        if(clickedBlock.getFace(BlockFace.NORTH).getType() == Material.REDSTONE_TORCH_ON
                && clickedBlock.getFace(BlockFace.SOUTH).getType() == Material.REDSTONE_TORCH_ON
                && clickedBlock.getFace(BlockFace.EAST).getType() == Material.REDSTONE_TORCH_ON
                && clickedBlock.getFace(BlockFace.WEST).getType() == Material.REDSTONE_TORCH_ON){
            return ChatColor.RED + "OVER 9000" + ChatColor.WHITE;
        }else{
            return "" + clickedBlock.getData();
        }
    }

    private String getCurrentLine(byte data) {
        String line = ChatColor.YELLOW + "[";
        if (data > 10)
            line = line + ChatColor.DARK_GREEN;
        else if (data > 5)
            line = line + ChatColor.GOLD;
        else if (data > 0)
            line = line + ChatColor.DARK_RED;
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


    public static class Factory extends AbstractMechanicFactory<Ammeter> {

        protected MechanismsPlugin plugin;

        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }

        public Ammeter detect(BlockWorldVector pt) {
            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (block.getTypeId() == BlockID.REDSTONE_WIRE) {
                return new Ammeter(plugin);
            }

            return null;
        }
    }
}
