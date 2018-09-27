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

package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.TernaryState;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This mechanism allow players to read bookshelves and get a random line from a file as as "book."
 *
 * @author sk89q
 */
public class Bookcase extends AbstractCraftBookMechanic {

    /**
     * Reads a book.
     *
     * @param player
     */
    public static void read(CraftBookPlayer player) {

        try {
            String text = getBookLine();

            if (text != null) {
                player.print("mech.bookcase.read-line");
                player.printRaw(text);
            } else
                player.printError("mech.bookcase.fail-line");
        } catch (Exception e) {
            player.printError("mech.bookcase.fail-file");
        }
    }

    @Override
    public boolean enable() {

        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "books.txt"), "books.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(CraftBookPlugin.inst().getDataFolder(),"books.txt")), "UTF-8"));
            Set<String> list = new LinkedHashSet<>();
            String l;
            while((l = reader.readLine()) != null)
                list.add(l);

            lines = list.toArray(new String[list.size()]);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null) try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void disable() {

        lines = null;
    }

    public static String[] lines;

    /**
     * Get a line from the book lines file.
     *
     * @return a line from the book lines file.
     *
     * @throws IOException if we have trouble with the "books.txt" configuration file.
     */
    private static String getBookLine() throws Exception {
        return lines[CraftBookPlugin.inst().getRandom().nextInt(lines.length)];
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock().getType() != Material.BOOKSHELF) return;

        if (!bookcaseReadWhenSneaking.doesPass(event.getPlayer().isSneaking())) return;

        if (!EventUtil.passesFilter(event))
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!player.hasPermission("craftbook.mech.bookshelf.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        if (bookcaseReadHoldingBlock || !player.isHoldingBlock())
            read(player);
    }

    private boolean bookcaseReadHoldingBlock;
    private TernaryState bookcaseReadWhenSneaking;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {


        config.setComment(path + "read-when-sneaking", "Enable reading while sneaking.");
        bookcaseReadWhenSneaking = TernaryState.getFromString(config.getString(path + "read-when-sneaking", "no"));

        config.setComment(path + "read-when-holding-block", "Allow bookshelves to work when the player is holding a block.");
        bookcaseReadHoldingBlock = config.getBoolean(path + "read-when-holding-block", false);
    }
}