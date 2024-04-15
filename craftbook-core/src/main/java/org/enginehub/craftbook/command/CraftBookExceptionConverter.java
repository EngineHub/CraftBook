package org.enginehub.craftbook.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.internal.command.exception.ExceptionConverterHelper;
import com.sk89q.worldedit.internal.command.exception.ExceptionMatch;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.piston.exception.CommandException;

import static com.google.common.base.Preconditions.checkNotNull;

public class CraftBookExceptionConverter extends ExceptionConverterHelper {

    private final CraftBook craftBook;

    public CraftBookExceptionConverter(CraftBook craftBook) {
        checkNotNull(craftBook);
        this.craftBook = craftBook;
    }

    private CommandException newCommandException(String message, Throwable cause) {
        return newCommandException(TextComponent.of(String.valueOf(message)), cause);
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
