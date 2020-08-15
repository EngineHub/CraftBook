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

package org.enginehub.craftbook.command.argument;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldConverter implements ArgumentConverter<World> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(World.class),
            new WorldConverter()
        );
    }

    private final TextComponent choices;

    private WorldConverter() {
        this.choices = TextComponent.of("any world");
    }

    @Override
    public Component describeAcceptableArguments() {
        return this.choices;
    }

    private Stream<? extends World> getWorlds() {
        return Bukkit.getWorlds().stream();
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        return getWorlds()
            .map(World::getName)
            .filter(world -> world.startsWith(input))
            .collect(Collectors.toList());
    }

    @Override
    public ConversionResult<World> convert(String s, InjectedValueAccess injectedValueAccess) {
        World result = getWorlds()
            .filter(world -> world.getName().equals(s))
            .findAny().orElse(null);
        return result == null
            ? FailedConversion.from(new IllegalArgumentException(
            "Not a valid world: " + s))
            : SuccessfulConversion.fromSingle(result);
    }
}
