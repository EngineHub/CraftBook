/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.area.complex.command;

import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.area.complex.ComplexArea;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@NonnullByDefault
public class ListCommand implements CommandExecutor {

    private ComplexArea area;

    public ListCommand(ComplexArea area) {
        this.area = area;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String namespace = args.<String>getOne("namespace").orElse(((Player) src).getUniqueId().toString());
        if (!namespace.equals(((Player) src).getUniqueId().toString()) && !area.commandListOtherPermissions.hasPermission(src)) {
            src.sendMessage(Text.of(TextColors.RED, "You don't have permission to list areas of other namespaces!"));
            return CommandResult.empty();
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder builder = paginationService.builder();

        String[] files = new File(new File(CraftBookPlugin.inst().getWorkingDirectory(), "areas"), namespace).list();
        List<Text> contents = new ArrayList<>();
        for (String file : files)
            if (file != null)
                contents.add(Text.of(file));

        builder.contents(contents).title(Text.of("Areas")).sendTo(src);

        return CommandResult.success();
    }
}
