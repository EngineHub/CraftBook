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

package com.sk89q.craftbook;

import java.util.UUID;

import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;

/**
 * Holds an abstraction for players.
 *
 */
public interface LocalPlayer {

    public void print(String message);

    public void printError(String message);

    public void printRaw(String message);

    public void checkPermission(String perm) throws InsufficientPermissionsException;

    public boolean hasPermission(String perm);

    public String getName();

    public UUID getUniqueId();

    public String getCraftBookId();

    public Location getPosition();

    public void setPosition(Vector pos, float pitch, float yaw);

    public void teleport(Location location);

    public boolean isSneaking();

    public void setSneaking(boolean state);

    public boolean isInsideVehicle();

    @Deprecated
    public int getHeldItemType();

    @Deprecated
    public short getHeldItemData();

    public ItemInfo getHeldItemInfo();

    public boolean isHoldingBlock();

    public String translate(String message);
}
