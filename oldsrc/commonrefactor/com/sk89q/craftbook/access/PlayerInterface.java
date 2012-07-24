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

package com.sk89q.craftbook.access;

/**
 * @author sk89q
 */
public interface PlayerInterface extends LivingEntityInterface {

    /**
     * Get the name of the player.
     *
     * @return String
     */
    public String getName();

    /**
     * Gives the player an item.
     *
     * @param type
     * @param amt
     */
    public void giveItem(int type, int amt);

    /**
     * Print a message.
     *
     * @param msg
     */
    public void printRaw(String msg);

    /**
     * Print a WorldEdit message.
     *
     * @param msg
     */
    public void print(String msg);

    /**
     * Print a WorldEdit error.
     *
     * @param msg
     */
    public void printError(String msg);

    public boolean canUseCommand(String permission);

    public boolean canUseObject(String object);

    public boolean canCreateObject(String object);

    public boolean canCreateIC(String icId);

    public void sendMessage(String string);

    public boolean isInGroup(String group);

    public int getItemInHand();
}
