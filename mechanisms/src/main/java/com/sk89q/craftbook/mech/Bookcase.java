// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.mech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Random;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * This mechanism allow players to read bookshelves and get a random line
 * from a file as as "book."
 *
 * @author sk89q
 */
public class Bookcase extends AbstractMechanic {

    /**
     * Used for picking random lines.
     */
    protected static Random rand = new Random();

    /**
     * Configuration.
     */
    protected final MechanismsPlugin plugin;

    /**
     * Construct a bookcase for a location.
     *
     * @param pt
     * @param plugin
     */
    public Bookcase(BlockWorldVector pt, MechanismsPlugin plugin) {

        super();
        this.plugin = plugin;
    }

    /**
     * Reads a book.
     *
     * @param player
     * @param bookReadLine message to print to the user
     */
    public void read(LocalPlayer player, String bookReadLine) {

        if (!player.hasPermission("craftbook.mech.bookshelf.use")) return;

        try {
            String text = getBookLine();

            if (text != null) {
                player.print(bookReadLine);
                player.printRaw(text);
            } else {
                player.printError("Failed to fetch a line from the books file.");
            }
        } catch (IOException e) {
            player.printError("Failed to read the books file.");
        }
    }

    /**
     * Get a line from the book lines file.
     *
     * @return a line from the book lines file.
     *
     * @throws IOException if we have trouble with the "books.txt" configuration file.
     */
    protected String getBookLine() throws IOException {

        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(plugin.getLocalConfiguration()
                .dataFolder, "books.txt")));
        lnr.skip(Long.MAX_VALUE);
        int lines = lnr.getLineNumber();
        lnr.close();
        int toRead = new Random().nextInt(lines);
        BufferedReader br = new BufferedReader(new FileReader(new File(plugin.getLocalConfiguration().dataFolder,
                "books.txt")));
        String line;
        int passes = 0;
        while ((line = br.readLine()) != null) {
            passes++;
            if (passes >= toRead) break;
        }
        br.close();
        return line;
    }

    /**
     * Raised when a block is right clicked.
     *
     * @param event
     */
    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getLocalConfiguration().bookcaseSettings.enable) return;

        LocalPlayer player = plugin.wrap(event.getPlayer());
        if (player.getTypeInHand() == 0 || !player.isHoldingBlock())
            read(player, plugin.getLocalConfiguration().bookcaseSettings.readLine);
    }

    /**
     * Unload this bookcase.
     */
    @Override
    public void unload() {

    }

    /**
     * Check if this bookcase is still active.
     */
    @Override
    public boolean isActive() {

        return false;   // this isn't a persistent mechanic, so the manager will never keep it around long enough to
        // even check this.
    }

    public static class Factory extends AbstractMechanicFactory<Bookcase> {

        protected final MechanismsPlugin plugin;

        public Factory(MechanismsPlugin plugin) {

            this.plugin = plugin;
        }

        @Override
        public Bookcase detect(BlockWorldVector pt) {

            if (pt.getWorld().getBlockType(pt) == BlockID.BOOKCASE) {
                return new Bookcase(pt, plugin);
            }

            return null;
        }

    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {

    }
}