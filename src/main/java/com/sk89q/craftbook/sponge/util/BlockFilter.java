package com.sk89q.craftbook.sponge.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.ArrayList;
import java.util.List;

public class BlockFilter {

    private String rule;

    private List<BlockState> cache;

    public BlockFilter(String rule) {
        this.rule = rule;
    }

    public List<BlockState> getApplicableBlockStates() {
        if(cache == null) {
            //Enumerate the cache.
            cache = new ArrayList<>();

            if(rule.contains("[")) {
                //TODO
            } else {
                BlockType blockType = Sponge.getGame().getRegistry().getType(BlockType.class, rule).orElse(null);
                if(blockType == null) {
                    return cache;
                }

                int[] counter = new int[blockType.getTraits().size()];

                if(counter.length != 0) {
                    while (true) {
                        BlockState state = blockType.getDefaultState();
                        List<BlockTrait> blockTraits = new ArrayList<>(state.getTraits());
                        for (int i = 0; i < counter.length; i++) {
                            BlockTrait trait = blockTraits.get(i);
                            List<?> possibleValues = new ArrayList<>(trait.getPossibleValues());
                            if (counter[i] >= possibleValues.size()) {
                                counter[i] = 0;
                                if (i + 1 >= counter.length)
                                    return cache;
                                counter[i + 1]++;
                            }
                            state = state.withTrait(trait, possibleValues.get(counter[i])).get();
                        }

                        cache.add(state);

                        counter[0] += 1;
                    }
                } else {
                    cache.add(blockType.getDefaultState());
                }
            }
        }

        return cache;
    }

    public String getRule() {
        return this.rule;
    }

    @Override
    public String toString() {
        return this.rule;
    }
}
