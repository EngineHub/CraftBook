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
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BlockParser {
    private static final ParserContext BLOCK_CONTEXT = new ParserContext();
    private static final Set<String> knownBadLines = new HashSet<>();

    private BlockParser() {
    }

    static {
        BLOCK_CONTEXT.setTryLegacy(true);
        BLOCK_CONTEXT.setRestricted(false);
    }

    public static @Nullable BaseBlock getBlock(@Nullable String line) {
        return getBlock(line, false);
    }

    public static @Nullable BaseBlock getBlock(@Nullable String line, boolean wild) {
        if (line == null || line.trim().isEmpty() || knownBadLines.contains(line)) {
            return null;
        }

        BLOCK_CONTEXT.setPreferringWildcard(wild);

        BaseBlock blockState = null;
        try {
            blockState = WorldEdit.getInstance().getBlockFactory().parseFromInput(line, BLOCK_CONTEXT);
        } catch (InputParseException e) {
            knownBadLines.add(line);
        }

        return blockState;
    }

    public static List<BaseBlock> getBlocks(List<String> lines) {
        return getBlocks(lines, false);
    }

    public static List<BaseBlock> getBlocks(List<String> lines, boolean wild) {
        return lines.stream()
            .map(line -> getBlock(line, wild))
            .filter(Objects::nonNull)
            .toList();
    }

    public static String toMinifiedId(BlockType holder) {
        String output = holder.id();
        if (output.startsWith("minecraft:")) {
            output = output.substring(10);
        }
        return output;
    }

    public static String toMinifiedId(BlockStateHolder<?> holder) {
        String output = holder.getAsString();
        if (output.startsWith("minecraft:")) {
            output = output.substring(10);
        }
        return output;
    }
}
