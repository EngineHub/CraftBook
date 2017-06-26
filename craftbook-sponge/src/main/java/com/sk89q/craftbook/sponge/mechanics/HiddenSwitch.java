/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
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

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

@Module(id = "hiddenswitch", name = "HiddenSwitch", onEnable="onInitialize", onDisable="onDisable")
public class HiddenSwitch extends SpongeSignMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> allowAnySide = new ConfigValue<>("allow-any-side", "Allows the user to click any side of the attached block.", false);

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.hidden-switch", "Allows the user to create Hidden Switches", PermissionDescription.ROLE_USER);
    private SpongePermissionNode usePermission = new SpongePermissionNode("craftbook.hidden-switch.use", "Allows the user to use Hidden Switches", PermissionDescription.ROLE_USER);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        allowAnySide.load(config);

        createPermissions.register();
        usePermission.register();
    }

    @Listener
    public void onClick(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        event.getTargetBlock().getLocation().ifPresent((location) -> {
            List<Location<World>> signLocations = new ArrayList<>();

            if (allowAnySide.getValue()) {
                signLocations.addAll(BlockUtil.getAdjacentExcept(location, event.getTargetSide()));
            } else {
                signLocations.add(location.getRelative(event.getTargetSide().getOpposite()));
            }

            for (Location<World> signLocation : signLocations) {
                if (SignUtil.isSign(signLocation)) {
                    Sign sign = (Sign) signLocation.getTileEntity().get();
                    if (isMechanicSign(sign)) {
                        if (!usePermission.hasPermission(player)) {
                            return;
                        }
                        if (toggleSwitches(sign, player)) {
                            player.sendMessage(Text.of(TextColors.YELLOW, "You hear the muffled click of a switch."));
                        }
                        break;
                    }
                }
            }
        });
    }

    private static boolean toggleSwitches(Sign sign, Player player) {
        Direction[] checkFaces = new Direction[4];
        checkFaces[0] = Direction.UP;
        checkFaces[1] = Direction.DOWN;

        switch (SignUtil.getFacing(sign.getLocation())) {
            case EAST:
            case WEST:
                checkFaces[2] = Direction.NORTH;
                checkFaces[3] = Direction.SOUTH;
                break;
            case NONE:
                break;
            default:
                checkFaces[2] = Direction.EAST;
                checkFaces[3] = Direction.WEST;
                break;
        }

        boolean found = false;

        for (Direction direction : checkFaces) {
            if (direction == null) continue;
            Location<World> checkBlock = sign.getLocation().getRelative(direction);

            if (checkBlock.getBlock().getType() == BlockTypes.LEVER) {
                checkBlock.offer(Keys.POWERED, !checkBlock.get(Keys.POWERED).orElse(false),
                        Cause.source(CraftBookPlugin.spongeInst().getContainer()).named(NamedCause.notifier(player)).build());
                found = true;
            } else if (checkBlock.getBlock().getType() == BlockTypes.STONE_BUTTON || checkBlock.getBlock().getType() == BlockTypes.WOODEN_BUTTON) {
                checkBlock.offer(Keys.POWERED, true,
                        Cause.source(CraftBookPlugin.spongeInst().getContainer()).named(NamedCause.notifier(player)).build());
                checkBlock.addScheduledUpdate(1, checkBlock.getBlock().getType() == BlockTypes.STONE_BUTTON ? 20 : 30);
                found = true;
            }
        }

        return found;
    }

    @Override
    public String getPath() {
        return "mechanics/hidden_switch";
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[X]"
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                createPermissions,
                usePermission
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                allowAnySide
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermissions;
    }
}
