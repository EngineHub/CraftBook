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

package org.enginehub.craftbook;

import com.sk89q.worldedit.entity.Player;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * A ChangedSign represents a single side of a sign block.
 */
public abstract class ChangedSign {

    public abstract void checkPlayerVariablePermissions(CraftBookPlayer player);

    public abstract int getX();

    public abstract int getY();

    public abstract int getZ();

    public abstract Component @Nullable [] getLines();

    public Component getLine(int index) throws IndexOutOfBoundsException {
        return this.getLine(index, null);
    }

    public abstract Component getLine(int index, @Nullable Player player) throws IndexOutOfBoundsException;

    public abstract Component getRawLine(int index) throws IndexOutOfBoundsException;

    public abstract void setLine(int index, Component line) throws IndexOutOfBoundsException;

    public abstract boolean update(boolean force);

    public abstract void setLines(Component[] lines);

    public abstract void setOldLines(Component[] oldLines);

    public abstract boolean hasChanged();

    public abstract void flushLines();

    public boolean updateSign(ChangedSign sign) {
        if (!equals(sign)) {
            flushLines();
            return true;
        }

        return false;
    }
}
