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

package com.sk89q.craftbook.util.exceptions;

import com.sk89q.craftbook.core.mechanic.MechanicType;

/**
 * This exception is thrown when a CraftBook mechanism fails to initialize.
 */
public class MechanicInitializationException extends CraftbookException {

    private final MechanicType<?> mechanicType;

    public MechanicInitializationException(MechanicType<?> mechanicType, String message) {
        super(message);

        this.mechanicType = mechanicType;
    }

    public MechanicInitializationException(MechanicType<?> mechanicType, String message, Throwable cause) {
        super(message, cause);

        this.mechanicType = mechanicType;
    }

    /**
     * Get the mechanic type that failed to initialize.
     *
     * @return The mechanic type
     */
    public MechanicType<?> getMechanicType() {
        return this.mechanicType;
    }
}
