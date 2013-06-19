// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.mech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * This mechanism allow players to read bookshelves and get a random line from a file as as "book."
 *
 * @author sk89q
 */
public class Bookcase extends AbstractMechanic {

    protected final CraftBookPlugin plugin = CraftBookPlugin.inst();

    /**
     * Reads a book.
     *
     * @param player
     * @param bookReadLine message to print to the user
     */
    public void read(LocalPlayer player, String bookReadLine) {

        try {
            String text = getBookLine();

            if (text != null) {
                player.print(bookReadLine);
                player.printRaw(text);
            } else
                player.printError("Failed to fetch a line from the books file.");
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

        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(),"books.txt")), "UTF-8"));
        lnr.skip(Long.MAX_VALUE);
        int lines = lnr.getLineNumber();
        lnr.close();
        int toRead = plugin.getRandom().nextInt(lines);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(),"books.txt")), "UTF-8"));
        String line;
        int passes = 0;
        while ((line = br.readLine()) != null) {
            passes++;
            if (passes >= toRead)
                break;
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

        if (!plugin.getConfiguration().bookcaseEnabled) return;
        if (event.getPlayer().isSneaking() != plugin.getConfiguration().bookcaseReadWhenSneaking) return;

        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());
        if (player.getHeldItemType() == 0 || !player.isHoldingBlock())
            read(player, plugin.getConfiguration().bookcaseReadLine);
    }

    public static class Factory extends AbstractMechanicFactory<Bookcase> {

        @Override
        public Bookcase detect(BlockWorldVector pt, LocalPlayer player) {

            if (pt.getWorld().getBlockType(pt) == BlockID.BOOKCASE && player.hasPermission("craftbook.mech.bookshelf.use")) return new Bookcase();

            return null;
        }
    }
}