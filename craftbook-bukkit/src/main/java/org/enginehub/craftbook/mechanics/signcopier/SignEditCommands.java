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

package org.enginehub.craftbook.mechanics.signcopier;

import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class SignEditCommands {

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration, SignCopier signCopier) {
        registration.register(
            commandManager,
            SignEditCommandsRegistration.builder(),
            new SignEditCommands(signCopier)
        );
    }

    private final SignCopier signCopier;

    public SignEditCommands(SignCopier signCopier) {
        this.signCopier = signCopier;
    }

    @Command(name = "edit", desc = "Edits the copied sign.")
    @CommandPermissions({ "craftbook.signcopier.edit" })
    public void editSign(CraftBookPlayer player,
                         @Arg(desc = "The line to edit") int line,
                         @Arg(desc = "The text to use", variable = true) String text) throws CraftBookException {
        if (!signCopier.hasSign(player.getUniqueId())) {
            throw new CraftBookException(TranslatableComponent.of("craftbook.signcopier.no-copy"));
        }

        if (line < 1 || line > 4) {
            throw new CraftBookException(TranslatableComponent.of("craftbook.signcopier.invalid-line"));
        }

        // TODO Integrate MiniMessage

        signCopier.setSignLine(player.getUniqueId(), line - 1, text);
        player.printInfo(TranslatableComponent.of(
            "craftbook.signcopier.edited",
            TextComponent.of(line),
            TextComponent.of(text)
        ));
    }

    @Command(name = "clear", desc = "Clears the copied sign.")
    @CommandPermissions({ "craftbook.signcopier.clear" })
    public void clear(CraftBookPlayer player) throws CraftBookException {
        if (!signCopier.hasSign(player.getUniqueId())) {
            throw new CraftBookException(TranslatableComponent.of("craftbook.signcopier.no-copy"));
        }

        signCopier.clearSign(player.getUniqueId());
        player.printInfo(TranslatableComponent.of("craftbook.signcopier.cleared"));
    }
}
