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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;

import javax.annotation.Nonnull;

/**
 * This mechanism allow players to toggle the fire on top of Netherrack or Soul Soil.
 */
public class RedstoneFire extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.isMinor()) {
            return;
        }

        Material type = event.getBlock().getType();

        if (!doesAffectBlock(event.getBlock().getType())) {
            return;
        }

        Block above = event.getBlock().getRelative(BlockFace.UP);
        Material aboveType = above.getType();

        if (event.isOn() && canReplaceWithFire(aboveType)) {
            above.setType(getFireForBlock(type));
        } else if (!event.isOn() && (aboveType == Material.FIRE || aboveType == Material.SOUL_FIRE)) {
            above.setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeftClick(PlayerInteractEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_BLOCK || !doesAffectBlock(event.getClickedBlock().getType())) {
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

    private boolean doesAffectBlock(Material type) {
        return (enableNetherrack && type == Material.NETHERRACK) || (enableSoulSoil && type == Material.SOUL_SOIL);
    }

    @Nonnull
    private Material getFireForBlock(Material type) {
        switch (type) {
            case NETHERRACK:
                return Material.FIRE;
            case SOUL_SOIL:
                return Material.SOUL_FIRE;
        }

        throw new RuntimeException("Tried to place fire on an unsupported block. Please report this error to CraftBook");
    }

    private static boolean canReplaceWithFire(Material type) {
        switch (type) {
            case SNOW:
            case GRASS:
            case VINE:
            case DEAD_BUSH:
            case AIR:
                return true;
            default:
                return false;
        }
    }

    private boolean enableNetherrack;
    private boolean enableSoulSoil;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("enable-netherrack", "Whether the mechanic should affect Netherrack.");
        enableNetherrack = config.getBoolean("enable-netherrack", true);

        config.setComment("enable-soul-soil", "Whether the mechanic should affect Soul Soil.");
        enableSoulSoil = config.getBoolean("enable-soul-soil", true);
    }
}
