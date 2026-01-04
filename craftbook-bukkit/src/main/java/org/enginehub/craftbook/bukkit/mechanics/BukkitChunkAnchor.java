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

package org.enginehub.craftbook.bukkit.mechanics;

import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.events.SourcedBlockRedstoneEvent;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.ChunkAnchor;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.SignUtil;

public class BukkitChunkAnchor extends ChunkAnchor implements Listener {

    public BukkitChunkAnchor(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        String signLine1 = PlainTextComponentSerializer.plainText().serialize(event.line(1));
        if (!signLine1.equalsIgnoreCase("[chunk]")) {
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
                for (Side side : Side.values()) {
                    if (PlainTextComponentSerializer.plainText().serialize(s.getSide(side).line(1)).equals("[Chunk]")) {
                        lplayer.printError(TranslatableComponent.of("craftbook.chunkanchor.already-anchored"));
                        SignUtil.cancelSignChange(event);
                        return;
                    }
                }
            }
        }

        event.line(1, Component.text( "[Chunk]"));
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
            Sign bukktiSign = (Sign) block.getState(false);
            BukkitChangedSign sign = BukkitChangedSign.create(block, bukktiSign.getInteractableSideFor(event.getSource().getLocation()));

            String line1 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(1));
            if (!line1.equals("[Chunk]")) {
                return;
            }

            sign.setLine(3, Component.text(event.isOn() ? "" : "OFF"));
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
            Sign sign = (Sign) event.getBlock().getState(false);

            for (Side side : Side.values()) {
                if (!sign.getSide(side).getLine(1).equals("[Chunk]")) {
                    continue;
                }

                Bukkit.getRegionScheduler().execute(
                    CraftBookPlugin.inst(),
                    event.getBlock().getLocation(),
                    () -> updateChunkTicket(event.getBlock().getChunk())
                );
            }
        }
    }

    private void updateChunkTicket(Chunk chunk) {
        boolean shouldAnchor = false;

        for (BlockState state : chunk.getTileEntities(SignUtil::isSign, false)) {
            if (state == null) {
                continue;
            }
            if (state instanceof Sign sign) {
                for (Side side : Side.values()) {
                    if (sign.getSide(side).getLine(1).equals("[Chunk]")) {
                        if (!useRedstone || !sign.getSide(side).getLine(3).equals("OFF")) {
                            shouldAnchor = true;
                            break;
                        }
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
}
