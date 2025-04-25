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

package org.enginehub.craftbook.mechanics.signcopier;

import com.google.common.collect.Maps;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.kyori.adventure.text.Component;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.ItemParser;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class SignCopier extends AbstractCraftBookMechanic {
    protected final Map<UUID, SignData<?>> signs = Maps.newHashMap();

    public SignCopier(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    /**
     * Gets whether this user has copied a sign.
     *
     * @param uuid The user
     * @return If they have copied a sign
     */
    public boolean hasSign(UUID uuid) {
        return signs.containsKey(uuid);
    }

    /**
     * Get the data of the copied sign for the user.
     *
     * @see SignData
     *
     * @param uuid The user's UUID
     * @return The sign data, or null
     */
    public @Nullable SignData<?> getSignData(UUID uuid) {
        return signs.get(uuid);
    }

    /**
     * Sets the line at the line number for the given user to the given line value.
     *
     * @param uuid The user
     * @param lineNumber The line number (0-3)
     * @param line The new line value
     */
    public void setSignLine(UUID uuid, int lineNumber, Component line) {
        signs.get(uuid).lines.set(lineNumber, line);
    }

    /**
     * Clears the copied sign of the user.
     *
     * @param uuid The user
     */
    public void clearSign(UUID uuid) {
        signs.remove(uuid);
    }

    /**
     * Stores data about the copied sign.
     */
    public record SignData<T>(List<Component> lines, boolean glowing, @Nullable T color) {
    }

    protected ItemType item;
    protected boolean copyColor;
    protected boolean copyGlowing;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("item", "The item for the sign copy tool.");
        item = ItemParser.getItem(config.getString("item", ItemTypes.FLINT.id())).getType();

        config.setComment("copy-color", "If the sign copier should also copy the dyed color of the sign.");
        copyColor = config.getBoolean("copy-color", true);

        config.setComment("copy-glowing", "If the sign copier should also copy the glowing status of the sign.");
        copyGlowing = config.getBoolean("copy-glowing", true);
    }
}
