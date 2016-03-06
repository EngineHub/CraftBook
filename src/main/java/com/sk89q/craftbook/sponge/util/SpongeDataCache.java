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
package com.sk89q.craftbook.sponge.util;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;
import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.MechanicDataCache;

import java.io.*;

public class SpongeDataCache extends MechanicDataCache {

    private static Genson jsonConverter = new GensonBuilder().useFields(true, VisibilityFilter.PACKAGE_PUBLIC).useMethods(false).useRuntimeType(true).useClassMetadata(true).useConstructorWithArguments(true).create();

    @Override
    protected <T extends MechanicData> T loadFromDisk(Class<T> clazz, String locationKey) {
        try {
            T data = null;

            File file = new File("craftbook-data", locationKey + ".json");
            if(file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    data = jsonConverter.deserialize(reader, clazz);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(data == null || !clazz.isInstance(data))
                data = clazz.newInstance();

            return data;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected <T extends MechanicData> void saveToDisk(Class<T> clazz, String locationKey, T data) {
        File file = new File("craftbook-data", locationKey + ".json");

        try(PrintWriter writer = new PrintWriter(file)) {
            writer.print(jsonConverter.serialize(data));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
