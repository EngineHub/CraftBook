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

package org.enginehub.craftbook.bukkit;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.adapter.bukkit.TextAdapter;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer;
import com.sk89q.worldedit.world.item.ItemType;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.util.TextUtil;

public final class BukkitCraftBookPlayer extends BukkitPlayer implements CraftBookPlayer {

    protected final CraftBookPlugin plugin;
    protected final Player player;

    public BukkitCraftBookPlayer(CraftBookPlugin plugin, Player player) {
        super(CraftBookPlugin.plugins.getWorldEdit(), player);

        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void print(Component component) {
        if (LegacyComponentSerializer.legacy().serialize(component).isBlank()) {
            return;
        }
        TextAdapter.sendMessage(player, TextUtil.format(component, getLocale()));
    }


    @Override
    public void printActionBar(Component component) {
        if (LegacyComponentSerializer.legacy().serialize(component).isBlank()) {
            return;
        }
        TextAdapter.sendActionBar(player, TextUtil.format(component, getLocale()));
    }

    @Override
    public void printInfo(Component component) {
        // Override to change the colour.
        print(component.color(TextColor.YELLOW));
    }

    @Override
    public boolean hasPermission(String perm) {
        return plugin.hasPermission(player, perm);
    }

    @Override
    public boolean isInsideVehicle() {
        return player.isInsideVehicle();
    }

    @Override
    public boolean isHoldingBlock() {
        ItemType mainitem = getItemInHand(HandSide.MAIN_HAND).getType();
        ItemType offitem = getItemInHand(HandSide.OFF_HAND).getType();
        return (mainitem.hasBlockType() && !mainitem.getBlockType().getMaterial().isAir())
            || (offitem.hasBlockType() && !offitem.getBlockType().getMaterial().isAir());
    }

    @Override
    public boolean isSneaking() {
        return player.isSneaking();
    }
}
