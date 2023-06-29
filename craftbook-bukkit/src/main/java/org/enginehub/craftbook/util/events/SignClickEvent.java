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

package org.enginehub.craftbook.util.events;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;

import javax.annotation.Nonnull;

public class SignClickEvent extends PlayerInteractEvent {

    private static final HandlerList handlers = new HandlerList();

    private final ChangedSign sign;
    private final CraftBookPlayer player;

    public SignClickEvent(Player who, Action action, ItemStack item, Block clickedBlock, BlockFace clickedFace, EquipmentSlot equipmentSlot, Vector interactionPoint) {
        super(who, action, item, clickedBlock, clickedFace, equipmentSlot, interactionPoint);

        this.player = CraftBookPlugin.inst().wrapPlayer(who);
        this.sign = new ChangedSign(clickedBlock, null, this.player);
    }

    @Nonnull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Nonnull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ChangedSign getSign() {
        return this.sign;
    }

    public CraftBookPlayer getWrappedPlayer() {
        return this.player;
    }
}
