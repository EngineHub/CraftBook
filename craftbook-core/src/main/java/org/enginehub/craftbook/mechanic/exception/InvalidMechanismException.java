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

// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package org.enginehub.craftbook.mechanic.exception;

import org.enginehub.craftbook.exception.CraftBookException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

/**
 * Thrown when a MechanicFactory is considering whether or not to produce a Mechanic and finds that an area of the
 * world looks like it it was intended
 * to be a mechanism, but it is is some way not a valid construction. (For example,
 * an area with a "[Bridge]" sign which has blocks of an
 * inappropriate material, or a sign facing an invalid direction, etc.) It is appropriate to extend this exception to
 * produce more specific types.
 *
 * @author hash
 */
public class InvalidMechanismException extends CraftBookException {

    private static final long serialVersionUID = -6917162805444409894L;

    public InvalidMechanismException(String message, Throwable cause) {

        super(TextComponent.of(message), cause);
    }

    public InvalidMechanismException(String message) {

        super(TextComponent.of(message));
    }

    @Override
    public Throwable fillInStackTrace() {
        return null;
    }
}
