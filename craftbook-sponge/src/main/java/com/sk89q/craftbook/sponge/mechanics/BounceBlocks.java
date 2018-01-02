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
package com.sk89q.craftbook.sponge.mechanics;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.RegexUtil;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.type.TypeTokens;
import ninja.leaping.configurate.ConfigurationNode;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Map;

@Module(id = "bounceblocks", name = "BounceBlocks", onEnable="onInitialize", onDisable="onDisable")
public class BounceBlocks extends SpongeSignMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.bounceblocks", "Allows the user to create the " + getName() + " mechanic.", PermissionDescription.ROLE_USER);
    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.bounceblocks.use", "Allows the user to use the " + getName() + " mechanic.", PermissionDescription.ROLE_USER);

    private ConfigValue<List<BlockFilter>> allowedBlocks = new ConfigValue<>("allowed-blocks", "The list of blocks that can be normal bounce blocks.",
            getDefaultBlocks(), new TypeTokens.BlockFilterListTypeToken());
    private ConfigValue<Double> sensitivity = new ConfigValue<>("sensitivity", "Sensitivity of jump detection.", 0.1d, TypeToken.of(Double.class));
    private ConfigValue<Map<BlockFilter, String>> autoBlocks = new ConfigValue<>("auto-blocks", "Bounce blocks that are predefined.",
            Maps.newHashMap(ImmutableMap.of(new BlockFilter("hardened_clay"), "2,1,2")), new TypeToken<Map<BlockFilter, String>>() {});

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        allowedBlocks.load(config);
        sensitivity.load(config);
        autoBlocks.load(config);

        createPermissions.register();
        usePermissions.register();
    }

    @Listener
    public void onEntityMove(MoveEntityEvent event) {
        Vector3d fromPosition = event.getFromTransform().getPosition();
        Vector3d toPosition = event.getToTransform().getPosition();

        if(Math.abs(toPosition.getY() - fromPosition.getY()) > sensitivity.getValue() && fromPosition.getY() - fromPosition.getFloorY() < 0.25) {
            Location<World> block = event.getFromTransform().getLocation().getRelative(Direction.DOWN);

            if (BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), block.getBlock())) {
                Location<World> signBlock = block.getRelative(Direction.DOWN);
                if (SignUtil.isSign(signBlock)) {
                    Sign sign = (Sign) signBlock.getTileEntity().get();
                    if (isMechanicSign(sign)) {
                        doAction(event.getTargetEntity(), SignUtil.getTextRaw(sign, 2), event.getToTransform().getRotation());
                    }
                }
            } else {
                for (Map.Entry<BlockFilter, String> blockFilterStringEntry : autoBlocks.getValue().entrySet()) {
                    if (blockFilterStringEntry.getKey().getApplicableBlockStates().contains(block.getBlock())) {
                        doAction(event.getTargetEntity(), blockFilterStringEntry.getValue(), event.getToTransform().getRotation());
                    }
                }
            }
        }
    }

    private void doAction(Entity entity, String positionString, Vector3d rotation) {
        if (entity instanceof Player) {
            if (!usePermissions.hasPermission((Subject) entity)) {
                return;
            }
        }

        double x = 0;
        double y;
        double z = 0;
        boolean straight = positionString.startsWith("!");

        String[] bits = RegexUtil.COMMA_PATTERN.split(StringUtils.replace(positionString, "!", ""));
        if (bits.length == 0) {
            y = 0.5;
        } else if (bits.length == 1) {
            try {
                y = Double.parseDouble(bits[0]);
            } catch (NumberFormatException e) {
                y = 0.5;
            }
        } else {
            x = Double.parseDouble(bits[0]);
            y = Double.parseDouble(bits[1]);
            z = Double.parseDouble(bits[2]);
        }

        if (!straight) {
            double pitch = ((rotation.getX() + 90d) * Math.PI) / 180d;
            double yaw  = ((rotation.getY() + 90d)  * Math.PI) / 180d;

            x *= Math.sin(pitch) * Math.cos(yaw);
            z *= Math.sin(pitch) * Math.sin(yaw);
        }

        entity.setVelocity(new Vector3d(x, y, z));
        entity.offer(Keys.FALL_DISTANCE, -20f);
    }

    @Override
    public String getPath() {
        return "mechanics/bounce_blocks";
    }

    @Override
    public boolean isValid(Location<World> location) {
        if (location.getBlockY() >= 1) {
            Location<World> below = location.getRelative(Direction.DOWN);
            if (SignUtil.isSign(below)) {
                Sign sign = (Sign) below.getTileEntity().get();

                return isMechanicSign(sign);
            }
        }
        return false;
    }

    private static List<BlockFilter> getDefaultBlocks() {
        return Lists.newArrayList(
                new BlockFilter("DIAMOND_BLOCK")
        );
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[Jump]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermissions;
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                allowedBlocks,
                sensitivity,
                autoBlocks
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[]  {
                createPermissions,
                usePermissions
        };
    }
}
