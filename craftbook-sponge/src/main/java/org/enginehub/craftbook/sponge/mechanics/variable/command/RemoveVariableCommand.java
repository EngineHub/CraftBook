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
package org.enginehub.craftbook.sponge.mechanics.variable.command;

import org.enginehub.craftbook.sponge.mechanics.variable.Variables;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class RemoveVariableCommand implements CommandExecutor {

    private Variables variables;

    public RemoveVariableCommand(Variables variables) {
        this.variables = variables;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String key = args.<String>getOne("key").get();
        boolean global = args.hasAny("g");
        String namespace = global ? Variables.GLOBAL_NAMESPACE : args.<String>getOne("namespace").orElse(((Player) src).getUniqueId().toString());
        if (namespace.equals(Variables.GLOBAL_NAMESPACE) && !global) {
            src.sendMessage(Text.of(TextColors.RED, "Invalid namespace!"));
            return CommandResult.empty();
        }

        variables.removeVariable(namespace, key);
        src.sendMessage(Text.of(TextColors.YELLOW, "Removed variable " + key));

        return CommandResult.success();
    }
}
