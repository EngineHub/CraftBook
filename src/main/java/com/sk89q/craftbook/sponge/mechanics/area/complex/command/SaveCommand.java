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
import com.sk89q.craftbook.sponge.mechanics.area.complex.CopyManager;
import com.sk89q.craftbook.sponge.mechanics.area.complex.CuboidCopy;
import com.sk89q.craftbook.sponge.mechanics.area.complex.MCEditCuboidCopy;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import com.sk89q.worldedit.world.DataException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.io.IOException;

public class SaveCommand implements CommandExecutor {

    private ComplexArea area;

    public SaveCommand(ComplexArea area) {
        this.area = area;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String id = args.<String>getOne("id").get();
        String namespace = args.<String>getOne("namespace").orElse(((Player) src).getUniqueId().toString());
        boolean personal = namespace.equals(((Player) src).getUniqueId().toString());

        if (!CopyManager.isValidName(id)) {
            src.sendMessage(Text.of("You cannot use this area ID"));
            return CommandResult.empty();
        }

        if (!personal && !area.commandSaveOtherPermissions.hasPermission(src)) {
            src.sendMessage(Text.of("You do not have permission to use this namespace!"));
            return CommandResult.empty();
        }

        World world = ((Player) src).getWorld();

        LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(src.getName());
        if (session == null) {
            src.sendMessage(Text.of(TextColors.RED, "You have not made a selection!"));
            return CommandResult.empty();
        }

        RegionSelector selector = session.getRegionSelector(SpongeWorldEdit.inst().getWorld(world));

        if(!selector.isDefined()) {
            src.sendMessage(Text.of(TextColors.RED, "You have not made a selection!"));
            return CommandResult.empty();
        }
        Region region = selector.getIncompleteRegion();

        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        Vector size = max.subtract(min).add(1, 1, 1);

        // Check maximum size
        if (area.maxAreaSize.getValue() != -1 && size.getBlockX() * size.getBlockY() * size.getBlockZ() > area.maxAreaSize.getValue() && !area.commandSaveBypassLimitPermissions.hasPermission(src)) {
            src.sendMessage(Text.of(TextColors.RED, "Area is larger than allowed " + area.maxAreaSize.getValue() + " blocks."));
            return CommandResult.empty();
        }

        // Check to make sure that a user doesn't have too many toggle
        // areas (to prevent flooding the server with files)
        if (area.maxPerUser.getValue() >= 0 && !namespace.equals("global") && !area.commandSaveBypassLimitPermissions.hasPermission(src)) {
            int count = CopyManager.meetsQuota(namespace, id, area.maxPerUser.getValue());
             if (count > -1) {
                 src.sendMessage(Text.of(TextColors.RED, "You are limited to " + area.maxPerUser.getValue() + " toggle area(s). You have " + count + " areas."));
                 return CommandResult.empty();
             }
        }

        CuboidCopy copy = new MCEditCuboidCopy(min, size, world);
        copy.copy();

        CraftBookPlugin.inst().getLogger().info(src.getName() + " saving toggle area with folder '" + namespace + "' and ID '" + id + "'.");

        // Save
        try {
            CopyManager.save(namespace, id.toLowerCase(), copy);
            src.sendMessage(Text.of(TextColors.YELLOW, "Area saved as '" + id + "' under the '" + namespace + "' namespace."));
            return CommandResult.success();
        } catch (IOException | DataException e) {
            src.sendMessage(Text.of(TextColors.RED, "Could not save area: " + e.getMessage()));
            return CommandResult.empty();
        }
    }
}
