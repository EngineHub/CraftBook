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

import com.google.common.collect.ImmutableList;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
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
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.TernaryState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This mechanism allow players to read bookshelves and get a random line from a file as as "book."
 */
public class ReadableBookshelf extends AbstractCraftBookMechanic {

    public List<String> lines;

    @Override
    public void enable() throws MechanicInitializationException {
        CraftBookPlugin.inst().createDefaultConfiguration("books.txt");

        try {
            List<String> lines = Files.readAllLines(
                CraftBook.getInstance().getPlatform().getWorkingDirectory().resolve("books.txt"),
                StandardCharsets.UTF_8
            );
            this.lines = ImmutableList.copyOf(lines);
            if (this.lines.isEmpty()) {
                throw new MechanicInitializationException(
                    getMechanicType(),
                    TranslatableComponent.of("craftbook.readablebookshelf.empty")
                );
            }
        } catch (IOException e) {
            throw new MechanicInitializationException(
                getMechanicType(),
                TranslatableComponent.of("craftbook.readablebookshelf.failed-to-read"),
                e
            );
        }
    }

    @Override
    public void disable() {
        this.lines = null;
    }

    /**
     * Causes the player to read a line from the book.
     *
     * @param player The player
     */
    public void read(CraftBookPlayer player) {
        player.printInfo(TextComponent.empty()
            .append(TranslatableComponent.of("craftbook.readablebookshelf.read"))
            .append(TextComponent.newline())
            .append(getBookLine())
        );
    }

    /**
     * Get a line from the book lines file.
     *
     * @return a line from the book lines file.
     */
    private TextComponent getBookLine() {
        return TextComponent.of(lines.get(ThreadLocalRandom.current().nextInt(lines.size())), TextColor.WHITE);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
            || event.getHand() != EquipmentSlot.HAND
            || event.getClickedBlock() == null
            || event.getClickedBlock().getType() != Material.BOOKSHELF
            || !allowSneaking.doesPass(event.getPlayer().isSneaking())
            || !EventUtil.passesFilter(event)) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.readablebookshelf.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("mech.use-permission");
            }
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("area.use-permissions");
            }
            return;
        }

        if (allowHoldingBlock || !player.isHoldingBlock()) {
            read(player);
        }
    }

    private boolean allowHoldingBlock;
    private TernaryState allowSneaking;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("allow-sneaking", "Enable reading while sneaking.");
        allowSneaking = TernaryState.parseTernaryState(config.getString("allow-sneaking", TernaryState.FALSE.toString()));

        config.setComment("allow-holding-block", "Allow bookshelves to work when the player is holding a block.");
        allowHoldingBlock = config.getBoolean("allow-holding-block", false);
    }
}
