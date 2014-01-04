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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;

public class BukkitPlayer implements LocalPlayer {

    protected final CraftBookPlugin plugin;
    protected final Player player;

    public Player getPlayer() {

        return player;
    }

    public BukkitPlayer(CraftBookPlugin plugin, Player player) {

        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void print(String message) {

        player.sendMessage(ChatColor.GOLD + ChatColor.translateAlternateColorCodes('&', plugin.getLanguageManager().getString(message, plugin.getLanguageManager().getPlayersLanguage(player))));
    }

    @Override
    public void printError(String message) {

        player.sendMessage(ChatColor.RED + ChatColor.translateAlternateColorCodes('&', plugin.getLanguageManager().getString(message, plugin.getLanguageManager().getPlayersLanguage(player))));
    }

    @Override
    public void printRaw(String message) {

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLanguageManager().getString(message, plugin.getLanguageManager().getPlayersLanguage(player))));
    }

    @Override
    public boolean hasPermission(String perm) {

        return plugin.hasPermission(player, perm);
    }

    @Override
    public void checkPermission(String perm) throws InsufficientPermissionsException {

        if (!hasPermission(perm)) throw new InsufficientPermissionsException();
    }

    @Override
    public String getName() {

        return player.getName();
    }

    @Override
    public Location getPosition() {

        return BukkitUtil.toLocation(player.getLocation());
    }

    @Override
    public void teleport(Location location) {

        player.teleport(BukkitUtil.toLocation(location));
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {

        player.teleport(new org.bukkit.Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ(), yaw, pitch));
    }

    @Override
    public boolean isInsideVehicle() {

        return player.isInsideVehicle();
    }

    @Override
    public int getHeldItemType() {

        if (player.getItemInHand() == null) return 0;
        return player.getItemInHand().getTypeId();
    }

    @Override
    public boolean isHoldingBlock() {

        if(getHeldItemType() == 0) return false;
        return BlockType.fromID(getHeldItemType()) != null;
    }

    @Override
    public String translate(String message) {

        return plugin.getLanguageManager().getString(message, plugin.getLanguageManager().getPlayersLanguage(player));
    }

    @Override
    public short getHeldItemData () {

        if (player.getItemInHand() == null) return 0;
        return player.getItemInHand().getDurability();
    }

    @Override
    public ItemInfo getHeldItemInfo () {
        return new ItemInfo(Material.getMaterial(getHeldItemType()), getHeldItemData());
    }

    @Override
    public boolean isSneaking () {
        return player.isSneaking();
    }

    @Override
    public void setSneaking (boolean state) {
        player.setSneaking(state);
    }
}