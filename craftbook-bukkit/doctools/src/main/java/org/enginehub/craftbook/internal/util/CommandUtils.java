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

package org.enginehub.craftbook.internal.util;

import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.craftbook.PlatformCommandManager;
import org.enginehub.craftbook.bukkit.mechanics.area.clipboard.AreaCommands;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.mechanics.headdrops.HeadDropsCommands;
import org.enginehub.craftbook.mechanics.signcopier.SignEditCommands;
import org.enginehub.piston.Command;
import org.enginehub.piston.config.TextConfig;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.util.HelpGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandUtils {

    private static final Pattern nameRegex = Pattern.compile("name = \"(.+?)\"");

    private static final Map<String, Command> commands = new HashMap<>();
    private static final PlatformCommandManager commandManager = new PlatformCommandManager();

    static {
        MechanicCommandRegistrar registrar = commandManager.getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
            "headdrops",
            List.of(),
            "CraftBook HeadDrops Commands",
            (commandManager1, registration) -> HeadDropsCommands.register(commandManager1, registration, null)
        );

        registrar.registerTopLevelWithSubCommands(
            "signedit",
            List.of("edsign", "signcopy"),
            "CraftBook SignCopier Commands",
            (commandManager, registration) -> SignEditCommands.register(commandManager, registration, null)
        );

        registrar.registerTopLevelWithSubCommands(
            "area",
            List.of("togglearea"),
            "CraftBook ToggleArea Commands",
            (commandManager, registration) -> AreaCommands.register(commandManager, registration, null)
        );

        commands.putAll(commandManager.getCommandManager().getAllCommands().collect(Collectors.toMap(Command::getName, command -> command)));
    }

    public static List<String> getAllCommandsIn(Class<?> commandClass) {
        Path sourceFile = Paths.get("craftbook-bukkit/src/main/java/" + commandClass.getName().replace('.', '/') + ".java");
        if (!Files.exists(sourceFile)) {
            throw new RuntimeException("Source not found for " + commandClass.getName() + ", looked at " + sourceFile.toAbsolutePath().toString());
        }

        List<String> commands = new ArrayList<>();

        try {
            final AtomicBoolean inCommand = new AtomicBoolean(false);
            Files.readAllLines(sourceFile).forEach(line -> {
                if (inCommand.get()) {
                    Matcher matcher = nameRegex.matcher(line);
                    if (matcher.find()) {
                        commands.add(matcher.group(1));
                    } else {
                        if (line.trim().equals(")")) {
                            inCommand.set(false);
                        }
                    }
                } else if (line.contains("@Command(")) {
                    inCommand.set(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return commands;
    }

    public static String dumpSection(String title, List<String> addCommandNames) {
        StringBuilder cmdOutput = new StringBuilder();

        cmdOutput.append(title).append("\n").append(repeatString("~", title.length())).append("\n");

        String prefix = ComponentRstRenderer.reduceToRst(TextConfig.commandPrefixValue());
        List<Command> commands = addCommandNames.stream().map(CommandUtils.commands::get).toList();

        cmdOutput.append(cmdsToPerms(commands, prefix));

        for (Command command : commands) {
            cmdOutput.append(writeCommandBlock(command, prefix, Stream.empty()));
            command.getParts().stream().filter(p -> p instanceof SubCommandPart)
                .flatMap(p -> ((SubCommandPart) p).getCommands().stream())
                .forEach(sc -> cmdOutput.append(writeCommandBlock(sc, prefix + command.getName() + " ", Stream.of(command))));
        }

        return cmdOutput.toString();
    }

    private static String cmdsToPerms(List<Command> cmds, String prefix) {
        StringBuilder permsOutput = new StringBuilder();
        cmds.forEach(c -> {
            permsOutput.append("    ").append(cmdToPerm(prefix, c)).append("\n");
            c.getParts().stream()
                .filter(part -> part instanceof SubCommandPart)
                .forEach(scp -> cmdsToPerms(
                    ((SubCommandPart) scp).getCommands()
                        .stream()
                        .sorted(Comparator.comparing(Command::getName))
                        .toList(),
                    prefix + c.getName() + " "));
        });
        return permsOutput.toString();
    }

    private static String cmdToPerm(String prefix, Command c) {
        Command.Condition cond = c.getCondition();
        String permissions = "";
        if (cond instanceof PermissionCondition && !((PermissionCondition) cond).getPermissions().isEmpty()) {
            permissions = ((PermissionCondition) cond).getPermissions()
                .stream()
                .map(con -> "``" + con + "``")
                .collect(Collectors.joining(", "));
        }
        return "``" + prefix + c.getName() + "``,\"" + permissions + "\"";
    }

    private static String writeCommandBlock(Command command, String prefix, Stream<Command> parents) {
        String name = prefix + command.getName();
        Map<String, String> entries = commandTableEntries(command, parents);

        StringBuilder cmdOutput = new StringBuilder();

        cmdOutput.append(".. raw:: html\n");
        cmdOutput.append("\n");
        cmdOutput.append("    <span id=\"command-").append(linkSafe(name)).append("\"></span>\n");
        cmdOutput.append("\n");
        cmdOutput.append(".. topic:: ``").append(name).append("``");
        if (!command.getAliases().isEmpty()) {
            cmdOutput.append("(or ").append(command.getAliases()
                .stream()
                .map(alias -> "``" + prefix + alias + "``")
                .collect(Collectors.joining(", "))).append(")\n");
        }
        cmdOutput.append("\n");
        cmdOutput.append("    :class: command-topic\n\n");
        CommandUtil.deprecationWarning(command).ifPresent(warning ->
            cmdOutput.append("    .. WARNING::\n" + "        ")
                .append(makeRstSafe(ComponentRstRenderer.reduceToRst(warning), "\n\n"))
                .append("\n")
        );
        cmdOutput.append(
            ".. csv-table::\n"
                + "  :widths: 8, 15\n"
        );
        cmdOutput.append("\n");
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            String rstSafe = makeRstSafe(entry.getValue(), "\n");
            cmdOutput.append(repeatString("    ", 2))
                .append(entry.getKey())
                .append(",")
                .append('"')
                .append(rstSafe)
                .append('"')
                .append("\n");
        }
        cmdOutput.append("\n");
        return cmdOutput.toString();
    }

    private static String makeRstSafe(String input, String lineJoiner) {
        return String.join(lineJoiner, input.trim()
            .replace("\"", "\\\"")
            .replace("\n", "\n" + repeatString("    ", 2))
            .split(","));
    }

    private static String linkSafe(String text) {
        return text.replace(" ", "-");
    }

    private static Map<String, String> commandTableEntries(Command command, Stream<Command> parents) {
        Component description = command.getDescription();
        Optional<Component> footer = CommandUtil.footerWithoutDeprecation(command);
        if (footer.isPresent()) {
            description = description.append(TextComponent.builder("\n\n").append(footer.get()));
        }

        Map<String, String> entries = new HashMap<>();
        entries.put("**Description**", ComponentRstRenderer.reduceToRst(description));

        Command.Condition cond = command.getCondition();
        if (cond instanceof PermissionCondition && !((PermissionCondition) cond).getPermissions().isEmpty()) {
            String perms = ((PermissionCondition) cond).getPermissions().stream().map(p -> "``" + p + "``").collect(Collectors.joining(", "));
            entries.put("**Permissions**", perms);
        }

        String usage = ComponentRstRenderer.reduceToRst(HelpGenerator.create(Stream.concat(parents, Stream.of(command)).toList()).getUsage());
        entries.put("**Usage**", "``" + usage + "``");

        // Part descriptions
        command.getParts().stream().filter(part -> !(part instanceof SubCommandPart))
            .forEach(it -> {
                String title = "\u2001\u2001``" + ComponentRstRenderer.reduceToRst(it.getTextRepresentation()) + "``";
                entries.put(title, ComponentRstRenderer.reduceToRst(it.getDescription()));
            });
        return entries;
    }

    private static String repeatString(String str, int length) {
        return String.valueOf(str).repeat(Math.max(0, length));
    }
}
