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
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * This allows users to Right-click to check the power level of redstone.
 */
public class Ammeter extends AbstractMechanic {

    protected final CraftBookPlugin plugin = CraftBookPlugin.inst();

    public Ammeter() {

        super();
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());

        Block block = event.getClickedBlock();
        if (event.getPlayer().getItemInHand().getTypeId() == plugin.getConfiguration().ammeterItem
                && (BlockType.canTransferRedstone(block.getTypeId()) || BlockType.isRedstoneSource(block.getTypeId())
                        )) {
            int data = getSpecialData(block);
            String line = getCurrentLine(data);
            player.print("Ammeter: " + line + ChatColor.WHITE + " " + data + " A");
        }
    }

    private int getSpecialData(Block block) {

        int typeId = block.getTypeId();
        byte data = block.getData();
        int current = 0;
        switch (typeId) {
            case BlockID.REDSTONE_WIRE:
                current = data;
                break;
            case BlockID.LEVER:
            case BlockID.STONE_BUTTON:
                if ((data & 0x8) == 0x8) {
                    current = 15;
                }
                break;
            case BlockID.STONE_PRESSURE_PLATE:
                if ((data & 0x1) == 0x1) {
                    current = 15;
                }
                break;
            case BlockID.POWERED_RAIL:
            case BlockID.DETECTOR_RAIL:
                if (data >= 0x8) {
                    current = 15;
                }
                break;
            case BlockID.REDSTONE_TORCH_ON:
            case BlockID.REDSTONE_REPEATER_ON:
                current = 15;
                break;
            default:
                current = 0;
                break;

        }

        return current;
    }

    private String getCurrentLine(int data) {

        StringBuilder line = new StringBuilder(25);
        line.append(ChatColor.YELLOW).append("[");
        if (data > 10) {
            line.append(ChatColor.DARK_GREEN);
        } else if (data > 5) {
            line.append(ChatColor.GOLD);
        } else if (data > 0) {
            line.append(ChatColor.DARK_RED);
        }
        for (int i = 0; i < data; i++) {
            line.append("|");
        }
        line.append(ChatColor.BLACK);
        for (int i = data; i < 15; i++) {
            line.append("|");
        }
        line.append(ChatColor.YELLOW).append("]");
        return line.toString();
    }

    public static class Factory extends AbstractMechanicFactory<Ammeter> {

        @Override
        public Ammeter detect(BlockWorldVector pt, LocalPlayer player) {

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (BlockType.canTransferRedstone(block.getTypeId()) || BlockType.isRedstoneSource(block.getTypeId())) {
                return player.getHeldItemType() == CraftBookPlugin.inst().getConfiguration().ammeterItem && player.hasPermission("craftbook.mech.ammeter.use") ? new Ammeter() : null;
            }

            return null;
        }
    }
}