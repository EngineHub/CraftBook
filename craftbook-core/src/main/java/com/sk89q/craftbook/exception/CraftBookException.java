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

package com.sk89q.craftbook.exception;

import com.sk89q.craftbook.util.TextUtil;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

import java.util.Locale;

/**
 * Parent type for all exceptions specific to CraftBook.
 */
public class CraftBookException extends Exception {

    private final Component message;

    protected CraftBookException() {
        super();

        this.message = null;
    }

    public CraftBookException(Component message, Throwable cause) {
        super(TextUtil.reduceToText(message, Locale.getDefault()), cause);

        this.message = message;
    }

    @Deprecated
    public CraftBookException(String message) {
        this(TextComponent.of(message));
    }

    public CraftBookException(Component message) {
        super(TextUtil.reduceToText(message, Locale.getDefault()));

        this.message = message;
    }

    public Component getRichMessage() {
        return message;
    }
}
