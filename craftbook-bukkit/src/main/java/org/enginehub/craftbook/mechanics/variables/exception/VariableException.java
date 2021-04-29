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

package org.enginehub.craftbook.mechanics.variables.exception;

import com.sk89q.worldedit.util.formatting.text.Component;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.craftbook.mechanics.variables.VariableKey;

/**
 * Refers to an error relating to variables or variable usage.
 */
public class VariableException extends CraftBookException {

    private final VariableKey variableKey;

    public VariableException(Component message, VariableKey variableKey) {
        super(message);

        this.variableKey = variableKey;
    }

    /**
     * Gets the variable key.
     *
     * @return
     */
    public VariableKey getVariableKey() {
        return this.variableKey;
    }
}
