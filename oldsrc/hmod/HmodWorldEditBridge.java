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

import com.sk89q.craftbook.access.LocalWorldEditBridgeException;
import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.WorldEditInterface;
import com.sk89q.craftbook.util.Vector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEditNotInstalled;

/**
 * Used to get WorldEdit information.
 *
 * @author sk89q
 */
public class HmodWorldEditBridge implements WorldEditInterface {

    /**
     * Get the minimum point.
     *
     * @param player
     *
     * @return
     */
    public Vector getRegionMinimumPoint(PlayerInterface player)
            throws LocalWorldEditBridgeException {

        try {
            return fromWE(WorldEditBridge.getRegionMinimumPoint(toHmodPlayer(player)));
        } catch (IncompleteRegionException e) {
            throw new LocalWorldEditBridgeException(e);
        } catch (WorldEditNotInstalled e) {
            throw new LocalWorldEditBridgeException(e);
        }
    }

    /**
     * Get the maximum point.
     *
     * @param player
     *
     * @return
     */
    public Vector getRegionMaximumPoint(PlayerInterface player)
            throws LocalWorldEditBridgeException {

        try {
            return fromWE(WorldEditBridge.getRegionMaximumPoint(toHmodPlayer(player)));
        } catch (IncompleteRegionException e) {
            throw new LocalWorldEditBridgeException(e);
        } catch (WorldEditNotInstalled e) {
            throw new LocalWorldEditBridgeException(e);
        }
    }

    /**
     * Convert a WorldEdit vector to a CraftBook vector.
     *
     * @param vec
     *
     * @return
     */
    private static Vector fromWE(com.sk89q.worldedit.Vector vec) {

        return new Vector(vec.getX(), vec.getY(), vec.getZ());
    }

    //Ugllyyyy~
    public static Player toHmodPlayer(PlayerInterface p) {

        return ((HmodPlayerImpl) p).player;
    }
}
