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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.BetterPhysics;
import org.enginehub.craftbook.util.EventUtil;

public class BukkitBetterPhysics extends BetterPhysics implements Listener {

    public BukkitBetterPhysics(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        checkForPhysics(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        checkForPhysics(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockUpdate(BlockPhysicsEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        checkForPhysics(event.getBlock());
    }

    private void checkForPhysics(Block block) {
        if (canLadderFall(block)) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new FallingLadders(block));
        }
    }

    public boolean canLadderFall(Block block) {
        return ladders
            && block.getType() == Material.LADDER
            && block.getRelative(0, -1, 0).getType().isAir();
    }

    private class FallingLadders implements Runnable {
        private final Block ladder;

        FallingLadders(Block ladder) {
            this.ladder = ladder;
        }

        @Override
        public void run() {
            if (!canLadderFall(ladder)) {
                return;
            }

            ladder.getWorld().spawn(ladder.getLocation().add(0.5, 0, 0.5), FallingBlock.class, fallingBlock -> fallingBlock.setBlockData(ladder.getBlockData()));
            ladder.setType(Material.AIR, false);

            checkForPhysics(ladder.getRelative(BlockFace.UP));
        }
    }
}
