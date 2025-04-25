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

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.BlockParser;

import java.util.List;

public class BetterPistons extends AbstractCraftBookMechanic {

    public BetterPistons(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    /**
     * Checks if the given {@link PistonType} is enabled by the user.
     *
     * @param type The piston type
     * @return Whether it's enabled
     */
    public boolean isEnabled(PistonType type) {
        return switch (type) {
            case CRUSH -> enableCrush;
            case SUPER_STICKY -> enableSuperSticky;
            case BOUNCE -> enableBounce;
            case SUPER_PUSH -> enableSuperPush;
        };
    }

    // Crush
    protected boolean enableCrush;
    protected boolean crushInstaKill;
    protected List<BaseBlock> crushBlockBlacklist;

    // Push / Sticky
    protected boolean enableSuperPush;
    protected boolean enableSuperSticky;
    protected List<BaseBlock> movementBlacklist;
    protected int maxDistance;

    // Bounce
    protected boolean enableBounce;
    protected List<BaseBlock> bounceBlockBlacklist;
    protected double maxBounceVelocity;

    public static List<String> getDefaultBlacklist() {
        return List.of(
            BlockTypes.OBSIDIAN.id(),
            BlockTypes.BEDROCK.id(),
            BlockTypes.NETHER_PORTAL.id(),
            BlockTypes.END_PORTAL.id(),
            BlockTypes.END_PORTAL_FRAME.id(),
            BlockTypes.END_GATEWAY.id()
        );
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("enable-crush", "Enables the Crush mechanic.");
        enableCrush = config.getBoolean("enable-crush", true);

        config.setComment("crush-kills-mobs", "Causes Crush to kill mobs as well as break blocks. This includes players.");
        crushInstaKill = config.getBoolean("crush-kills-mobs", false);

        config.setComment("crush-block-blacklist", "A list of blocks that the Crush piston cannot break.");
        crushBlockBlacklist = BlockParser.getBlocks(config.getStringList("crush-block-blacklist", getDefaultBlacklist()), true);

        config.setComment("enable-super-sticky", "Enables the SuperSticky mechanic.");
        enableSuperSticky = config.getBoolean("enable-super-sticky", true);

        config.setComment("enable-super-push", "Enables the SuperPush mechanic.");
        enableSuperPush = config.getBoolean("enable-super-push", true);

        config.setComment("movement-blacklist", "A list of blocks that the movement related BetterPistons can not interact with.");
        movementBlacklist = BlockParser.getBlocks(config.getStringList("movement-blacklist", getDefaultBlacklist()), true);

        config.setComment("enable-bounce", "Enables the Bounce mechanic.");
        enableBounce = config.getBoolean("enable-bounce", true);

        config.setComment("bounce-blacklist", "A list of blocks that the Bounce piston can not bounce.");
        bounceBlockBlacklist = BlockParser.getBlocks(config.getStringList("bounce-blacklist", getDefaultBlacklist()), true);

        config.setComment("max-distance", "The maximum distance a BetterPiston can interact with blocks from.");
        maxDistance = config.getInt("max-distance", 12);

        config.setComment("bounce-max-velocity", "The maximum velocity bounce pistons can use.");
        maxBounceVelocity = config.getDouble("bounce-max-velocity", 5.0);
    }
}
