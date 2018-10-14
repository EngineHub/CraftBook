// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.bukkit;

import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.core.LanguageManager;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.item.ItemType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class BukkitCraftBookPlayer extends BukkitPlayer implements CraftBookPlayer {

    protected final CraftBookPlugin plugin;
    protected final Player player;

    public BukkitCraftBookPlayer(CraftBookPlugin plugin, Player player) {
        super(CraftBookPlugin.plugins.getWorldEdit(), player);

        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void print(String message) {
        player.sendMessage(ChatColor.GOLD + ChatColor.translateAlternateColorCodes('&', translate(message)));
    }

    @Override
    public void printError(String message) {
        player.sendMessage(ChatColor.RED + ChatColor.translateAlternateColorCodes('&', translate(message)));
    }

    @Override
    public void printRaw(String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', translate(message)));
    }

    @Override
    public boolean hasPermission(String perm) {

        return plugin.hasPermission(player, perm);
    }

    @Override
    public void teleport(Location location) {

        player.teleport(CraftBookBukkitUtil.toLocation(location));
    }

    @Override
    public boolean isInsideVehicle() {

        return player.isInsideVehicle();
    }

    @Override
    public boolean isHoldingBlock() {
        ItemType mainitem = getItemInHand(HandSide.MAIN_HAND).getType();
        ItemType offitem = getItemInHand(HandSide.OFF_HAND).getType();
        return (mainitem.hasBlockType() && !mainitem.getBlockType().getMaterial().isAir())
                || (offitem.hasBlockType() && !offitem.getBlockType().getMaterial().isAir());
    }

    @Override
    public String translate(String message) {

        return plugin.getLanguageManager().getString(message, LanguageManager.getPlayersLanguage(player));
    }

    @Override
    public boolean isSneaking () {
        return player.isSneaking();
    }

    @Override
    public String getCraftBookId() {
        return CraftBookPlugin.inst().getUUIDMappings().getCBID(getUniqueId());
    }
}