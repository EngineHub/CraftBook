// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.util;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockSyntax {
    private static ParserContext BLOCK_CONTEXT = new ParserContext();

    private static Set<String> knownBadLines = new HashSet<>();

    static {
        BLOCK_CONTEXT.setPreferringWildcard(true);
        BLOCK_CONTEXT.setRestricted(false);
    }

    public static BaseBlock getBlock(String line) {
        return getBlock(line, false);
    }

    public static BaseBlock getBlock(String line, boolean wild) {
        if (line == null || line.trim().isEmpty() || knownBadLines.contains(line)) {
            return null;
        }

        BLOCK_CONTEXT.setPreferringWildcard(wild);

        BaseBlock blockState = null;
        try {
            blockState = WorldEdit.getInstance().getBlockFactory().parseFromInput(line, BLOCK_CONTEXT);
        } catch (InputParseException e) {
        }

        if (blockState == null) {
            String[] dataSplit = RegexUtil.COLON_PATTERN.split(line.replace("\\:", ":"), 2);
            Material material = Material.getMaterial(dataSplit[0], true);
            if (material != null) {
                int data = 0;
                if (dataSplit.length > 1) {
                    data = Integer.parseInt(dataSplit[1]);
                    if (data < 0 || data > 15) {
                        data = 0;
                    }
                }
                blockState = LegacyMapper.getInstance().getBlockFromLegacy(BukkitAdapter.asBlockType(material).getLegacyId(), data).toBaseBlock();
            }
            if (material == null) {
                CraftBookPlugin.logger().warning("Invalid block format: " + line);
                knownBadLines.add(line);
            }
        }
        return blockState;
    }

    public static List<BaseBlock> getBlocks(List<String> lines) {
        return getBlocks(lines, false);
    }

    public static List<BaseBlock> getBlocks(List<String> lines, boolean wild) {
        return lines.stream().map(line -> getBlock(line, wild)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static BlockData getBukkitBlock(String line) {
        return BukkitAdapter.adapt(getBlock(line));
    }

    public static String toMinifiedId(BlockType holder) {
        String output = holder.getId();
        if (output.startsWith("minecraft:")) {
            output = output.substring(10);
        }
        return output;
    }

    public static String toMinifiedId(BlockStateHolder holder) {
        String output = holder.getAsString();
        if (output.startsWith("minecraft:")) {
            output = output.substring(10);
        }
        return output;
    }
}
