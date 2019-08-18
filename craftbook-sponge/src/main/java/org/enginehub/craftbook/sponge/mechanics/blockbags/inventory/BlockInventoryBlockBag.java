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
package org.enginehub.craftbook.sponge.mechanics.blockbags.inventory;

import com.me4502.modularframework.module.ModuleWrapper;
import org.enginehub.craftbook.sponge.CraftBookPlugin;
import org.enginehub.craftbook.sponge.mechanics.blockbags.BlockBagManager;
import org.enginehub.craftbook.sponge.mechanics.blockbags.IdentifiableBlockBag;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class BlockInventoryBlockBag extends InventoryBlockBag implements IdentifiableBlockBag {

    public Location<World> location;

    private long id;

    public BlockInventoryBlockBag(Location<World> location) {
        super(((Carrier)location.getTileEntity().get()).getInventory());

        this.location = location;
        Optional<ModuleWrapper> moduleWrapper = CraftBookPlugin.spongeInst().moduleController.getModule("blockbag");
        if (moduleWrapper.isPresent() && moduleWrapper.get().isEnabled()) {
            BlockBagManager manager = ((BlockBagManager) moduleWrapper.get().getModule().get());
            this.id = manager.getUnusedId();
        }
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }
}
