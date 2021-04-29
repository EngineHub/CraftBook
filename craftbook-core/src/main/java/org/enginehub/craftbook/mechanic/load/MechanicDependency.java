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

package org.enginehub.craftbook.mechanic.load;

import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.mechanic.MechanicType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A load dependency on another CraftBook mechanic.
 */
public class MechanicDependency implements LoadDependency {

    private final MechanicType<?> mechanicType;
    private final boolean optional;

    public MechanicDependency(MechanicType<?> mechanicType) {
        this(mechanicType, false);
    }

    public MechanicDependency(MechanicType<?> mechanicType, boolean optional) {
        checkNotNull(mechanicType);

        this.mechanicType = mechanicType;
        this.optional = optional;
    }

    /**
     * Gets the {@link MechanicType} that is being depended upon.
     *
     * @return The mechanic type
     */
    public MechanicType<?> getMechanicType() {
        return this.mechanicType;
    }

    @Override
    public String getDependencyId() {
        return this.mechanicType.getId();
    }

    @Override
    public boolean isOptional() {
        return this.optional;
    }

    @Override
    public boolean isMet() {
        return CraftBook.getInstance().getPlatform().getMechanicManager().isMechanicEnabled(this.mechanicType);
    }
}
