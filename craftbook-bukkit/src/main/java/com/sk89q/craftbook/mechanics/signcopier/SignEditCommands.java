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

package com.sk89q.craftbook.mechanics.signcopier;

import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.exception.CraftBookException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class SignEditCommands {

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration) {
        registration.register(
                commandManager,
                SignEditCommandsRegistration.builder(),
                new SignEditCommands()
        );
    }

    public SignEditCommands() {
    }

    @Command(name = "edit", desc = "Edits the copied sign.")
    @CommandPermissions({"craftbook.mech.signcopy.edit"})
    public void editSign(CraftBookPlayer player,
            @Arg(desc = "The line to edit") int line,
            @Arg(desc = "The text to use", variable = true) String text) throws CraftBookException {

        if(!SignCopier.signs.containsKey(player.getName()))
            throw new CraftBookException("You haven't copied a sign!");

        if (line < 1 || line > 4) {
            throw new CraftBookException("Line out of bounds. Must be between 1 and 4.");
        }

        String[] signCache = SignCopier.signs.get(player.getName());
        signCache[line - 1] = text;
        SignCopier.signs.put(player.getName(), signCache);

        player.print("Edited line " + line + ". Text is now: " + text);
    }
}
