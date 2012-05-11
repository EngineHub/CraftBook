/*
 * CommandBook
 * Copyright (C) 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.jinglenote;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.bukkit.PlayerUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

public class JingleNoteComponent implements Listener {

    private JingleNoteManager jingleNoteManager;

    public void enable() {
        // Jingle note manager
        jingleNoteManager = new JingleNoteManager();
    }

    public void disable() {

        jingleNoteManager.stopAll();
    }

    /**
     * Get the jingle note manager.
     *
     * @return
     */
    public JingleNoteManager getJingleNoteManager() {

        return jingleNoteManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        MidiJingleSequencer sequencer;

        try {
            File file = new File(CircuitsPlugin.getInst().getDataFolder(), "intro.mid");
            if (file.exists()) {
                sequencer = new MidiJingleSequencer(file);
                getJingleNoteManager().play(event.getPlayer(), sequencer, 2000);
            }
        } catch (MidiUnavailableException e) {
            CircuitsPlugin.getInst().getLogger().log(Level.WARNING, "Failed to access MIDI: "
                    + e.getMessage());
        } catch (InvalidMidiDataException e) {
            CircuitsPlugin.getInst().getLogger().log(Level.WARNING, "Failed to read intro MIDI file: "
                    + e.getMessage());
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            CircuitsPlugin.getInst().getLogger().log(Level.WARNING, "Failed to read intro MIDI file: "
                    + e.getMessage());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        getJingleNoteManager().stop(event.getPlayer());
    }

    public class Commands {

        public void intro(CommandContext args, CommandSender sender) throws CommandException {

            Iterable<Player> targets;
            if (args.argsLength() == 0) {
                targets = PlayerUtil.matchPlayers(PlayerUtil.checkPlayer(sender));
            } else {
                targets = PlayerUtil.matchPlayers(sender, args.getString(0));
            }

            for (Player target : targets) {
                if (target != sender) {
                    CircuitsPlugin.getInst().hasPermission(sender, "commandbook.intro.other");
                    break;
                }
            }

            try {
                MidiJingleSequencer sequencer = new MidiJingleSequencer(
                        new File(CircuitsPlugin.getInst().getDataFolder(), "intro.mid"));
                for (Player player : targets) {
                    getJingleNoteManager().play(player, sequencer, 0);
                    player.sendMessage(ChatColor.YELLOW + "Playing intro.midi...");
                }
            } catch (MidiUnavailableException e) {
                throw new CommandException("Failed to access MIDI: "
                        + e.getMessage());
            } catch (InvalidMidiDataException e) {
                throw new CommandException("Failed to read intro MIDI file: "
                        + e.getMessage());
            } catch (FileNotFoundException e) {
                throw new CommandException("No intro.mid is available.");
            } catch (IOException e) {
                throw new CommandException("Failed to read intro MIDI file: "
                        + e.getMessage());
            }
        }

        @Command(aliases = {"midi", "play"},
                usage = "[-p player] [midi]", desc = "Play a MIDI file", flags = "p:",
                min = 0, max = 1)
        public void midi(CommandContext args, CommandSender sender) throws CommandException {

            Iterable<Player> targets;
            if (args.hasFlag('p')) {
                targets = PlayerUtil.matchPlayers(sender, args.getFlag('p'));
            } else {
                targets = PlayerUtil.matchPlayers(PlayerUtil.checkPlayer(sender));
            }

            if (args.argsLength() == 0) {
                for (Player target : targets) {
                    if (getJingleNoteManager().stop(target)) {
                        target.sendMessage(ChatColor.YELLOW + "All music stopped.");
                    }
                }
                return;
            }

            String filename = args.getString(0);

            if (!filename.matches("^[A-Za-z0-9 \\-_\\.~\\[\\]\\(\\$),]+$")) {
                throw new CommandException("Invalid filename specified!");
            }

            File[] trialPaths = {
                    new File(CircuitsPlugin.getInst().getDataFolder(), "midi/" + filename),
                    new File(CircuitsPlugin.getInst().getDataFolder(), "midi/" + filename + ".mid"),
                    new File(CircuitsPlugin.getInst().getDataFolder(), "midi/" + filename + ".midi"),
                    new File("midi", filename),
                    new File("midi", filename + ".mid"),
                    new File("midi", filename + ".midi"),
            };

            File file = null;

            for (File f : trialPaths) {
                if (f.exists()) {
                    file = f;
                    break;
                }
            }

            if (file == null) {
                throw new CommandException("The specified MIDI file was not found.");
            }

            try {
                MidiJingleSequencer sequencer = new MidiJingleSequencer(file);
                for (Player player : targets) {
                    getJingleNoteManager().play(player, sequencer, 0);
                    player.sendMessage(ChatColor.YELLOW + "Playing " + file.getName()
                            + "... Use '/midi' to stop.");
                }
            } catch (MidiUnavailableException e) {
                throw new CommandException("Failed to access MIDI: "
                        + e.getMessage());
            } catch (InvalidMidiDataException e) {
                throw new CommandException("Failed to read intro MIDI file: "
                        + e.getMessage());
            } catch (FileNotFoundException e) {
                throw new CommandException("The specified MIDI file was not found.");
            } catch (IOException e) {
                throw new CommandException("Failed to read intro MIDI file: "
                        + e.getMessage());
            }
        }
    }
}
