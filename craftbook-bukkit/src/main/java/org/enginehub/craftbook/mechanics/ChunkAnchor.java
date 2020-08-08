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

package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBook;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkAnchor extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[chunk]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.chunk")) {
            if(CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        if(checkChunks) {
            for(BlockState state : event.getBlock().getChunk().getTileEntities()) {
                if(state instanceof Sign) {
                    Sign s = (Sign) state;
                    if(s.getLine(1).equalsIgnoreCase("[Chunk]")) {
                        lplayer.printError("mech.anchor.already-anchored");
                        SignUtil.cancelSign(event);
                        return;
                    }
                }
            }
        }

        event.setLine(1, "[Chunk]");
        lplayer.print("mech.anchor.create");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!EventUtil.passesFilter(event)) return;

        updateChunkTicket(event.getChunk());
    }

    @EventHandler
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!allowRedstone) return;
        Block block = event.getBlock();
        if (SignUtil.isSign(block)) {
            ChangedSign sign = CraftBookBukkitUtil.toChangedSign(block);

            if(!sign.getLine(1).equals("[Chunk]")) return;

            sign.setLine(3, event.getNewCurrent() > event.getOldCurrent() ? "on" : "off");
            sign.update(false);

            updateChunkTicket(event.getBlock().getChunk());
        }
    }

    private void updateChunkTicket(Chunk chunk) {
        boolean shouldAnchor = false;

        for(BlockState state : chunk.getTileEntities()) {
            if(state == null) continue;
            if(state instanceof Sign) {
                if(((Sign) state).getLine(1).equals("[Chunk]")) {
                    if (!allowRedstone || !((Sign) state).getLine(3).equalsIgnoreCase("off")) {
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

    private boolean allowRedstone;
    private boolean checkChunks;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("enable-redstone", "Enable toggling with redstone.");
        allowRedstone = config.getBoolean("enable-redstone", true);

        config.setComment("check-chunks", "On creation, check the chunk for already existing chunk anchors.");
        checkChunks = config.getBoolean("check-chunks", true);
    }
}