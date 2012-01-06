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

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

import de.schlichtherle.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

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
    protected MechanismsPlugin plugin;
    
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
        if (!player.hasPermission("craftbook.mech.bookshelf.use")) {
            return;
        }
        
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
     * @return
     * @throws IOException
     */
    protected String getBookLine() throws IOException {
        RandomAccessFile file = new RandomAccessFile(
                new File(plugin.getLocalConfiguration().dataFolder, "books.txt"), "r");

        long len = file.length();
        byte[] data = new byte[500];

        for (int tries = 0; tries < 3; tries++) {
            int j = rand.nextInt((int)len);
            if (tries == 2) { // File is too small
                j = 0;
            }
            file.seek(j);
            file.read(data);

            StringBuilder buffer = new StringBuilder();
            boolean found = j == 0;
            byte last = 0;

            for (int i = 0; i < data.length; i++) {
                if (found) {
                    if (data[i] == 10 || data[i] == 13 || i >= len) {
                        if (last != 10 && last != 13) {
                            file.close();
                            return buffer.toString();
                        }
                    } else {
                        buffer.appendCodePoint(data[i]);
                    }
                } else if (data[i] == 10 || data[i] == 13) { // Line feeds
                    found = true;
                }

                last = data[i];
            }
        }

        file.close();
        return null;
    }
    
    /**
     * Raised when a block is right clicked.
     * 
     * @param event
     */
    @Override
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.getLocalConfiguration().bookcaseSettings.enable) return;
        
        Player player = event.getPlayer();
        read(plugin.wrap(player), plugin.getLocalConfiguration().bookcaseSettings.readLine);
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
        return false;   // this isn't a persistent mechanic, so the manager will never keep it around long enough to even check this.
    }

    public static class Factory extends AbstractMechanicFactory<Bookcase> {
        
        protected MechanismsPlugin plugin;
        
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
}
