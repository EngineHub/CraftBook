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

import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.adapter.bukkit.TextAdapter;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.enginehub.craftbook.util.TextUtil;

public class BukkitCraftBookCommandSender extends BukkitCommandSender {

    public BukkitCraftBookCommandSender(WorldEditPlugin plugin, CommandSender sender) {
        super(plugin, sender);
    }

    @Override
    public void print(Component component) {
        TextAdapter.sendMessage(getSender(), TextUtil.format(component, getLocale()));
    }

    @Override
    public void printInfo(Component component) {
        // Override to change the colour.
        print(component.color(TextColor.YELLOW));
    }
}
