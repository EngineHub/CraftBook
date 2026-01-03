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

package org.enginehub.craftbook.mechanics.piston;

import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Represents the different types of BetterPistons.
 */
public enum PistonType {
    CRUSH("[Crush]", Set.of(BlockTypes.PISTON, BlockTypes.STICKY_PISTON)),
    SUPER_STICKY("[SuperSticky]", Set.of(BlockTypes.STICKY_PISTON)),
    BOUNCE("[Bounce]", Set.of(BlockTypes.PISTON)),
    SUPER_PUSH("[SuperPush]", Set.of(BlockTypes.PISTON, BlockTypes.STICKY_PISTON));

    private final String signText;
    @SuppressWarnings("ImmutableEnumChecker")
    private final Set<BlockType> allowedBlocks;

    PistonType(String signText, Set<BlockType> allowedBlocks) {
        this.signText = signText;
        this.allowedBlocks = allowedBlocks;
    }

    /**
     * Gets the text on a sign that refers to this piston type.
     *
     * @return The sign text
     */
    public String getSignText() {
        return this.signText;
    }

    /**
     * Gets the allowed base blocks for this mechanic.
     *
     * @return The allowed blocks
     */
    public Set<BlockType> getAllowedBlocks() {
        return this.allowedBlocks;
    }

    /**
     * Get the BetterPiston type based on this sign text.
     *
     * @param line The line from the sign
     * @return The type, if any
     */
    public static @Nullable PistonType getFromSign(String line) {
        return switch (line) {
            case "[Crush]" -> CRUSH;
            case "[SuperSticky]" -> SUPER_STICKY;
            case "[Bounce]" -> BOUNCE;
            case "[SuperPush]" -> SUPER_PUSH;
            default -> null;
        };
    }
}
