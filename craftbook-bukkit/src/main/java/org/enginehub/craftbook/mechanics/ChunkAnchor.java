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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

public class ChunkAnchor extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!event.getLine(1).equalsIgnoreCase("[chunk]")) {
            return;
        }

        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!lplayer.hasPermission("craftbook.chunkanchor.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                lplayer.printError(TranslatableComponent.of(
                    "craftbook.mechanisms.create-permission",
                    TextComponent.of(getMechanicType().getName())
                ));
            }
            SignUtil.cancelSignChange(event);
            return;
        }

        for (BlockState state : event.getBlock().getChunk().getTileEntities(SignUtil::isSign, false)) {
            if (state instanceof Sign s) {
                if (s.getLine(1).equals("[Chunk]")) {
                    lplayer.printError(TranslatableComponent.of("craftbook.chunkanchor.already-anchored"));
                    SignUtil.cancelSignChange(event);
                    return;
                }
            }
        }

        event.setLine(1, "[Chunk]");
        lplayer.printInfo(TranslatableComponent.of("craftbook.chunkanchor.create"));
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        updateChunkTicket(event.getChunk());
    }

    @EventHandler
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!useRedstone || event.isMinor() || !EventUtil.passesFilter(event)) {
            return;
        }

        Block block = event.getBlock();
        if (SignUtil.isSign(block)) {
            ChangedSign sign = CraftBookBukkitUtil.toChangedSign(block);

            if (!sign.getLine(1).equals("[Chunk]")) {
                return;
            }

            sign.setLine(3, event.isOn() ? "" : "OFF");
            sign.update(false);

            updateChunkTicket(event.getBlock().getChunk());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (SignUtil.isSign(event.getBlock())) {
            ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

            if (!sign.getLine(1).equals("[Chunk]")) {
                return;
            }

            Bukkit.getScheduler().runTask(
                CraftBookPlugin.inst(),
                () -> updateChunkTicket(event.getBlock().getChunk())
            );
        }
    }

    private void updateChunkTicket(Chunk chunk) {
        boolean shouldAnchor = false;

        for (BlockState state : chunk.getTileEntities(SignUtil::isSign, false)) {
            if (state == null) {
                continue;
            }
            if (state instanceof Sign sign) {
                if (sign.getLine(1).equals("[Chunk]")) {
                    if (!useRedstone || !sign.getLine(3).equals("OFF")) {
                        shouldAnchor = true;
                        break;
                    }
                }
            }
        }

        if (shouldAnchor) {
            chunk.addPluginChunkTicket(CraftBookPlugin.inst());
        } else {
            chunk.removePluginChunkTicket(CraftBookPlugin.inst());
        }
    }

    private boolean useRedstone;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("redstone-toggle", "Allow Chunk Anchors to be turned on and off with redstone.");
        useRedstone = config.getBoolean("redstone-toggle", true);
    }
}
