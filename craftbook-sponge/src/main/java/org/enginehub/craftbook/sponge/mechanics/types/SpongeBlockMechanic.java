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
package org.enginehub.craftbook.sponge.mechanics.types;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public abstract class SpongeBlockMechanic extends SpongeMechanic {

    /**
     * Gets whether the chosen block is an instance of this mechanic.
     * <p>
     * Whilst this can be used internally by the mechanic, that may not be the most efficient thing to do.
     * </p>
     * 
     * @param location The location to check at.
     * @return If the block is an instance of this mechanic
     */
    public abstract boolean isValid(Location<World> location);
}
