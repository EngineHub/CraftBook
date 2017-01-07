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
package com.sk89q.craftbook.sponge.mechanics.signcopier.command;

import com.sk89q.craftbook.sponge.mechanics.signcopier.SignCopier;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

public class EditSignCommand implements CommandExecutor {

    private SignCopier signCopier;

    public EditSignCommand(SignCopier signCopier) {
        this.signCopier = signCopier;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            if (!signCopier.getSigns().containsKey(((Player) src).getUniqueId())) {
                src.sendMessage(Text.of(TextColors.RED, "You haven't copied a sign!"));
            } else {
                int line = args.<Integer>getOne("line").orElse(-1) - 1;
                if (line < 0 || line > 3) {
                    src.sendMessage(Text.of(TextColors.RED, "Line must be between 1 and 4."));
                } else {
                    String text = args.<String>getOne("text").orElse("");

                    signCopier.getSigns().get(((Player) src).getUniqueId()).set(line, TextSerializers.FORMATTING_CODE.deserialize(text));
                    src.sendMessage(Text.of(TextColors.YELLOW, "Updated message!"));
                }
            }
        } else {
            src.sendMessage(Text.of(TextColors.RED, "Only players can use this mechanic!"));
        }

        return CommandResult.success();
    }
}
