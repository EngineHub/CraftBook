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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.HistoryHashMap;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Handler for Light switches. Toggles all torches in the area from being redstone to normal torches. This is done
 * every time a sign with [|] or [I]
 * is right clicked by a player.
 *
 * @author fullwall
 */
public class LightSwitch extends AbstractCraftBookMechanic {

    /**
     * Store a list of recent light toggles to prevent spamming. Someone clever can just use two signs though.
     */
    private static final HistoryHashMap<Location, Long> recentLightToggles = new HistoryHashMap<Location, Long>(20);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!event.getLine(1).equalsIgnoreCase("[i]") && !event.getLine(1).equalsIgnoreCase("[|]")) return;
        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.light-switch")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("You don't have permission for this.");
            SignUtil.cancelSign(event);
            return;
        }

        event.setLine(1, "[I]");
        lplayer.print("mech.lightswitch.create");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ChangedSign sign = event.getSign();
        if(!sign.getLine(1).equalsIgnoreCase("[I]") && sign.getLine(1).equalsIgnoreCase("[|]")) return;
        LocalPlayer player = event.getWrappedPlayer();
        if (!player.hasPermission("craftbook.mech.light-switch.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        toggleLights(event.getClickedBlock(), player);
        event.setCancelled(true);
    }

    /**
     * Toggle lights in the immediate area.
     *
     * @param block
     *
     * @return true if the block was recogized as a lightswitch; this may or may not mean that any lights were
     *         actually toggled.
     */
    private boolean toggleLights(Block block, LocalPlayer player) {

        // check if this looks at all like something we're interested in first
        if (block.getTypeId() != BlockID.WALL_SIGN) return false;
        int radius = Math.min(10, CraftBookPlugin.inst().getConfiguration().lightSwitchMaxRange);
        int maximum = Math.min(CraftBookPlugin.inst().getConfiguration().lightSwitchMaxLights, 20);
        ChangedSign sign = BukkitUtil.toChangedSign(block);
        try {
            radius = Math.min(Integer.parseInt(sign.getLine(2)), CraftBookPlugin.inst().getConfiguration().lightSwitchMaxRange);
        } catch (Exception ignored) {
        }
        try {
            maximum = Math.min(Integer.parseInt(sign.getLine(3)), CraftBookPlugin.inst().getConfiguration().lightSwitchMaxLights);
        } catch (Exception ignored) {
        }

        int wx = block.getX();
        int wy = block.getY();
        int wz = block.getZ();
        int aboveID = block.getRelative(0, 1, 0).getTypeId();

        if (aboveID == BlockID.TORCH || aboveID == BlockID.REDSTONE_TORCH_OFF || aboveID == BlockID.REDSTONE_TORCH_ON) {
            // Check if block above is a redstone torch.
            // Used to get what to change torches to.
            boolean on = aboveID != BlockID.TORCH;
            // Prevent spam
            Long lastUse = recentLightToggles.remove(block.getLocation());
            long currTime = System.currentTimeMillis();

            if (lastUse != null && currTime - lastUse < 500) {
                recentLightToggles.put(block.getLocation(), lastUse);
                return true;
            }

            recentLightToggles.put(block.getLocation(), currTime);
            int changed = 0;
            for (int x = -radius + wx; x <= radius + wx; x++) {
                for (int y = -radius + wy; y <= radius + wy; y++) {
                    for (int z = -radius + wz; z <= radius + wz; z++) {
                        int id = block.getWorld().getBlockTypeIdAt(x, y, z);
                        if (id == BlockID.TORCH || id == BlockID.REDSTONE_TORCH_OFF || id == BlockID.REDSTONE_TORCH_ON) {
                            // Limit the maximum number of changed lights
                            if (changed >= maximum) return true;

                            if (on) {
                                block.getWorld().getBlockAt(x, y, z).setTypeId(BlockID.TORCH);
                            } else {
                                block.getWorld().getBlockAt(x, y, z).setTypeId(BlockID.REDSTONE_TORCH_ON);
                            }
                            changed++;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
}