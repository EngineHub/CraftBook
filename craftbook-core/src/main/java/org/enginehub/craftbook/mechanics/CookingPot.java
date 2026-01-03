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
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.jspecify.annotations.Nullable;

public abstract class CookingPot extends AbstractCraftBookMechanic {

    public CookingPot(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected enum CookingPotFuel {
        COAL(ItemTypes.COAL, 40),
        CHARCOAL(ItemTypes.CHARCOAL, 40),
        COALBLOCK(ItemTypes.COAL_BLOCK, 360),
        BLAZEDUST(ItemTypes.BLAZE_POWDER, 250),
        BLAZE(ItemTypes.BLAZE_ROD, 500),
        LAVA(ItemTypes.LAVA_BUCKET, 6000);

        @SuppressWarnings("ImmutableEnumChecker")
        private final ItemType id;
        private final int fuelCount;

        CookingPotFuel(ItemType id, int fuelCount) {
            this.id = id;
            this.fuelCount = fuelCount;
        }

        public int getFuelCount() {
            return this.fuelCount;
        }

        public static @Nullable CookingPotFuel getByItemType(ItemType id) {
            for (CookingPotFuel in : values()) {
                if (in.id == id) {
                    return in;
                }
            }

            return null;
        }
    }

    protected boolean allowRedstone;
    protected boolean requireFuel;
    protected boolean allowSmelting;
    protected boolean openSign;
    protected int progressPerFuel;
    protected int fuelPerTick;
    protected boolean emptyCooldown;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("allow-redstone", "Allows for redstone to be used as a fuel source.");
        allowRedstone = config.getBoolean("allow-redstone", false);

        config.setComment("require-fuel", "Require fuel to cook.");
        requireFuel = config.getBoolean("require-fuel", true);

        config.setComment("allow-smelting", "Allows the cooking pot to cook ores and other smeltable items.");
        allowSmelting = config.getBoolean("allow-smelting", false);

        config.setComment("sign-click-open", "When enabled, right clicking the [Cook] sign will open the cooking pot.");
        openSign = config.getBoolean("sign-click-open", true);

        config.setComment("progress-per-fuel", "How much the current smelt progress increases per unit of fuel (line 4). Decreases fuel per cooked item and increases cooking speed.");
        progressPerFuel = config.getInt("progress-per-fuel", 2);

        config.setComment("fuel-per-tick", "How many fuel units (line 4) are used per tick. Increases cooking speed.");
        fuelPerTick = config.getInt("fuel-per-tick", 5);

        config.setComment("empty-cooldown", "Put the cooking pot in a \"low power\" mode while the chest is empty. Useful for low-performance machines or overloaded servers.");
        emptyCooldown = config.getBoolean("empty-cooldown", false);
    }
}
