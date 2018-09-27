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

package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * This allows users to Right-click to check the power level of redstone.
 */
public class Ammeter extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;

        if (!EventUtil.passesFilter(event)) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(player.getItemInHand(HandSide.MAIN_HAND).getType() != item) return;
        if(!player.hasPermission("craftbook.mech.ammeter.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        Block block = event.getClickedBlock();
        int data = getSpecialData(block);
        if (data >= 0) {
            String line = getCurrentLine(data);
            player.print(player.translate("mech.ammeter.ammeter") + ": " + line + ChatColor.WHITE + ' ' + data + " A");
            event.setCancelled(true);
        }
    }

    private static int getSpecialData(Block block) {

        BlockData blockData = block.getBlockData();
        int current = -1;
        if (blockData instanceof Powerable) {
            current = ((Powerable) blockData).isPowered() ? 15 : 0;
        } else if (blockData instanceof AnaloguePowerable) {
            current = ((AnaloguePowerable) blockData).getPower();
        } else if (blockData instanceof Lightable) {
            current = ((Lightable) blockData).isLit() ? 15 : 0;
        } else if (block.getType() == Material.REDSTONE_BLOCK) {
            current = 15;
        }

        return current;
    }

    private static String getCurrentLine(int data) {

        StringBuilder line = new StringBuilder(25);
        line.append(ChatColor.YELLOW).append('[');
        if (data > 10)
            line.append(ChatColor.DARK_GREEN);
        else if (data > 5)
            line.append(ChatColor.GOLD);
        else if (data > 0)
            line.append(ChatColor.DARK_RED);
        for (int i = 0; i < data; i++)
            line.append('|');
        line.append(ChatColor.BLACK);
        for (int i = data; i < 15; i++)
            line.append('|');
        line.append(ChatColor.YELLOW).append(']');
        return line.toString();
    }

    private ItemType item;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "item", "Set the item that is the ammeter tool.");
        item = BukkitAdapter.asItemType(ItemSyntax.getItem(config.getString(path + "item", ItemTypes.COAL.getId())).getType());
    }
}