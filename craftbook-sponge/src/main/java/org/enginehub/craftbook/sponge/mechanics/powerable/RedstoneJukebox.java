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
package org.enginehub.craftbook.sponge.mechanics.powerable;

import com.me4502.modularframework.module.Module;
import org.enginehub.craftbook.core.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.util.data.CraftBookKeys;
import org.enginehub.craftbook.sponge.util.data.mutable.LastPowerData;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Jukebox;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Module(id = "redstonejukebox", name = "RedstoneJukebox", onEnable="onInitialize", onDisable="onDisable")
public class RedstoneJukebox extends SimplePowerable implements DocumentationProvider {

    @Override
    public String getPath() {
        return "mechanics/jukebox";
    }

    @Override
    public boolean isValid(Location<World> location) {
        return location.getBlockType() == BlockTypes.JUKEBOX;
    }

    @Override
    public void updateState(Location<?> location, boolean powered) {
        Jukebox jukebox = (Jukebox) location.getTileEntity().get();
        jukebox.offer(new LastPowerData(powered ? 15 : 0));

        if (powered) {
            jukebox.playRecord();
        } else {
            jukebox.stopRecord();
        }
    }

    @Override
    public boolean getState(Location<?> location) {
        return location.get(CraftBookKeys.LAST_POWER).orElse(0) > 0;
    }
}
