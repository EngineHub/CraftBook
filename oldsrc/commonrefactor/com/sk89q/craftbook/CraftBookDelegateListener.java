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

import com.sk89q.craftbook.access.*;
import com.sk89q.craftbook.util.Vector;

/**
 * Proxy plugin listener called by CraftBook. It has additional hooks
 * that are called by the main CraftBook listener, namely the Redstone
 * input hook.
 *
 * @author sk89q
 */
public abstract class CraftBookDelegateListener {

    /**
     * CraftBook.
     */
    protected CraftBookCore craftBook;

    /**
     * Server interface
     */
    protected ServerInterface server;

    public CraftBookDelegateListener() {

    }

    /**
     * Construct the object.
     *
     * @param craftBook
     * @param listener
     */
    public CraftBookDelegateListener(
            CraftBookCore craftBook,
            ServerInterface server) {

        this.craftBook = craftBook;
        this.server = server;
    }

    /**
     * Reads the configuration from the properties file.
     */
    public abstract void loadConfiguration();

    /**
     * Called on plugin unload.
     */
    public void disable() {

    }

    public void onTick(WorldInterface world) {

    }

    public void onSignCreate(WorldInterface world, int x, int y, int z) {

    }

    public boolean onSignChange(PlayerInterface i, WorldInterface world, Vector v, SignInterface s) {

        return false;
    }

    public boolean onCommand(PlayerInterface player, String[] split) {

        return false;
    }

    public boolean onConsoleCommand(String[] split) {

        return false;
    }

    public void onWireInput(WorldInterface world, Vector pt, boolean isOn, Vector changed) {

    }

    public void onDisconnect(PlayerInterface player) {

    }

    public boolean onBlockPlace(WorldInterface world, PlayerInterface p, Vector blockPlaced,
                                Vector blockClicked, int itemInHand) {

        return false;
    }

    public void onBlockRightClicked(WorldInterface world, PlayerInterface p, Vector block,
                                    int itemInHand) {

    }

    public boolean onBlockDestroy(WorldInterface world, PlayerInterface p, Vector block, int status) {

        return false;
    }

    public void onMinecartPositionChange(WorldInterface world, MinecartInterface cart, int x, int y, int z) {

    }

    public void onMinecartVelocityChange(WorldInterface world, MinecartInterface cart) {

    }

    public boolean onMinecartDamage(WorldInterface world, MinecartInterface cart,
                                    BaseEntityInterface attacker, int damage) {

        return false;
    }

    public void onMinecartEnter(WorldInterface world, MinecartInterface cart,
                                BaseEntityInterface entity, boolean entering) {

    }

    public void onMinecartDestroyed(WorldInterface world, MinecartInterface cart) {

    }

    public void onWorldLoad(WorldInterface world) {

    }

    public void onWorldUnload(WorldInterface world) {

    }
}
