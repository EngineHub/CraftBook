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

package org.enginehub.craftbook.st;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.enginehub.craftbook.internal.platform.CraftBookPlatform;

import java.util.Collection;
import java.util.HashSet;

public abstract class SelfTriggerManager {

    private CraftBookPlatform platform;

    private Collection<Location> selfTriggeringMechanics = new HashSet<>();

    public SelfTriggerManager(CraftBookPlatform platform) {
        this.platform = platform;
    }

    public void initialize() {

    }

    public void unload() {

    }

    public void think() {
        if (selfTriggeringMechanics.isEmpty()) {
            // Fast-fail
            return;
        }

        for (Location location : selfTriggeringMechanics) {
            World world = (World) location.getExtent();

        }
    }
}
