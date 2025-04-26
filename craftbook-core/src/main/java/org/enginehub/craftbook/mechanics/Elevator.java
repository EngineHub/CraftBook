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
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.jspecify.annotations.Nullable;

/**
 * The default elevator mechanism -- wall signs in a vertical column that teleport the player
 * vertically when triggered.
 */
public abstract class Elevator extends AbstractCraftBookMechanic {

    public Elevator(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected enum LiftType {
        UP("[Lift Up]"),
        DOWN("[Lift Down]"),
        BOTH("[Lift UpDown]"),
        RECV("[Lift]");

        private final String label;

        LiftType(String label) {
            this.label = label;
        }

        /**
         * Get the label of this lift type.
         *
         * @return The label
         */
        public String getLabel() {
            return this.label;
        }

        /**
         * Get the lift type from this label.
         *
         * @param label The label
         * @return The lift type, or null
         */
        public static @Nullable LiftType fromLabel(String label) {
            for (LiftType liftType : values()) {
                if (liftType.label.equalsIgnoreCase(label)) {
                    return liftType;
                }
            }

            return null;
        }
    }

    protected boolean elevatorAllowRedstone;
    protected int elevatorRedstoneRadius;
    protected boolean elevatorButtonEnabled;
    protected boolean elevatorLoop;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("allow-redstone", "Allows elevators to be triggered by redstone, which will move all players in a radius.");
        elevatorAllowRedstone = config.getBoolean("allow-redstone", false);

        config.setComment("redstone-player-search-radius", "The radius that elevators will look for players in when triggered by redstone.");
        elevatorRedstoneRadius = config.getInt("redstone-player-search-radius", 3);

        config.setComment("enable-buttons", "Allow elevators to be used by a button on the other side of the block.");
        elevatorButtonEnabled = config.getBoolean("enable-buttons", true);

        config.setComment("allow-looping", "Allows elevators to loop the world height. The heighest lift up will go to the next lift on the bottom of the world and vice versa.");
        elevatorLoop = config.getBoolean("allow-looping", false);
    }
}
