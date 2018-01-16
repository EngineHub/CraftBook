/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.util;

import com.sk89q.craftbook.core.util.BlockFilter;
import com.sk89q.craftbook.core.util.RegexUtil;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpongeBlockFilter extends BlockFilter<BlockState> {

    private List<BlockState> cache;

    /**
     * Create a generic filter for this BlockType.
     *
     * @param blockType The {@link BlockType}
     */
    public SpongeBlockFilter(BlockType blockType) {
        super(blockType.getName());
    }

    public SpongeBlockFilter(BlockState blockState) {
        super(blockState.toString());
    }

    public SpongeBlockFilter(String rule) {
        super(rule);
    }

    public List<BlockState> getApplicableBlocks() {
        if(cache == null) {
            //Enumerate the cache.
            cache = new ArrayList<>();

            BlockType blockType;

            Map<String, String> traitSpecifics = new HashMap<>();

            if(getRule().contains("[") && getRule().endsWith("]")) {
                String subRule = getRule().substring(getRule().indexOf('['), getRule().length()-2);
                String[] parts = RegexUtil.COMMA_PATTERN.split(subRule);

                blockType = Sponge.getGame().getRegistry().getType(BlockType.class, getRule().substring(0, getRule().indexOf('['))).orElse(null);

                for(String part : parts) {
                    String[] keyValue = RegexUtil.EQUALS_PATTERN.split(part);
                    traitSpecifics.put(keyValue[0].toLowerCase(), keyValue[1]);
                }
            } else {
                blockType = Sponge.getGame().getRegistry().getType(BlockType.class, getRule()).orElse(null);
            }

            if(blockType == null) {
                CraftBookPlugin.spongeInst().getLogger().warn("Missing type for filter rule: " + getRule());
                return cache;
            }

            int[] counter = new int[blockType.getTraits().size()];

            if(counter.length != 0) {
                while (true) {
                    BlockState state = blockType.getDefaultState();
                    List<BlockTrait<?>> blockTraits = new ArrayList<>(state.getTraits());
                    for (int i = 0; i < counter.length; i++) {
                        BlockTrait<?> trait = blockTraits.get(i);
                        if(traitSpecifics.containsKey(trait.getName().toLowerCase()))
                            state = state.withTrait(trait, traitSpecifics.get(trait.getName().toLowerCase())).orElse(null);
                        else {
                            ArrayList<?> possibleValues = new ArrayList<>(trait.getPossibleValues());
                            if (counter[i] >= possibleValues.size()) {
                                counter[i] = 0;
                                if (i + 1 >= counter.length)
                                    return cache;
                                counter[i + 1]++;
                            }
                            state = state.withTrait(trait, possibleValues.get(counter[i])).get();
                        }
                    }

                    if(state != null) {
                        cache.add(state);
                    } else {
                        CraftBookPlugin.spongeInst().getLogger().warn("A state was null when it shouldn't have been. Are you sure '" + getRule() + "' is correct?");
                    }

                    counter[0] += 1;
                }
            } else {
                cache.add(blockType.getDefaultState());
            }
        }

        return cache;
    }
}
