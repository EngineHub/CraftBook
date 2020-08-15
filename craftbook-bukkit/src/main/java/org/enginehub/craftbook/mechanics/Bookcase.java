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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.TernaryState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
    public void enable() {

        CraftBookPlugin.inst().createDefaultConfiguration("books.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(CraftBookPlugin.inst().getDataFolder(), "books.txt")), "UTF-8"));
            Set<String> list = new LinkedHashSet<>();
            String l;
            while ((l = reader.readLine()) != null)
                list.add(l);

            lines = list.toArray(new String[list.size()]);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
     * @throws IOException if we have trouble with the "books.txt" configuration file.
     */
    private static String getBookLine() throws Exception {
        return lines[ThreadLocalRandom.current().nextInt(lines.length)];
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getClickedBlock().getType() != Material.BOOKSHELF) return;

        if (!bookcaseReadWhenSneaking.doesPass(event.getPlayer().isSneaking())) return;

        if (!EventUtil.passesFilter(event))
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.bookshelf.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        if (bookcaseReadHoldingBlock || !player.isHoldingBlock())
            read(player);
    }

    private boolean bookcaseReadHoldingBlock;
    private TernaryState bookcaseReadWhenSneaking;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {


        config.setComment("read-when-sneaking", "Enable reading while sneaking.");
        bookcaseReadWhenSneaking = TernaryState.getFromString(config.getString("read-when-sneaking", "no"));

        config.setComment("read-when-holding-block", "Allow bookshelves to work when the player is holding a block.");
        bookcaseReadHoldingBlock = config.getBoolean("read-when-holding-block", false);
    }
}