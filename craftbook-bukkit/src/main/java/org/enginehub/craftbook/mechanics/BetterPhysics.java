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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.EventUtil;

public class BetterPhysics extends AbstractCraftBookMechanic {

    protected static BetterPhysics instance;

    @Override
    public void enable() {
        instance = this;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        checkForPhysics(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        checkForPhysics(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockUpdate(BlockPhysicsEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        checkForPhysics(event.getBlock());
    }

    private static void checkForPhysics(Block block) {
        if (FallingLadders.isValid(block)) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new FallingLadders(block));
        }
    }

    private static class FallingLadders implements Runnable {
        private Block ladder;

        FallingLadders(Block ladder) {
            this.ladder = ladder;
        }

        public static boolean isValid(Block block) {
            return block.getType() == Material.LADDER && instance.ladders && block.getRelative(0, -1, 0).getType().isAir();
        }

        @Override
        public void run() {
            if (!isValid(ladder)) return;
            ladder.getWorld().spawnFallingBlock(ladder.getLocation().add(0.5, 0, 0.5), ladder.getBlockData());
            ladder.setType(Material.AIR, false);

            checkForPhysics(ladder.getRelative(BlockFace.UP));
        }
    }

    private boolean ladders;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("falling-ladders", "Enables BetterPhysics Falling Ladders.");
        ladders = config.getBoolean("falling-ladders", true);
    }
}