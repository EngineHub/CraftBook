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
package org.enginehub.craftbook.sponge.mechanics.area.complex.command;

import org.enginehub.craftbook.sponge.CraftBookPlugin;
import org.enginehub.craftbook.sponge.mechanics.area.complex.ComplexArea;
import org.enginehub.craftbook.sponge.mechanics.area.complex.CopyManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.File;
import java.util.Locale;

@NonnullByDefault
public class DeleteCommand implements CommandExecutor {

    private ComplexArea area;

    public DeleteCommand(ComplexArea area) {
        this.area = area;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String id = args.<String>getOne("id").get();
        String namespace = args.<String>getOne("namespace").orElse(((Player) src).getUniqueId().toString());
        boolean personal = namespace.equals(((Player) src).getUniqueId().toString());

        if (!personal && !area.commandDeleteOtherPermissions.hasPermission(src)) {
            src.sendMessage(Text.of("You do not have permission to use this namespace!"));
            return CommandResult.empty();
        }

        id = id.toLowerCase(Locale.ENGLISH);

        File folder = new File(new File(CraftBookPlugin.inst().getWorkingDirectory(), "areas"), namespace);
        File file = new File(folder, id + CopyManager.getFileSuffix());

        if (!folder.exists() || !file.exists()) {
            src.sendMessage(Text.of(TextColors.RED, "This area does not exist!"));
            return CommandResult.empty();
        }

        file.delete();

        src.sendMessage(Text.of(TextColors.YELLOW, "Area deleted!"));

        return CommandResult.success();
    }
}
