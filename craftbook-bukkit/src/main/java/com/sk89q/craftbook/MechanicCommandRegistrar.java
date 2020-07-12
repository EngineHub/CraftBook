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

package com.sk89q.craftbook;

import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.SubCommandPart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class MechanicCommandRegistrar {
    private CommandManagerService service;
    private CommandManager topLevelCommandManager;
    private CommandRegistrationHandler registration;

    public MechanicCommandRegistrar(CommandManagerService service, CommandManager topLevelCommandManager, CommandRegistrationHandler registration) {
        this.service = service;
        this.topLevelCommandManager = topLevelCommandManager;
        this.registration = registration;
    }

    public void registerAsSubCommand(String command, Collection<String> aliases, String description,
            CommandManager parentManager, BiConsumer<CommandManager, CommandRegistrationHandler> op) {
        parentManager.register(command, builder -> {
            builder.description(TextComponent.of(description));
            builder.aliases(aliases);

            CommandManager manager = service.newCommandManager();
            op.accept(manager, registration);

            builder.addPart(SubCommandPart.builder(TranslatableComponent.of("worldedit.argument.action"), TextComponent.of("Sub-command to run."))
                    .withCommands(manager.getAllCommands().collect(Collectors.toList()))
                    .required()
                    .build());
        });
    }

    public void registerAsSubCommand(String command, String description, CommandManager parentManager,
            BiConsumer<CommandManager, CommandRegistrationHandler> op) {
        registerAsSubCommand(command, new ArrayList<>(), description, parentManager, op);
    }

    public void registerTopLevelCommands(BiConsumer<CommandManager, CommandRegistrationHandler> op) {
        CommandManager componentManager = service.newCommandManager();
        op.accept(componentManager, registration);
        topLevelCommandManager.registerManager(componentManager);
    }

    @Deprecated
    public <T> void registerConverter(Key<T> key, ArgumentConverter<T> converter) {
        topLevelCommandManager.registerConverter(key, converter);
    }
}