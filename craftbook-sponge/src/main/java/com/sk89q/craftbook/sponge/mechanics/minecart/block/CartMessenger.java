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
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.regex.Pattern;

@Module(id = "cartmessenger", name = "CartMessenger", onEnable="onInitialize", onDisable="onDisable")
public class CartMessenger extends SpongeCartBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.cartmessenger", "Allows the user to create the " + getName() + " mechanic.", PermissionDescription.ROLE_USER);

    private ConfigValue<BlockFilter> allowedBlocks = new ConfigValue<>("material", "The block that this mechanic requires.", new BlockFilter("END_STONE"), TypeToken
            .of(BlockFilter.class));

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        allowedBlocks.load(config);

        createPermissions.register();
    }

    @Override
    public void impact(Minecart minecart, CartMechanismBlocks blocks, boolean minor) {
        super.impact(minecart, blocks, minor);

        if (minor || isActive(blocks) == TernaryState.FALSE) {
            return;
        }

        if (!blocks.matches("print") || minecart.getPassengers().isEmpty()) {
            return;
        }

        Sign sign = blocks.getSign().getTileEntity().map(tile -> (Sign) tile).get();
        StringBuilder messageBuilder = new StringBuilder();

        while (sign != null) {
            for (int line = messageBuilder.length() == 0 ? 2 : 0; line < 4; line ++) {
                messageBuilder.append(SignUtil.getTextRaw(sign, line));
            }
            Location<World> below = sign.getLocation().getRelative(Direction.DOWN);
            if (SignUtil.isSign(below)) {
                sign = below.getTileEntity().map(tile -> (Sign) tile).get();
            } else {
                sign = null;
            }
        }

        String message = messageBuilder.toString();
        Arrays.stream(message.split(Pattern.quote("\\n"))).forEach(messageLine -> {
            Text messageText = Text.of(messageLine);

            minecart.getPassengers().stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .forEach(player -> player.sendMessage(messageText));
        });
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[Print]"
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
        return "mechanics/minecart/block/messenger";
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
