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

import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.WorldInterface;

/**
 * @author sk89q
 * @author Lymia
 */
public class HmodPlayerImpl extends HmodLivingEntityImpl
        implements PlayerInterface {

    /**
     * Stores the player.
     */
    protected Player player;

    /**
     * Construct the object.
     *
     * @param player
     */
    public HmodPlayerImpl(Player player, WorldInterface w) {

        super(player, w);
        this.player = player;
    }

    /**
     * Get the name of the player.
     *
     * @return String
     */
    @Override
    public String getName() {

        return player.getName();
    }

    /**
     * Gives the player an item.
     *
     * @param type
     * @param amt
     */
    @Override
    public void giveItem(int type, int amt) {

        player.giveItem(type, amt);
    }

    /**
     * Print a message.
     *
     * @param msg
     */
    @Override
    public void printRaw(String msg) {

        player.sendMessage(msg);
    }

    /**
     * Print a CraftBook message.
     *
     * @param msg
     */
    @Override
    public void print(String msg) {

        player.sendMessage(Colors.Gold + msg);
    }

    /**
     * Print a CraftBook error.
     *
     * @param msg
     */
    @Override
    public void printError(String msg) {

        player.sendMessage(Colors.Rose + msg);
    }

    /**
     * @return the player
     */
    public Player getPlayerObject() {

        return player;
    }

    public boolean canUseCommand(String permission) {

        return player.canUseCommand("/" + permission);
    }

    public boolean canUseObject(String object) {

        return player.canUseCommand("/" + object);
    }

    public boolean canCreateObject(String object) {

        return player.canUseCommand("/make" + object);
    }

    public boolean canCreateIC(String icId) {

        return player.canUseCommand("/" + icId) || player.canUseCommand("/allic");
    }

    public void sendMessage(String msg) {

        player.sendMessage(msg);
    }

    public boolean isInGroup(String group) {

        return player.isInGroup(group);
    }

    /**
     * Returns true if equal.
     *
     * @param other
     *
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object other) {

        if (!(other instanceof PlayerInterface)) {
            return false;
        }
        PlayerInterface other2 = (PlayerInterface) other;
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

    @Override
    public int getItemInHand() {

        return player.getItemInHand();
    }
}
