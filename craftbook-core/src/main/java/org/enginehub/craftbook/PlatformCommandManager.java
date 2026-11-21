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

package org.enginehub.craftbook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.command.argument.Arguments;
import com.sk89q.worldedit.command.argument.VectorConverter;
import com.sk89q.worldedit.command.argument.WorldConverter;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandArgParseException;
import com.sk89q.worldedit.internal.command.CommandArgParser;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.internal.command.ExtractedArg;
import com.sk89q.worldedit.internal.command.FullStringSuggestion;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.enginehub.craftbook.command.CraftBookExceptionConverter;
import org.enginehub.craftbook.command.argument.RegistryConverter;
import org.enginehub.craftbook.commands.CraftBookCommands;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class PlatformCommandManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private final CommandManagerServiceImpl commandManagerService;
    private final CommandManager commandManager;
    private final InjectedValueStore globalInjectedValues;
    private final CommandRegistrationHandler registration;
    private final MechanicCommandRegistrar mechanicCommandRegistrar;
    private final CraftBookExceptionConverter exceptionConverter;

    public PlatformCommandManager() {
        this.commandManagerService = new CommandManagerServiceImpl();
        this.commandManager = commandManagerService.newCommandManager();
        this.globalInjectedValues = MapBackedValueStore.create();
        this.registration = new CommandRegistrationHandler(List.of());
        this.mechanicCommandRegistrar = new MechanicCommandRegistrar(commandManagerService, commandManager, registration);
        this.exceptionConverter = new CraftBookExceptionConverter(CraftBook.getInstance());

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

    public void registerCommandsWith(CraftBookPlatform platform) {
        platform.registerCommands(this.commandManager);
        this.mechanicCommandRegistrar.unmarkDirty();
    }

    /**
     * Check that the input starts with {@code /}. Some sources, such as the CLI or command blocks, can pass arbitrary
     * text that should not be treated as a command.
     */
    private static boolean isCommandInput(String input) {
        return input.startsWith("/");
    }

    private Stream<ExtractedArg> parseArgs(String input, boolean forCompletions) {
        return CommandArgParser.parse(input.substring(1), forCompletions);
    }

    private MemoizingValueAccess initializeInjectedValues(Arguments arguments, Actor actor) {
        InjectedValueStore store = MapBackedValueStore.create();

        store.injectValue(Key.of(Actor.class), ValueProvider.constant(actor));
        if (actor instanceof CraftBookPlayer player) {
            store.injectValue(Key.of(CraftBookPlayer.class), ValueProvider.constant(player));
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
        LOGGER.error("An unexpected error while handling a CraftBook command", t);
    }

    public void handleCommand(Actor actor, String arguments) {
        if (!isCommandInput(arguments)) {
            return;
        }
        List<String> split;
        try {
            split = parseArgs(arguments, false)
                .map(ExtractedArg::parsedText)
                .toList();
        } catch (CommandArgParseException e) {
            actor.printError(TranslatableComponent.of("worldedit.command.parse-error", e.getRichMessage()));
            return;
        }

        // No command found!
        if (!commandManager.containsCommand(split.getFirst())) {
            return;
        }

        MemoizingValueAccess context = initializeInjectedValues(() -> arguments, actor);

        try {
            // This is a bit of a hack, since the call method can only throw CommandExceptions
            // everything needs to be wrapped at least once. Which means to handle all WorldEdit
            // exceptions without writing a hook into every dispatcher, we need to unwrap these
            // exceptions and rethrow their converted form, if their is one.
            try {
                commandManager.execute(context, split);
            } catch (Throwable t) {
                // Use the exception converter to convert the exception if any of its causes
                // can be converted, otherwise throw the original exception
                Throwable next = t;
                do {
                    exceptionConverter.convert(next);
                    next = next.getCause();
                } while (next != null);

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
            handleUnknownException(actor, e.getCause());
        } catch (CommandException e) {
            actor.printError(TextComponent.builder("")
                .color(TextColor.RED)
                .append(e.getRichMessage())
                .build());
        } catch (Throwable t) {
            handleUnknownException(actor, t);
        }
    }

    public List<FullStringSuggestion> handleCommandSuggestion(Actor actor, String arguments) {
        try {
            if (!isCommandInput(arguments)) {
                return Collections.emptyList();
            }
            List<ExtractedArg> split;
            try {
                split = parseArgs(arguments, true).toList();
            } catch (CommandArgParseException e) {
                LOGGER.debug("Failed to parse command arguments for suggestions: " + arguments, e);
                return Collections.emptyList();
            }
            List<String> argStrings = split.stream()
                .map(ExtractedArg::parsedText)
                .toList();
            MemoizingValueAccess access = initializeInjectedValues(() -> arguments, actor);
            ImmutableSet<Suggestion> suggestions;
            try {
                suggestions = commandManager.getSuggestions(access, argStrings);
            } catch (Throwable t) { // catch errors which are *not* command exceptions generated by parsers/suggesters
                if (!(t instanceof CommandException)) {
                    LOGGER.debug("Unexpected error occurred while generating suggestions for input: " + arguments, t);
                    return Collections.emptyList();
                }
                throw t;
            }

            return suggestions.stream()
                .map(suggestion -> {
                    if (suggestion.getReplacedArgument() == split.size()) {
                        ExtractedArg last = split.getLast();
                        if (last.unclosedQuote()) {
                            // Re-quote the last argument so it's closed, otherwise the new argument would be
                            // inside the quote. This also drops any partial escape at the end.
                            // +1 to compensate for the removal of / in parseArgs
                            int start = last.originalStart() + 1;
                            return new FullStringSuggestion(
                                    CommandArgParser.quote(last.parsedText(), arguments.charAt(start))
                                            + " " + CommandArgParser.escape(suggestion.getSuggestion()),
                                    start,
                                    arguments.length()
                            );
                        }
                        // Argument addition, use end of string as target
                        // Also add a space on the front to cause it to be a new argument
                        return new FullStringSuggestion(
                            " " + CommandArgParser.escape(suggestion.getSuggestion()),
                            arguments.length(),
                            arguments.length()
                        );
                    }

                    ExtractedArg arg = split.get(suggestion.getReplacedArgument());
                    // +1 to compensate for the removal of / in parseArgs
                    int start = arg.originalStart() + 1;
                    // Keep the user's quote style, otherwise the suggestion won't match what they typed
                    char firstChar = start < arguments.length() ? arguments.charAt(start) : ' ';
                    String escapedText = firstChar == '"' || firstChar == '\''
                        ? CommandArgParser.quote(suggestion.getSuggestion(), firstChar)
                        : CommandArgParser.escape(suggestion.getSuggestion());
                    return new FullStringSuggestion(escapedText, start, arg.originalEnd() + 1);
                }).toList();
        } catch (ConditionFailedException ignored) {
            // Ignore condition failures.
        }

        return Collections.emptyList();
    }

    /**
     * Get the command manager instance.
     *
     * @return the command manager
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

}