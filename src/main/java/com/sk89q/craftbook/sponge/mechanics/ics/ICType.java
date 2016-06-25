/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.ics;

import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import org.spongepowered.api.world.Location;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static com.sk89q.craftbook.core.util.documentation.DocumentationGenerator.createStringOfLength;

public class ICType<T extends IC> implements DocumentationProvider {

    String name;
    String description;
    String modelId;
    String shorthandId;
    String defaultPinset;

    Class<T> icClass;
    Object[] extraArguments = null;
    Class<?>[] argumentTypes = new Class<?>[2];

    public ICType(String modelId, String shorthandId, String name, String description, Class<T> icClass) {
        this.modelId = modelId;
        this.shorthandId = shorthandId;
        this.name = name;
        this.description = description;
        this.icClass = icClass;
        argumentTypes[0] = ICType.class;
        argumentTypes[1] = Location.class;
    }

    public ICType(String modelId, String shorthandId, String name, String description, Class<T> icClass, String defaultPinset) {
        this(modelId, shorthandId, name, description, icClass);
        this.defaultPinset = defaultPinset;
    }

    public ICType<T> setExtraArguments(Object... args) {
        extraArguments = args;

        argumentTypes = new Class<?>[extraArguments.length + 2];
        argumentTypes[0] = ICType.class;
        argumentTypes[1] = Location.class;
        int num = 2;
        for (Object obj : args)
            argumentTypes[num++] = obj.getClass();

        return this;
    }

    public String getDefaultPinSet() {
        return defaultPinset != null ? defaultPinset : "SISO";
    }

    public IC buildIC(Location block) {
        IC ic = null;

        try {
            Constructor<? extends IC> construct = icClass.getConstructor(argumentTypes);
            if (extraArguments != null && extraArguments.length > 0)
                ic = construct.newInstance(this, block, extraArguments);
            else ic = construct.newInstance(this, block);

            ic.load();
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InstantiationException | InvocationTargetException | IllegalArgumentException e) {
            CraftBookAPI.<CraftBookPlugin>inst().getLogger().error("FAILED TO CREATE IC: " + icClass.getName() + ". WITH ARGS: " + Arrays.toString(extraArguments), e);
        }

        return ic;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getPath() {
        return "mechanics/ics/" + modelId;
    }

    @Override
    public String getTemplatePath() {
        return "mechanics/ics/template";
    }

    @Override
    public String performCustomConversions(String input) {
        String icHeader = createStringOfLength(modelId.length(), '=') + '\n' + modelId + '\n' + createStringOfLength(modelId.length(), '=');
        return input.replace("%IC_HEADER%", icHeader);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ICType<?> icType = (ICType<?>) o;

        return modelId.equals(icType.modelId) && icClass.equals(icType.icClass);
    }

    @Override
    public int hashCode() {
        int result = modelId.hashCode();
        result = 31 * result + icClass.hashCode();
        return result;
    }
}
