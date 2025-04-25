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

package org.enginehub.craftbook.mechanics.dispenser;

import com.sk89q.util.yaml.YAMLProcessor;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

public abstract class DispenserRecipes extends AbstractCraftBookMechanic {

    public DispenserRecipes(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean cannonEnable;
    protected boolean fanEnable;
    protected boolean vacuumEnable;
    protected boolean fireArrowsEnable;
    protected boolean snowShooterEnable;
    protected boolean xpShooterEnable;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("cannon-enable", "Enables Cannon Dispenser Recipe.");
        cannonEnable = config.getBoolean("cannon-enable", true);

        config.setComment("fan-enable", "Enables Fan Dispenser Recipe.");
        fanEnable = config.getBoolean("fan-enable", true);

        config.setComment("vacuum-enable", "Enables Vacuum Dispenser Recipe.");
        vacuumEnable = config.getBoolean("vacuum-enable", true);

        config.setComment("fire-arrows-enable", "Enables Fire Arrows Dispenser Recipe.");
        fireArrowsEnable = config.getBoolean("fire-arrows-enable", true);

        config.setComment("snow-shooter-enable", "Enables Snow Shooter Dispenser Recipe.");
        snowShooterEnable = config.getBoolean("snow-shooter-enable", true);

        config.setComment("xp-shooter-enable", "Enables XP Shooter Dispenser Recipe.");
        xpShooterEnable = config.getBoolean("xp-shooter-enable", true);
    }
}
