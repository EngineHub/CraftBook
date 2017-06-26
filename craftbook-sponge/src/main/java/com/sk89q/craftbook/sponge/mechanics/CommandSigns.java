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

import static com.sk89q.craftbook.sponge.util.locale.TranslationsManager.USE_PERMISSIONS;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.LastPowerData;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;

@Module(id = "commandsigns", name = "CommandSigns", onEnable="onInitialize", onDisable="onDisable")
public class CommandSigns extends SpongeSignMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private SpongePermissionNode createPermission = new SpongePermissionNode("craftbook.commandsigns", "Allows creation of Command Signs", PermissionDescription.ROLE_ADMIN);
    private SpongePermissionNode usePermission = new SpongePermissionNode("craftbook.commandsigns.use", "Allows usage of Command Signs", PermissionDescription.ROLE_USER);

    private ConfigValue<Boolean> allowRedstone = new ConfigValue<>("allow-redstone", "Allow redstone to trigger the commands.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        createPermission.register();
        usePermission.register();

        allowRedstone.load(config);
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        event.getTargetBlock().getLocation().filter(this::isValid).ifPresent(worldLocation -> triggerMechanic(worldLocation, player));
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First LocatableBlock source) {
        if(!allowRedstone.getValue())
            return;

        Location<World> block = source.getLocation();

        if (isValid(block)) {
            Player player = event.getCause().get(NamedCause.SOURCE, Player.class).orElse(null);

            boolean isPowered = BlockUtil.getBlockPowerLevel(block).orElse(0) > 0;
            boolean wasPowered = block.get(CraftBookKeys.LAST_POWER).orElse(0) > 0;

            if (isPowered && !wasPowered) {
                triggerMechanic(block, player);
            }

            block.offer(new LastPowerData(isPowered ? 15 : 0));
        }
    }

    private void triggerMechanic(Location<World> location, @Nullable Player player) {
        if (player != null && !usePermission.hasPermission(player)) {
            player.sendMessage(USE_PERMISSIONS);
            return;
        }

        Sign sign = location.getTileEntity().map(tile -> (Sign) tile).get();
        if (SignUtil.getTextRaw(sign, 0).equalsIgnoreCase("EXPANSION")) {
            return;
        }

        StringBuilder command = new StringBuilder();

        do {
            if(command.length() > 0 && !SignUtil.getTextRaw(sign, 0).equals("EXPANSION")) break;
            sign = location.getTileEntity().map(tile -> (Sign) tile).get();
            command.append(SignUtil.getTextRaw(sign, 2)).append(SignUtil.getTextRaw(sign, 3));

            location = location.getRelative(Direction.DOWN);
        } while(isValid(location));

        String commandString = command.toString();
        if (player == null) {
            if (commandString.contains("@p")) {
                return;
            }
        } else {
            commandString = commandString.replace("@p", player.getName());
        }

        if (commandString.startsWith("/")) {
            commandString = commandString.substring(1);
        }

        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), commandString);
    }

    @Override
    public String getPath() {
        return "mechanics/command_signs";
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[Command]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermission;
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                createPermission,
                usePermission
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                allowRedstone
        };
    }
}
