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
package org.enginehub.craftbook.sponge.mechanics.minecart.block;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import org.enginehub.craftbook.util.ConfigValue;
import org.enginehub.craftbook.util.CraftBookException;
import org.enginehub.craftbook.util.PermissionNode;
import org.enginehub.craftbook.util.TernaryState;
import org.enginehub.craftbook.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.util.SignUtil;
import org.enginehub.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

@Module(id = "cartejector", name = "CartEjector", onEnable="onInitialize", onDisable="onDisable")
public class CartEjector extends SpongeCartBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.cartejector", "Allows the user to create the " + getName() + " mechanic.", PermissionDescription.ROLE_USER);

    private ConfigValue<SpongeBlockFilter> allowedBlocks = new ConfigValue<>("material", "The block that this mechanic requires.",
            new SpongeBlockFilter(BlockTypes.IRON_BLOCK), TypeToken.of(SpongeBlockFilter.class));

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        allowedBlocks.load(config);

        createPermissions.register();
    }

    @Override
    public void impact(Minecart minecart, CartMechanismBlocks blocks, boolean minor) {
        super.impact(minecart, blocks, minor);

        if (minecart.getPassengers().isEmpty() || isActive(blocks) == TernaryState.FALSE) {
            return;
        }

        Location<World> teleportTarget = null;
        if (blocks.matches("eject")) {
            teleportTarget = blocks.getRail().getRelative(SignUtil.getFront(blocks.getSign()));
        }

        List<Entity> passengers = minecart.getPassengers();
        minecart.clearPassengers();
        if (teleportTarget != null) {
            for (Entity passenger : passengers) {
                passenger.setLocationSafely(teleportTarget);
            }
        }
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[Eject]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermissions;
    }

    @Override
    public SpongeBlockFilter getBlockFilter() {
        return allowedBlocks.getValue();
    }

    @Override
    public String getPath() {
        return "mechanics/minecart/block/ejector";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                allowedBlocks
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                createPermissions
        };
    }
}
