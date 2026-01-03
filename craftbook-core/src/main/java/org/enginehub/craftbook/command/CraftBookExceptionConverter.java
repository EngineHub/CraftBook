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

package org.enginehub.craftbook.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.internal.command.exception.ExceptionConverterHelper;
import com.sk89q.worldedit.internal.command.exception.ExceptionMatch;
import com.sk89q.worldedit.util.formatting.text.Component;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.piston.exception.CommandException;

import static com.google.common.base.Preconditions.checkNotNull;

public class CraftBookExceptionConverter extends ExceptionConverterHelper {

    public CraftBookExceptionConverter(CraftBook craftBook) {
        checkNotNull(craftBook);
    }

    private CommandException newCommandException(Component message, Throwable cause) {
        return new CommandException(message, cause, ImmutableList.of());
    }

    @ExceptionMatch
    public void convert(CraftBookException e) throws CommandException {
        throw newCommandException(e.getRichMessage(), e);
    }

    @ExceptionMatch
    public void convert(WorldEditException e) throws CommandException {
        throw newCommandException(e.getRichMessage(), e);
    }
}
