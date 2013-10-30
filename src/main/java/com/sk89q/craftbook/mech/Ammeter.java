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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * This allows users to Right-click to check the power level of redstone.
 */
public class Ammeter extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(!BlockType.canTransferRedstone(event.getClickedBlock().getTypeId()) && !BlockType.isRedstoneSource(event.getClickedBlock().getTypeId())) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!CraftBookPlugin.inst().getConfiguration().ammeterItem.equals(player.getHeldItemInfo())) return;
        if(!player.hasPermission("craftbook.mech.ammeter.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        Block block = event.getClickedBlock();
        int data = getSpecialData(block);
        String line = getCurrentLine(data);
        player.print(player.translate("mech.ammeter.ammeter") + ": " + line + ChatColor.WHITE + " " + data + " A");
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
            case BlockID.WOODEN_BUTTON:
            case BlockID.POWERED_RAIL:
            case BlockID.DETECTOR_RAIL:
            case BlockID.TRIPWIRE_HOOK:
            case BlockID.ACTIVATOR_RAIL:
                if ((data & 0x8) == 0x8)
                    current = 15;
                break;
            case BlockID.STONE_PRESSURE_PLATE:
            case BlockID.WOODEN_PRESSURE_PLATE:
                if ((data & 0x1) == 0x1)
                    current = 15;
                break;
            case BlockID.REDSTONE_TORCH_ON:
            case BlockID.REDSTONE_REPEATER_ON:
            case BlockID.COMPARATOR_ON:
            case BlockID.REDSTONE_BLOCK:
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
        if (data > 10)
            line.append(ChatColor.DARK_GREEN);
        else if (data > 5)
            line.append(ChatColor.GOLD);
        else if (data > 0)
            line.append(ChatColor.DARK_RED);
        for (int i = 0; i < data; i++)
            line.append("|");
        line.append(ChatColor.BLACK);
        for (int i = data; i < 15; i++)
            line.append("|");
        line.append(ChatColor.YELLOW).append("]");
        return line.toString();
    }
}