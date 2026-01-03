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

package org.enginehub.craftbook.mechanics.headdrops;

import com.sk89q.util.yaml.YAMLProcessor;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

public class HeadDrops extends AbstractCraftBookMechanic {
    public HeadDrops(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean enableMobs;
    protected boolean enablePlayers;
    protected boolean playerKillsOnly;
    protected boolean overrideNatural;
    protected double dropRate;
    protected double lootingModifier;
    protected boolean nameOnClick;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("drop-mob-heads", "Whether mobs should drop their heads when killed.");
        enableMobs = config.getBoolean("drop-mob-heads", true);

        config.setComment("drop-player-heads", "Whether players should drop their heads when killed.");
        enablePlayers = config.getBoolean("drop-player-heads", true);

        config.setComment("require-player-killer", "Only drop heads when killed by a player. (Allows requiring permission)");
        playerKillsOnly = config.getBoolean("require-player-killer", true);

        config.setComment("override-natural-head-drops", "Override natural head drops, this will cause natural head drops to use the chances provided by CraftBook. (Eg, Wither Skeleton Heads)");
        overrideNatural = config.getBoolean("override-natural-head-drops", false);

        config.setComment("drop-rate", "A value between 1 and 0 which dictates the global chance of heads being dropped. This can be overridden per-entity type.");
        dropRate = config.getDouble("drop-rate", 0.05);

        config.setComment("looting-rate-modifier", "This amount is added to the chance for every looting level on an item. Eg, a chance of 0.05(5%) and a looting mod of 0.05(5%) on a looting 3 sword, would give a 0.20 chance (20%).");
        lootingModifier = config.getDouble("looting-rate-modifier", 0.05);

        config.setComment("show-name-right-click", "When enabled, right clicking a placed head will say the owner of the head.");
        nameOnClick = config.getBoolean("show-name-right-click", true);
    }
}
