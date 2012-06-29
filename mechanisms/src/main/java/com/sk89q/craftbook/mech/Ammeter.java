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

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * This allows users to Right-click to check the power level of redstone.
 */
public class Ammeter extends AbstractMechanic {

    protected MechanismsPlugin plugin;

    public Ammeter(MechanismsPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.wrap(event.getPlayer()).hasPermission("craftbook.mech.ammeter.use")) {
            return;
        }

        Block block = event.getClickedBlock();
        if (event.getPlayer().getItemInHand().getType() == Material.COAL
                && (BlockType.canTransferRedstone(block.getTypeId()) ||
                        BlockType.isRedstoneSource(block.getTypeId()))) {
            int data = getSpecialData(block);
            String line = getCurrentLine(data);
            int current = Integer.valueOf(data);
            event.getPlayer().sendMessage(
                    ChatColor.YELLOW + "Ammeter: " + line + ChatColor.WHITE +
                    " " + current + " A");
        }
    }

    private int getSpecialData(Block block) {
        Material type = block.getType();
        byte data = block.getData();
        int current = 0;
        if (type == Material.LEVER || type == Material.STONE_BUTTON) {
            if ((data & 0x8) == 0x8) {
                current = 15;
            }
        } else if (type == Material.STONE_PLATE || type == Material.STONE_PLATE) {
            if ((data & 0x1) == 0x1) {
                current = 15;
            }
        } else if (type == Material.POWERED_RAIL || type == Material.DETECTOR_RAIL) {
            if (data >= 0x8) {
                current = 15;
            }
        } else if (type == Material.REDSTONE_TORCH_ON) {
            current = 15;
        } else if (type == Material.REDSTONE_TORCH_OFF) {
            current = 0;
        } else if (type == Material.REDSTONE_WIRE) {
            current = data;
        } else if (type == Material.DIODE_BLOCK_ON) {
            current = 15;
        }
        
        return current;
    }

    private String getCurrentLine(int data) {
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


    public static class Factory extends AbstractMechanicFactory<Ammeter> {

        protected MechanismsPlugin plugin;

        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }

        public Ammeter detect(BlockWorldVector pt) {
            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (BlockType.canTransferRedstone(block.getTypeId()) ||
                    BlockType.isRedstoneSource(block.getTypeId())) {
                return new Ammeter(plugin);
            }

            return null;
        }
    }


	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		
	}
}
