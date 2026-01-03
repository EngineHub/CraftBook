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

package org.enginehub.craftbook.mechanic;

import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.impl.CommandManagerImpl;
import org.enginehub.piston.part.SubCommandPart;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

public class MechanicCommandRegistrar {
    private final CommandManagerService service;
    private final CommandManager topLevelCommandManager;
    private final CommandRegistrationHandler registration;

    private boolean dirty;

    public MechanicCommandRegistrar(CommandManagerService service, CommandManager topLevelCommandManager, CommandRegistrationHandler registration) {
        this.service = service;
        this.topLevelCommandManager = topLevelCommandManager;
        this.registration = registration;
    }

    private void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void unmarkDirty() {
        this.dirty = false;
    }

    public void registerAsSubCommand(String command, Collection<String> aliases, String description,
                                     CommandManager parentManager, BiConsumer<CommandManager, CommandRegistrationHandler> op) {
        markDirty();
        parentManager.register(command, builder -> {
            builder.description(TextComponent.of(description));
            builder.aliases(aliases);

            CommandManager manager = service.newCommandManager();
            op.accept(manager, registration);

            builder.addPart(SubCommandPart.builder(TranslatableComponent.of("worldedit.argument.action"), TextComponent.of("Sub-command to run."))
                .withCommands(manager.getAllCommands().toList())
                .required()
                .build());
        });
    }

    public void registerAsSubCommand(String command, String description, CommandManager parentManager,
                                     BiConsumer<CommandManager, CommandRegistrationHandler> op) {
        registerAsSubCommand(command, new ArrayList<>(), description, parentManager, op);
    }

    public void registerTopLevelWithSubCommands(String command, Collection<String> aliases, String description,
                                                BiConsumer<CommandManager, CommandRegistrationHandler> op) {
        registerAsSubCommand(command, aliases, description, topLevelCommandManager, op);
    }

    public void registerTopLevelCommands(BiConsumer<CommandManager, CommandRegistrationHandler> op) {
        markDirty();
        CommandManager componentManager = service.newCommandManager();
        op.accept(componentManager, registration);
        topLevelCommandManager.registerManager(componentManager);
    }

    @SuppressWarnings({ "unchecked" })
    public void unregisterTopLevel(String command) {
        markDirty();
        try {
            Field field = CommandManagerImpl.class.getDeclaredField("commands");
            field.setAccessible(true);
            Map<String, Command> commandMap = (Map<String, Command>) field.get(topLevelCommandManager);
            commandMap.remove(command);
            field.set(topLevelCommandManager, commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            CraftBook.LOGGER.warn("Failed to unregister top-level command", e);
        }
    }

}
