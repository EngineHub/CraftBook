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
package org.enginehub.craftbook.sponge.command.docs;

import com.me4502.modularframework.exception.ModuleNotInstantiatedException;
import com.me4502.modularframework.module.ModuleWrapper;
import com.me4502.modularframework.module.SpongeModuleWrapper;
import org.enginehub.craftbook.core.Mechanic;
import org.enginehub.craftbook.core.util.documentation.DocumentationGenerator;
import org.enginehub.craftbook.core.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.CraftBookPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class GenerateDocsCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        for (ModuleWrapper module : CraftBookPlugin.spongeInst().moduleController.getModules()) {
            if(!module.isEnabled()) continue;
            try {
                Mechanic mechanic = (Mechanic) ((SpongeModuleWrapper) module).getModuleUnchecked();
                if(mechanic instanceof DocumentationProvider)
                    DocumentationGenerator.generateDocumentation((DocumentationProvider) mechanic);
            } catch (ModuleNotInstantiatedException e) {
                CraftBookPlugin.spongeInst().getLogger().error("Failed to generate docs for module: " + module.getName(), e);
            }
        }

        DocumentationGenerator.generateDocumentation(CraftBookPlugin.spongeInst().getConfig());

        return CommandResult.success();
    }
}
