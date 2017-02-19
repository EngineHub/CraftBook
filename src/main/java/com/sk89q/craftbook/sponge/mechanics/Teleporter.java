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
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

import javax.annotation.Nullable;

@Module(id = "teleporter", name = "Teleporter", onEnable="onInitialize", onDisable="onDisable")
public class Teleporter extends SpongeSignMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> allowButtons = new ConfigValue<>("allow-buttons", "Allow teleporters to be controlled by buttons opposite the sign.", true);
    private ConfigValue<Boolean> requireSign = new ConfigValue<>("require-sign",  "Require a teleport sign at the destination.", true);
    private ConfigValue<Double> maxRange = new ConfigValue<>("max-range", "The max range of the teleport, or -1 for infinite.", -1d, TypeToken.of(Double.class));

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.teleporter", "Allows the user to create Teleporters", PermissionDescription.ROLE_STAFF);
    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.teleporter.use", "Allows the user to use Teleporters", PermissionDescription.ROLE_USER);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        allowButtons.load(config);
        requireSign.load(config);
        maxRange.load(config);

        createPermissions.register();
        usePermissions.register();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") @Override
    public boolean verifyLines(Location<World> location, List<Text> lines, @Nullable Player player) {
        String line3Raw = SignUtil.getTextRaw(lines.get(2));
        if (line3Raw.trim().isEmpty() || line3Raw.equalsIgnoreCase("ARRIVAL")) {
            lines.set(2, Text.of("ARRIVAL"));
        } else {
            String[] pos = RegexUtil.COLON_PATTERN.split(line3Raw);
            if (pos.length <= 2) {
                if (player != null) {
                    player.sendMessage(Text.of("mech.teleport.invalidcoords"));
                }
                return false;
            } else {
                try {
                    Double.parseDouble(pos[0]);
                    Double.parseDouble(pos[1]);
                    Double.parseDouble(pos[2]);
                } catch (NumberFormatException e) {
                    if (player != null) {
                        player.sendMessage(Text.of("mech.teleport.invalidcoords"));
                    }
                    return false;
                }
            }
        }
        return super.verifyLines(location, lines, player);
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary.MainHand event, @Named(NamedCause.SOURCE) Player player) {
        event.getTargetBlock().getLocation().ifPresent((location) -> {
            Location<World> signLocation = location;

            if (allowButtons.getValue() &&
                    signLocation.getBlockType() == BlockTypes.STONE_BUTTON || signLocation.getBlockType() == BlockTypes.WOODEN_BUTTON) {
                Direction backDir = SignUtil.getBack(signLocation);
                signLocation = signLocation.getRelative(backDir).getRelative(backDir);
            }

            if (SignUtil.isSign(signLocation)) {
                Sign sign = (Sign) signLocation.getTileEntity().get();

                if (isMechanicSign(sign)) {
                    if (!usePermissions.hasPermission(player)) {
                        player.sendMessage(Text.of(TextColors.RED, "You don't have permission to use this mechanic!"));
                        return;
                    }

                    String destinationTest = SignUtil.getTextRaw(sign, 2);
                    if (destinationTest.equals("ARRIVAL")) {
                        player.sendMessage(Text.of("You can only arrive here."));
                        return;
                    }

                    String[] pos = RegexUtil.COLON_PATTERN.split(destinationTest);
                    if (pos.length <= 2) {
                        player.sendMessage(Text.of("mech.teleport.invalidcoords"));
                        return;
                    }

                    double x, y, z;
                    try {
                        x = Double.parseDouble(pos[0]);
                        y= Double.parseDouble(pos[1]);
                        z = Double.parseDouble(pos[2]);
                    } catch(NumberFormatException e) {
                        player.sendMessage(Text.of("mech.teleport.invalidcoords"));
                        return;
                    }

                    Location<World> destinationLocation = signLocation.getExtent().getLocation(x, y, z);

                    if (requireSign.getValue()
                            && !(SignUtil.isSign(destinationLocation) && isMechanicSign((Sign) destinationLocation.getTileEntity().get()))) {
                        player.sendMessage(Text.of("Missing sign at destination."));
                        return;
                    }

                    if (maxRange.getValue() >= 0
                            && destinationLocation.getPosition().distanceSquared(signLocation.getPosition()) > maxRange.getValue() * maxRange.getValue()) {
                        player.sendMessage(Text.of("Destination too far away."));
                        return;
                    }

                    Location<World> safeLocation = Sponge.getGame().getTeleportHelper().getSafeLocation(destinationLocation).orElse(null);
                    if (safeLocation != null) {
                        player.setLocation(safeLocation);
                        String destinationName = SignUtil.getTextRaw(sign, 0);
                        if (destinationName.isEmpty()) {
                            player.sendMessage(Text.of("You've teleported."));
                        } else {
                            player.sendMessage(Text.of("You are now at " + destinationName));
                        }
                    } else {
                        player.sendMessage(Text.of("Destination Obstructed"));
                    }
                }
            }
        });
    }

    @Override
    public String[] getValidSigns() {
        return new String[]{
                "[Teleporter]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermissions;
    }

    @Override
    public String getPath() {
        return "mechanics/teleporter";
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                createPermissions,
                usePermissions
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                allowButtons,
                requireSign,
                maxRange
        };
    }
}
