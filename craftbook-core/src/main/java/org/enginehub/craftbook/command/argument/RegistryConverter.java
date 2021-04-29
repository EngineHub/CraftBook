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

package org.enginehub.craftbook.command.argument;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public final class RegistryConverter<V extends Keyed> implements ArgumentConverter<V> {

    @SuppressWarnings("unchecked")
    public static void register(CommandManager commandManager) {
        ImmutableMap.of(
            new TypeToken<MechanicType<?>>() {
            }, MechanicType.class
        )
            .forEach((key, value) -> commandManager.registerConverter(Key.of(key), from((Class<Keyed>) (Object) value)));
    }

    @SuppressWarnings("unchecked")
    private static <V extends Keyed> RegistryConverter<V> from(Class<Keyed> registryType) {
        try {
            Field registryField = registryType.getDeclaredField("REGISTRY");
            Registry<V> registry = (Registry<V>) registryField.get(null);
            return new RegistryConverter<>(registry);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Not a registry-backed type: " + registryType.getName());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Registry field inaccessible on " + registryType.getName());
        }
    }

    private final Registry<V> registry;
    private final TextComponent choices;

    private RegistryConverter(Registry<V> registry) {
        this.registry = registry;
        this.choices = TextComponent.of("any " + registry.getName());
    }

    @Override
    public Component describeAcceptableArguments() {
        return this.choices;
    }

    @Override
    public ConversionResult<V> convert(String argument, InjectedValueAccess injectedValueAccess) {
        V result = registry.get(argument);
        return result == null
            ? FailedConversion.from(new IllegalArgumentException(
            "Not a valid " + registry.getName() + ": " + argument))
            : SuccessfulConversion.fromSingle(result);
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        return SuggestionHelper.getRegistrySuggestions(registry, input).collect(Collectors.toList());
    }
}
