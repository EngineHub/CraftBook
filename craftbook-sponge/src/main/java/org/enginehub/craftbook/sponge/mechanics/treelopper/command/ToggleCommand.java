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
package org.enginehub.craftbook.sponge.mechanics.treelopper.command;

import org.enginehub.craftbook.sponge.mechanics.treelopper.TreeLopper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class ToggleCommand implements CommandExecutor {

    private TreeLopper treeLopper;

    public ToggleCommand(TreeLopper treeLopper) {
        this.treeLopper = treeLopper;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            return CommandResult.empty();
        }
        boolean currentState = treeLopper.disabledPlayers.getValue().contains(((Player) src).getUniqueId());
        if (args.hasAny("state")) {
            currentState = args.<Boolean>getOne("state").get();
        } else {
            currentState = !currentState;
        }

        treeLopper.disabledPlayers.getValue().remove(((Player) src).getUniqueId());
        if (!currentState) {
            treeLopper.disabledPlayers.getValue().add(((Player) src).getUniqueId());
        }

        return CommandResult.success();
    }
}
