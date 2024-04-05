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

package org.enginehub.craftbook.util;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.world.item.ItemType;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemParser {
    private static final ParserContext ITEM_CONTEXT = new ParserContext();
    private static final Set<String> knownBadLines = new HashSet<>();

    private ItemParser() {
    }

    static {
        ITEM_CONTEXT.setTryLegacy(true);
        ITEM_CONTEXT.setRestricted(false);
    }

    public static BaseItem getItem(String line) {
        return getItem(line, false);
    }

    public static BaseItem getItem(String line, boolean wild) {
        if (line == null || line.trim().isEmpty() || knownBadLines.contains(line)) {
            return null;
        }

        ITEM_CONTEXT.setPreferringWildcard(wild);

        BaseItem baseItem = null;
        try {
            baseItem = WorldEdit.getInstance().getItemFactory().parseFromInput(line, ITEM_CONTEXT);
        } catch (InputParseException e) {
            knownBadLines.add(line);
        }

        return baseItem;
    }

    public static List<BaseItem> getItems(List<String> lines) {
        return getItems(lines, false);
    }

    public static List<BaseItem> getItems(List<String> lines, boolean wild) {
        return lines.stream()
            .map(line -> getItem(line, wild))
            .filter(Objects::nonNull)
            .toList();
    }

    public static String toMinifiedId(ItemType holder) {
        String output = holder.id();
        if (output.startsWith("minecraft:")) {
            output = output.substring(10);
        }
        return output;
    }

    public static String toMinifiedId(BaseItem holder) {
        String output = holder.toString();
        if (output.startsWith("minecraft:")) {
            output = output.substring(10);
        }
        return output;
    }
}
