/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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
package org.enginehub.craftbook.core.util;

import java.util.List;

/**
 * This class filters blocks based on a given String rule.
 *
 * <note>
 * Filters are made using the standard Minecraft block syntax.
 *
 * For example (Pre 1.13), minecraft:stone[variant=andesite]
 * </note>
 *
 * @param <T>
 */
public abstract class BlockFilter<T> {

    private static final String BASE_QUALIFIER = "minecraft:";

    private String rule;

    /**
     * Create a filter for blocks using the given string rule.
     *
     * @param rule The string rule
     */
    public BlockFilter(String rule) {
        this.setRule(rule);
    }

    public abstract List<T> getApplicableBlocks();

    private void setRule(String rule) {
        // Fully qualify rules.
        if (!rule.contains(":")) {
            rule = BASE_QUALIFIER + rule;
        }
        this.rule = rule;
    }

    public String getRule() {
        return this.rule;
    }

    @Override
    public String toString() {
        return this.rule;
    }
}
