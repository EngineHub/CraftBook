package com.sk89q.craftbook.mech;
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

import com.sk89q.craftbook.access.BlockEntity;
import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.SignInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

/**
 * A sign oriented mechanism.
 *
 * @author sk89q
 */
public abstract class SignOrientedMechanism {

    /**
     * Sign position.
     */
    protected Vector pt;
    /**
     * Sign text.
     */
    protected SignInterface signText;

    protected ServerInterface server;
    protected WorldInterface world;

    protected int x, y, z;

    /**
     * Construct the object.
     *
     * @param pt
     */
    public SignOrientedMechanism(ServerInterface s, WorldInterface w, Vector pt) {

        x = pt.getBlockX();
        y = pt.getBlockY();
        z = pt.getBlockZ();

        BlockEntity blockEntity =
                w.getBlockEntity(x, y, z);
        if (blockEntity instanceof SignInterface)
            signText = (SignInterface) blockEntity;
        else throw new IllegalArgumentException("block not sign");
        this.pt = pt;

        server = s;
        world = w;
    }

    /**
     * Construct the object.
     *
     * @param pt
     */
    public SignOrientedMechanism(ServerInterface s, WorldInterface w, Vector pt, SignInterface signText) {

        this.signText = signText;

        server = s;
        world = w;
    }

    /**
     * Get sign text.
     *
     * @return
     */
    public SignText getSignText() {

        return signText;
    }

    /**
     * Get the mechanism identifier. This is the second line of the sign
     * (i.e. [Bridge]) and this will return the square brackets as well.
     *
     * @return
     */
    public String getSignIdentifier() {

        return signText.getLine2();
    }
}
