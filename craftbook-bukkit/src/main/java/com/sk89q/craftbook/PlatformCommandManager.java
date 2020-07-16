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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.util.formatting.WorldEditText.reduceToText;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sk89q.bukkit.util.CommandInfo;
import com.sk89q.bukkit.util.CommandRegistration;
import com.sk89q.craftbook.bukkit.BukkitCommandInspector;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.commands.CraftBookCommands;
import com.sk89q.craftbook.core.command.argument.WorldConverter;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.argument.Arguments;
import com.sk89q.craftbook.core.command.argument.RegistryConverter;
import com.sk89q.worldedit.command.argument.VectorConverter;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandArgParser;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.internal.util.Substring;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.logging.DynamicStreamHandler;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.CommandExecutionException;
import org.enginehub.piston.exception.ConditionFailedException;
import org.enginehub.piston.exception.UsageException;
import org.enginehub.piston.impl.CommandManagerServiceImpl;
import org.enginehub.piston.inject.InjectedValueStore;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MapBackedValueStore;
import org.enginehub.piston.inject.MemoizingValueAccess;
import org.enginehub.piston.inject.MergedValueAccess;
import org.enginehub.piston.suggestion.Suggestion;
import org.enginehub.piston.util.HelpGenerator;
import org.enginehub.piston.util.ValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlatformCommandManager {
    public static final Pattern COMMAND_CLEAN_PATTERN = Pattern.compile("^[/]+");
    private static final Logger log = LoggerFactory.getLogger(PlatformCommandManager.class);

    private final CraftBookPlugin plugin;
    private final CommandManagerServiceImpl commandManagerService;
    private final CommandManager commandManager;
    private final InjectedValueStore globalInjectedValues;
    private final DynamicStreamHandler dynamicHandler = new DynamicStreamHandler();
    private final CommandRegistrationHandler registration;
    private final MechanicCommandRegistrar mechanicCommandRegistrar;

    public PlatformCommandManager(final CraftBookPlugin plugin) {
        checkNotNull(plugin);

        this.plugin = plugin;
        this.commandManagerService = new CommandManagerServiceImpl();
        this.commandManager = commandManagerService.newCommandManager();
        this.globalInjectedValues = MapBackedValueStore.create();
        this.registration = new CommandRegistrationHandler(ImmutableList.of());
        this.mechanicCommandRegistrar = new MechanicCommandRegistrar(commandManagerService, commandManager, registration);

        // setup separate from main constructor
        // ensures that everything is definitely assigned
        initialize();
    }

    private void initialize() {
        // Set up the commands manager
        registerAlwaysInjectedValues();
        registerArgumentConverters();
        registerCoreCommands();
    }

    private void registerAlwaysInjectedValues() {

    }

    private void registerArgumentConverters() {
        RegistryConverter.register(commandManager);
        com.sk89q.worldedit.command.argument.RegistryConverter.register(commandManager);
        VectorConverter.register(commandManager);
        WorldConverter.register(commandManager);
    }

    private void registerCoreCommands() {
        CraftBookCommands.register(commandManagerService, commandManager, registration);
    }

    public MechanicCommandRegistrar getMechanicRegistrar() {
        return mechanicCommandRegistrar;
    }

    public void registerCommandsWith(CraftBookPlugin plugin) {
        BukkitCommandInspector inspector = new BukkitCommandInspector(plugin, commandManager);

        CommandRegistration registration = new CommandRegistration(plugin);
        registration.register(commandManager.getAllCommands()
                .map(command -> {
                    String[] permissionsArray = command.getCondition()
                            .as(PermissionCondition.class)
                            .map(PermissionCondition::getPermissions)
                            .map(s -> s.toArray(new String[0]))
                            .orElseGet(() -> new String[0]);

                    String[] aliases = Stream.concat(
                            Stream.of(command.getName()),
                            command.getAliases().stream()
                    ).toArray(String[]::new);
                    // TODO Handle localisation correctly
                    return new CommandInfo(reduceToText(command.getUsage(), WorldEdit.getInstance().getConfiguration().defaultLocale),
                            reduceToText(command.getDescription(), WorldEdit.getInstance().getConfiguration().defaultLocale), aliases,
                            inspector, permissionsArray);
                }).collect(Collectors.toList()));
    }

    private Stream<Substring> parseArgs(String input) {
        return CommandArgParser.forArgString(input.substring(1)).parseArgs();
    }

    private MemoizingValueAccess initializeInjectedValues(Arguments arguments, Actor actor) {
        InjectedValueStore store = MapBackedValueStore.create();

        store.injectValue(Key.of(Actor.class), ValueProvider.constant(actor));
        if (actor instanceof CraftBookPlayer) {
            store.injectValue(Key.of(CraftBookPlayer.class), ValueProvider.constant((CraftBookPlayer) actor));
        } else {
            store.injectValue(Key.of(CraftBookPlayer.class), context -> {
                throw new CommandException(
                        TextComponent.of("This command must be used with a player."),
                        ImmutableList.of()
                );
            });
        }

        store.injectValue(Key.of(Arguments.class), ValueProvider.constant(arguments));

        return MemoizingValueAccess.wrap(MergedValueAccess.of(store, globalInjectedValues));
    }

    private void handleUnknownException(Actor actor, Throwable t) {
        actor.printError(TranslatableComponent.of("worldedit.command.error.report"));
        actor.print(TextComponent.of(t.getClass().getName() + ": " + t.getMessage()));
        log.error("An unexpected error while handling a CraftBook command", t);
    }

    public void handleCommand(Actor actor, String arguments) {
        String[] split = parseArgs(arguments).map(Substring::getSubstring).toArray(String[]::new);

        // No command found!
        if (!commandManager.containsCommand(split[0])) {
            return;
        }

        MemoizingValueAccess context = initializeInjectedValues(() -> arguments, actor);

        try {
            // This is a bit of a hack, since the call method can only throw CommandExceptions
            // everything needs to be wrapped at least once. Which means to handle all WorldEdit
            // exceptions without writing a hook into every dispatcher, we need to unwrap these
            // exceptions and rethrow their converted form, if their is one.
            try {
                commandManager.execute(context, ImmutableList.copyOf(split));
            } catch (Throwable t) {
                // Use the exception converter to convert the exception if any of its causes
                // can be converted, otherwise throw the original exception

                // FIXME: Reimplement or use WorldEdit's exceptions
                // Throwable next = t;
                // do {
                //     exceptionConverter.convert(next);
                //     next = next.getCause();
                // } while (next != null);

                throw t;
            }
        } catch (ConditionFailedException e) {
            if (e.getCondition() instanceof PermissionCondition) {
                actor.printError(TranslatableComponent.of("worldedit.command.permissions"));
            } else {
                actor.print(e.getRichMessage());
            }
        } catch (UsageException e) {
            actor.print(TextComponent.builder("")
                    .color(TextColor.RED)
                    .append(e.getRichMessage())
                    .build());
            ImmutableList<Command> cmd = e.getCommands();
            if (!cmd.isEmpty()) {
                actor.print(TextComponent.builder("Usage: ")
                        .color(TextColor.RED)
                        .append(HelpGenerator.create(e.getCommandParseResult()).getUsage())
                        .build());
            }
        } catch (CommandExecutionException e) {
            // FIXME: Put this in an exception converter.
            if (e.getCause() instanceof com.sk89q.minecraft.util.commands.CommandException) {
                actor.print(TextComponent.builder(e.getCause().getMessage()).color(TextColor.RED).build());
                return;
            }

            handleUnknownException(actor, e.getCause());
        } catch (CommandException e) {
            actor.print(TextComponent.builder("")
                    .color(TextColor.RED)
                    .append(e.getRichMessage())
                    .build());
        } catch (Throwable t) {
            handleUnknownException(actor, t);
        }
    }

    public List<Substring> handleCommandSuggestion(Actor actor, String arguments) {
        try {
            List<Substring> split = parseArgs(arguments).collect(Collectors.toList());
            List<String> argStrings = split.stream()
                    .map(Substring::getSubstring)
                    .collect(Collectors.toList());
            MemoizingValueAccess access = initializeInjectedValues(() -> arguments, actor);
            ImmutableSet<Suggestion> suggestions;
            try {
                suggestions = commandManager.getSuggestions(access, argStrings);
            } catch (Throwable t) { // catch errors which are *not* command exceptions generated by parsers/suggesters
                if (!(t instanceof CommandException)) {
                    log.debug("Unexpected error occurred while generating suggestions for input: " + arguments, t);
                    return Collections.emptyList();
                }
                throw t;
            }

            return suggestions.stream()
                    .map(suggestion -> {
                        int noSlashLength = arguments.length() - 1;
                        Substring original = suggestion.getReplacedArgument() == split.size()
                                ? Substring.from(arguments, noSlashLength, noSlashLength)
                                : split.get(suggestion.getReplacedArgument());
                        // increase original points by 1, for removed `/` in `parseArgs`
                        return Substring.wrap(
                                suggestion.getSuggestion(),
                                original.getStart() + 1,
                                original.getEnd() + 1
                        );
                    }).collect(Collectors.toList());
        } catch (ConditionFailedException ignored) { }

        return Collections.emptyList();
    }
}