/*
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

package org.enginehub.craftbook.mechanics.minecart.blocks.speed;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartBlockMechanism;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.RedstoneUtil;
import org.jspecify.annotations.Nullable;

public abstract class AbstractCartBooster extends CartBlockMechanism {

    public AbstractCartBooster(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected abstract @Nullable Vector getNewVelocity(Minecart minecart);

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        if (event.isMinor() || !event.getBlocks().matches(getBlock()) || RedstoneUtil.Power.OFF == isActive(event.getBlocks())) {
            return;
        }

        Vector newVelocity = getNewVelocity(event.getMinecart());
        if (newVelocity != null) {
            event.getVehicle().setVelocity(newVelocity);
        }
    }
}
