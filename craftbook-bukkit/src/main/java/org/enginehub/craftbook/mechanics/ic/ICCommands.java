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

package org.enginehub.craftbook.mechanics.ic;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import org.bukkit.ChatColor;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@CommandContainer
public class ICCommands {

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration) {
        registration.register(
                commandManager,
                ICCommandsRegistration.builder(),
                new ICCommands()
        );
    }

    public ICCommands() {

    }

    @Command(name = "info", aliases = {"doc", "docs", "help", "man"}, desc = "Documentation on CraftBook IC's")
    public void info(Actor actor, @Arg(desc = "The IC ID") String ic) {
        ICDocsParser.generateICDocs(actor, ic);
    }

    @Command(name = "list", desc = "List available IC's")
    public void listCmd(Actor actor,
            @ArgFlag(name = 'p', desc = "The page", def = "1") int page
    ) {
        String[] lines = ICManager.inst().generateICText(actor, null, null);
        int pages = (lines.length - 1) / 9 + 1;

        if (page < 1 || page >= pages) {
            actor.printError("Invalid page \"" + page + "\"");
            return;
        }

        actor.print(ChatColor.BLUE + "  ");
        actor.print(ChatColor.BLUE + "CraftBook ICs (Page " + page + " of " + pages + "):");

        for (int i = (page - 1) * 9; i < lines.length && i < page * 9; i++) {
            actor.print(lines[i]);
        }
    }

    @Command(name = "search", desc = "Search available IC's with names")
    public void searchCmd(Actor actor,
            @Arg(desc = "The search term") String term,
            @ArgFlag(name = 'p', desc = "The page", def = "1") int page
    ) {
        String[] lines = ICManager.inst().generateICText(actor, term, null);
        int pages = (lines.length - 1) / 9 + 1;

        if (page < 1 || page >= pages) {
            actor.printError("Invalid page \"" + page + "\"");
            return;
        }

        actor.print(ChatColor.BLUE + "  ");
        actor.print(ChatColor.BLUE + "CraftBook ICs \"" + term + "\" (Page " + page + " of " + pages + "):");

        for (int i = (page - 1) * 9; i < lines.length && i < page * 9; i++) {
            actor.print(lines[i]);
        }
    }

    @Command(name = "midis", aliases = {"midilist"}, desc = "List MIDI's available for Melody IC")
    public void midis(Actor actor, @ArgFlag(name = 'p', desc = "Page number", def = "1") int page) {
        List<String> lines = new ArrayList<>();

        FilenameFilter fnf = (dir, name) -> name.endsWith("mid") || name.endsWith(".midi");
        for (File f : ICManager.inst().getMidiFolder().listFiles(fnf)) {
            lines.add(f.getName().replace(".midi", "").replace(".mid", ""));
        }
        lines.sort(Comparator.naturalOrder());
        int pages = (lines.size() - 1) / 9 + 1;

        if (page < 1 || page >= pages) {
            actor.printError("Invalid page \"" + page + "\"");
            return;
        }

        actor.print(ChatColor.BLUE + "  ");
        actor.print(ChatColor.BLUE + "CraftBook MIDIs (Page " + page + " of " + pages + "):");

        for (int i = (page - 1) * 9; i < lines.size() && i < page * 9; i++) {
            actor.print(ChatColor.GREEN + lines.get(i));
        }
    }

    @Command(name = "fireworks", desc = "List Fireworks available for PFD IC")
    public void fireworks(Actor actor, @ArgFlag(name = 'p', desc = "Page number", def = "1") int page) {
        List<String> lines = new ArrayList<>();

        FilenameFilter fnf = (dir, name) -> name.endsWith(".fwk") || name.endsWith(".txt");
        for (File f : ICManager.inst().getFireworkFolder().listFiles(fnf)) {
            lines.add(f.getName().replace(".txt", "").replace(".fwk", ""));
        }
        lines.sort(String::compareTo);
        int pages = (lines.size() - 1) / 9 + 1;

        if (page < 1 || page >= pages) {
            actor.printError("Invalid page \"" + page + "\"");
            return;
        }

        actor.print(ChatColor.BLUE + "  ");
        actor.print(ChatColor.BLUE + "CraftBook Firework Displays (Page " + page + " of " + pages + "):");

        for (int i = (page - 1) * 9; i < lines.size() && i < page * 9; i++) {
            actor.print(ChatColor.GREEN + lines.get(i));
        }
    }
}
