/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.Component;

/**
 * Holds an abstraction for players.
 */
public interface CraftBookPlayer extends Player {

    String getCraftBookId();

    void teleport(Location location); // TODO Add to WorldEdit

    boolean isSneaking(); // TODO Add to WorldEdit

    boolean isInsideVehicle(); // TODO Add to WorldEdit

    boolean isHoldingBlock(); // TODO Add to WorldEdit

    @Deprecated
    String translate(String message);

    /**
     * Print a {@link Component} to the action bar.
     *
     * @param component The component to print
     */
    void printActionBar(Component component);
}
