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

package org.enginehub.craftbook.mechanics.minecart.blocks.station;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.bukkit.Bukkit;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class StationCommands {

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration, CartStation cartStation) {
        registration.register(
            commandManager,
            StationCommandsRegistration.builder(),
            new StationCommands(cartStation)
        );
    }

    private final CartStation cartStation;

    public StationCommands(CartStation cartStation) {
        this.cartStation = cartStation;
    }

    @Command(
        name = "station",
        desc = "Select a CraftBook Minecart station."
    )
    @CommandPermissions({ "craftbook.minecartstation.station" })
    public void st(Actor actor,
                   @Arg(desc = "The station to select, or null to view current", def = "") String station,
                   @ArgFlag(name = 'p', desc = "The player to target") String otherPlayer) throws CraftBookException {
        Player player = null;
        if (otherPlayer != null) {
            org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(otherPlayer);
            if (bukkitPlayer != null) {
                player = BukkitAdapter.adapt(bukkitPlayer);
            }
        } else if (actor instanceof Player) {
            player = (Player) actor;
        } else {
            throw new CraftBookException(TranslatableComponent.of("craftbook.command.player-required"));
        }

        if (player == null) {
            throw new CraftBookException(TranslatableComponent.of("craftbook.command.unknown-player"));
        }

        if (station == null || station.isBlank()) {
            String stationName = cartStation.getStation(player.getUniqueId());

            if (stationName == null) {
                actor.printInfo(TranslatableComponent.of("craftbook.minecartstation.none-selected"));
            } else {
                actor.printInfo(TranslatableComponent.of("craftbook.minecartstation.current-selected", TextComponent.of(stationName).color(TextColor.AQUA)));
            }
        } else {
            cartStation.setStation(player.getUniqueId(), station);
            actor.printInfo(TranslatableComponent.of("craftbook.minecartstation.set-to", TextComponent.of(station).color(TextColor.AQUA)));
        }
    }
}
