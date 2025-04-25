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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.BlockParser;

import java.util.List;

/**
 * Snow fall mechanism. Builds up/tramples snow
 */
public abstract class Snow extends AbstractCraftBookMechanic {
    protected static final double SNOW_MELTING_TEMPERATURE = 0.05D;
    protected static final double SNOW_FORM_TEMPERATURE = 0.15D;

    public Snow(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean snowPiling;
    protected boolean trample;
    protected boolean partialTrample;
    protected boolean snowballPlacement;
    protected boolean slowdown;
    protected boolean dispersionMode;
    protected boolean pileHigh;
    protected int maxPileHeight;
    protected boolean jumpTrample;
    protected List<BaseBlock> dispersionReplacables;
    protected int dispersionTickSpeed;
    protected boolean freezeWater;
    protected boolean meltSunlight;
    protected boolean meltPartial;

    private static List<String> getDefaultReplacables() {
        return List.of(
            BlockTypes.DEAD_BUSH.id(),
            BlockTypes.SHORT_GRASS.id(),
            BlockTypes.FIRE.id(),
            BlockTypes.FERN.id());
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("piling", "Enables the piling feature of the Snow mechanic.");
        snowPiling = config.getBoolean("piling", false);

        config.setComment("trample", "Enables the trampling feature of the Snow mechanic.");
        trample = config.getBoolean("trample", false);

        config.setComment("partial-trample-only", "If trampling is enabled, only trample it down to the smallest snow.");
        partialTrample = config.getBoolean("partial-trample-only", false);

        config.setComment("jump-trample", "Require jumping to trample snow.");
        jumpTrample = config.getBoolean("jump-trample", false);

        config.setComment("place-snowball", "Allow snowballs to create snow when they land.");
        snowballPlacement = config.getBoolean("place-snowball", false);

        config.setComment("slowdown", "Slows down entities as they walk through thick snow.");
        slowdown = config.getBoolean("slowdown", false);

        config.setComment("dispersion", "Enable realistic snow dispersion.");
        dispersionMode = config.getBoolean("dispersion", false);

        config.setComment("high-piling", "Allow piling above the 1 block height.");
        pileHigh = config.getBoolean("high-piling", false);

        config.setComment("max-pile-height", "The maximum piling height of high piling snow.");
        maxPileHeight = config.getInt("max-pile-height", 3);

        config.setComment("replaceable-blocks", "A list of blocks that can be replaced by snow dispersion.");
        dispersionReplacables = BlockParser.getBlocks(config.getStringList("replaceable-blocks", getDefaultReplacables()), true);

        config.setComment("dispersion-tick-speed", "The speed at which dispersion actions are run");
        dispersionTickSpeed = config.getInt("dispersion-tick-speed", 20);

        config.setComment("freeze-water", "Should snow freeze water?");
        freezeWater = config.getBoolean("freeze-water", false);

        config.setComment("melt-in-sunlight", "Enables snow to melt in sunlight.");
        meltSunlight = config.getBoolean("melt-in-sunlight", false);

        config.setComment("partial-melt-only", "If melt in sunlight is enabled, only melt it down to the smallest snow similar to vanilla MC.");
        meltPartial = config.getBoolean("partial-melt-only", true);
    }
}
