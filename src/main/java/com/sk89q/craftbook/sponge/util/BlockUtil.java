/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.PoweredProperty;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

public class BlockUtil {

    /**
     * Gets the relative direction of 'other' from 'base'.
     *
     * @param base The location of base.
     * @param other The location of other.
     * @return The relative direction
     */
    public static Direction getFacing(Location base, Location other) {
        for (Direction dir : Direction.values()) {
            if (base.getRelative(dir).getPosition().equals(other.getPosition()))
                return dir;
        }

        return null;
    }

    public static Optional<Integer> getBlockPowerLevel(Location<?> ... blocks) {
        for(Location<?> block : blocks) {
            Optional<Integer> optional = getBlockPowerLevel(block);
            if(optional.isPresent())
                return optional;
        }

        return Optional.empty();
    }

    public static Optional<Integer> getBlockPowerLevel(Location<?> block) {
        if (block.getBlock().get(Keys.POWER).isPresent()) {
            return Optional.of(block.getBlock().get(Keys.POWER).get());
        } else if (block.getBlock().get(Keys.POWERED).isPresent()) {
            return Optional.of(block.getBlock().get(Keys.POWERED).get() ? 15 : 0);
        } else if (block.getProperty(PoweredProperty.class).isPresent()) {
            return Optional.of(block.getProperty(PoweredProperty.class).get().getValue() ? 15 : 0);
        }

        return Optional.empty();
    }

    public static BlockState getBlockStateFromString(String rule) {
        BlockType blockType;

        Map<String, String> traitSpecifics = new HashMap<>();

        if(rule.contains("[") && rule.endsWith("]")) {
            String subRule = rule.substring(rule.indexOf('['), rule.length()-2);
            String[] parts = RegexUtil.COMMA_PATTERN.split(subRule);

            blockType = Sponge.getGame().getRegistry().getType(BlockType.class, rule.substring(0, rule.indexOf('['))).orElse(null);

            for(String part : parts) {
                String[] keyValue = RegexUtil.EQUALS_PATTERN.split(part);
                traitSpecifics.put(keyValue[0].toLowerCase(), keyValue[1]);
            }
        } else {
            blockType = Sponge.getGame().getRegistry().getType(BlockType.class, rule).orElse(null);
        }

        if(blockType == null) {
            return null;
        }

        BlockState state = blockType.getDefaultState();
        for(Map.Entry<String, String> entry : traitSpecifics.entrySet()) {
            state.getTrait(entry.getKey()).ifPresent((trait) -> state.withTrait(trait, entry.getValue()));
        }

        return state;
    }

    private static Direction[] directFaces = null;

    /**
     * Get faces that are directly touching the block.
     *
     * @return Faces that are directly touching the block.
     */
    public static Direction[] getDirectFaces() {
        if(directFaces == null)
            directFaces = new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        return directFaces;
    }

    public static List<Location<World>> getAdjacentExcept(Location<World> location, Direction ... directions) {
        List<Location<World>> locations = new ArrayList<>();

        for(Direction direction : getDirectFaces()) {
            boolean passes = true;
            for(Direction direction1 : directions) {
                if(direction1 == direction) {
                    passes = false;
                    break;
                }
            }
            if (passes) {
                locations.add(location.getRelative(direction));
            }
        }

        return locations;
    }

    /**
     * Gets whether or not the specified {@link BlockState} passes the {@link BlockFilter}s.
     *
     * @param filters The filters
     * @param state The state to test
     * @return If it passes
     */
    public static boolean doesStatePassFilters(Collection<BlockFilter> filters, BlockState state) {
        for(BlockFilter filter : filters)
            for(BlockState blockState : filter.getApplicableBlockStates())
                if(blockState.equals(state))
                    return true;
        return false;
    }

    /**
     * Gets the length of a line of blocks, with a maximum length.
     *
     * @param startBlock The starting location
     * @param testState The block that the line is made of
     * @param direction The direction of the line from the starting block
     * @param maximum The maximum length
     * @return The found length
     */
    public static int getLength(Location startBlock, BlockState testState, Direction direction, int maximum) {
        int length = 0;

        while(length < maximum) {
            if(startBlock.getBlock().equals(testState)) {
                length ++;
                startBlock = startBlock.getRelative(direction);
            } else {
                break;
            }
        }

        return length;
    }

    public static int getMinimumLength(Location firstBlock, Location secondBlock, BlockState testState, Direction direction, int maximum) {
        return Math.min(getLength(firstBlock, testState, direction, maximum), getLength(secondBlock, testState, direction, maximum));
    }
}
