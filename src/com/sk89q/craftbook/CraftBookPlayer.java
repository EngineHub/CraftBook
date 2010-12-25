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

package com.sk89q.craftbook;

/**
 *
 * @author sk89q
 */
public abstract class CraftBookPlayer {
    /**
     * Move the player.
     *
     * @param pos
     */
    public void setPosition(Vector pos) {
        setPosition(pos, (float)getPitch(), (float)getYaw());
    }

    /**
     * Returns true if equal.
     *
     * @param other
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CraftBookPlayer)) {
            return false;
        }
        CraftBookPlayer other2 = (CraftBookPlayer)other;
        return other2.getName().equals(getName());
    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Get the point of the block that is being stood in.
     *
     * @return point
     */
    public abstract Vector getBlockIn();

    /**
     * Get the point of the block that is being stood upon.
     *
     * @return point
     */
    public abstract Vector getBlockOn();

    /**
     * Get the name of the player.
     *
     * @return String
     */
    public abstract String getName();

    /**
     * Get the player's view pitch.
     *
     * @return pitch
     */
    public abstract double getPitch();

    /**
     * Get the player's position.
     *
     * @return point
     */
    public abstract Vector getPosition();

    /**
     * Get the player's view yaw.
     *
     * @return yaw
     */
    public abstract double getYaw();

    /**
     * Gives the player an item.
     *
     * @param type
     * @param amt
     */
    public abstract void giveItem(int type, int amt);

    /**
     * Print a message.
     *
     * @param msg
     */
    public abstract void printRaw(String msg);

    /**
     * Print a WorldEdit message.
     *
     * @param msg
     */
    public abstract void print(String msg);

    /**
     * Print a WorldEdit error.
     *
     * @param msg
     */
    public abstract void printError(String msg);

    /**
     * Move the player.
     *
     * @param pos
     * @param pitch
     * @param yaw
     */
    public abstract void setPosition(Vector pos, float pitch, float yaw);
    
    /**
     * Check if a user has a permission.
     * 
     * @param permission
     * @return
     */
    public abstract boolean hasPermission(String permission);
}
