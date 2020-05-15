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
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.HistoryHashMap;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Handler for Light switches. Toggles all torches in the area from being redstone to normal torches. This is done
 * every time a sign with [|] or [I]
 * is right clicked by a player.
 *
 * @author fullwall
 */
public class LightSwitch extends AbstractCraftBookMechanic {

    @Override
    public boolean enable() {

        recentLightToggles = new HistoryHashMap<>(20);
        return true;
    }

    /**
     * Store a list of recent light toggles to prevent spamming. Someone clever can just use two signs though.
     */
    private HistoryHashMap<Location, Long> recentLightToggles;

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[i]") && !event.getLine(1).equalsIgnoreCase("[|]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.light-switch")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("You don't have permission for this.");
            SignUtil.cancelSign(event);
            return;
        }

        event.setLine(1, "[I]");
        lplayer.print("mech.lightswitch.create");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ChangedSign sign = event.getSign();
        if(!sign.getLine(1).equalsIgnoreCase("[I]") && !sign.getLine(1).equalsIgnoreCase("[|]")) return;

        if (!EventUtil.passesFilter(event))
            return;

        CraftBookPlayer player = event.getWrappedPlayer();
        if (!player.hasPermission("craftbook.mech.light-switch.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
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
    private boolean toggleLights(Block block, CraftBookPlayer player) {

        // check if this looks at all like something we're interested in first
        if (!SignUtil.isSign(block)) return false;
        int radius = Math.min(10, maxRange);
        int maximum = Math.min(maxLights, 20);
        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(block);
        try {
            radius = Math.min(Integer.parseInt(sign.getLine(2)), maxRange);
        } catch (Exception ignored) {
        }
        try {
            maximum = Math.min(Integer.parseInt(sign.getLine(3)), maxLights);
        } catch (Exception ignored) {
        }

        int wx = block.getX();
        int wy = block.getY();
        int wz = block.getZ();
        Material aboveID = block.getRelative(0, 1, 0).getType();

        if (aboveID == Material.WALL_TORCH || aboveID == Material.REDSTONE_WALL_TORCH) {
            // Check if block above is a redstone torch.
            // Used to get what to change torches to.
            boolean on = aboveID != Material.WALL_TORCH;
            // Prevent spam
            Long lastUse = recentLightToggles.remove(block.getLocation());
            long currTime = System.currentTimeMillis();

            if (lastUse != null && currTime - lastUse < 100) {
                recentLightToggles.put(block.getLocation(), lastUse);
                return true;
            }

            recentLightToggles.put(block.getLocation(), currTime);
            int changed = 0;
            for (int x = -radius + wx; x <= radius + wx; x++) {
                for (int y = -radius + wy; y <= radius + wy; y++) {
                    for (int z = -radius + wz; z <= radius + wz; z++) {
                        Block relBlock = block.getWorld().getBlockAt(x, y, z);
                        Material id = relBlock.getType();
                        if (id == Material.TORCH || id == Material.WALL_TORCH || id == Material.REDSTONE_TORCH || id == Material.REDSTONE_WALL_TORCH) {
                            // Limit the maximum number of changed lights
                            if (changed >= maximum) return true;

                            if (id == Material.WALL_TORCH || id == Material.REDSTONE_WALL_TORCH) {
                                Directional currentData = (Directional) relBlock.getBlockData();

                                Directional directional = (Directional) (on ? Material.WALL_TORCH : Material.REDSTONE_WALL_TORCH).createBlockData();
                                directional.setFacing(currentData.getFacing());
                                relBlock.setBlockData(directional, false);
                            } else {
                                relBlock.setType(on ? Material.TORCH : Material.REDSTONE_TORCH, false);
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

    private int maxRange;
    private int maxLights;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "max-range", "The maximum range that the mechanic searches for lights in.");
        maxRange = config.getInt(path + "max-range", 10);

        config.setComment(path + "max-lights", "The maximum amount of lights that a Light Switch can toggle per usage.");
        maxLights = config.getInt(path + "max-lights", 20);
    }
}