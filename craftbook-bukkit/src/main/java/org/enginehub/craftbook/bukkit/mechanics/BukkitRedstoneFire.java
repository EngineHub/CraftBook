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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.RedstoneFire;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

/**
 * This mechanism allow players to toggle the fire on top of Netherrack or Soul Soil.
 */
public class BukkitRedstoneFire extends RedstoneFire implements Listener {

    public BukkitRedstoneFire(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.isMinor()) {
            return;
        }

        BlockType type = BukkitAdapter.asBlockType(event.getBlock().getType());

        if (!doesAffectBlock(type)) {
            return;
        }

        Block above = event.getBlock().getRelative(BlockFace.UP);
        Material aboveType = above.getType();

        if (event.isOn() && BlockUtil.isBlockReplacable(aboveType)) {
            above.setType(BukkitAdapter.adapt(getFireForBlock(type)));
        } else if (!event.isOn() && (aboveType == Material.FIRE || aboveType == Material.SOUL_FIRE)) {
            above.setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeftClick(PlayerInteractEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        BlockType type = BukkitAdapter.asBlockType(event.getClickedBlock().getType());
        if (!doesAffectBlock(type)) {
            return;
        }

        if (event.getBlockFace() == BlockFace.UP) {
            Block fire = event.getClickedBlock().getRelative(event.getBlockFace());
            Material fireMaterial = fire.getType();
            if ((fireMaterial == Material.FIRE || fireMaterial == Material.SOUL_FIRE) && fire.getRelative(BlockFace.DOWN).isBlockPowered()) {
                event.setCancelled(true);
            }
        }
    }
}
