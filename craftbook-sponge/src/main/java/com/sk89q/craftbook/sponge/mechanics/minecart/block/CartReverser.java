/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.minecart.block;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.TernaryState;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.util.Direction;

@Module(id = "cartreverser", name = "CartReverser", onEnable="onInitialize", onDisable="onDisable")
public class CartReverser extends SpongeCartBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.cartreverser", "Allows the user to create the " + getName() + " mechanic.", PermissionDescription.ROLE_USER);

    private ConfigValue<BlockFilter> allowedBlocks = new ConfigValue<>("material", "The block that this mechanic requires.", new BlockFilter("WOOL"), TypeToken.of(BlockFilter.class));

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        allowedBlocks.load(config);

        createPermissions.register();
    }

    @Override
    public void impact(Minecart minecart, CartMechanismBlocks blocks, boolean minor) {
        super.impact(minecart, blocks, minor);

        if (isActive(blocks) == TernaryState.FALSE || minor) {
            return;
        }

        if (!blocks.matches("reverse")) {
            minecart.setVelocity(minecart.getVelocity().mul(-1));
            return;
        }

        Direction direction = SignUtil.getFacing(blocks.getSign());
        Vector3d normalVelocity = minecart.getVelocity().normalize();

        switch(direction) {
            case NORTH:
                if (normalVelocity.getFloorZ() != -1) {
                    minecart.setVelocity(minecart.getVelocity().mul(-1));
                }
                break;
            case SOUTH:
                if (normalVelocity.getFloorZ() != 1) {
                    minecart.setVelocity(minecart.getVelocity().mul(-1));
                }
                break;
            case EAST:
                if (normalVelocity.getFloorX() != 1) {
                    minecart.setVelocity(minecart.getVelocity().mul(-1));
                }
                break;
            case WEST:
                if (normalVelocity.getFloorX() != -1) {
                    minecart.setVelocity(minecart.getVelocity().mul(-1));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[Reverse]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermissions;
    }

    @Override
    public BlockFilter getBlockFilter() {
        return allowedBlocks.getValue();
    }

    @Override
    public String getPath() {
        return "mechanics/minecart/block/reverser";
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
