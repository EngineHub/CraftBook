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
            T data = clazz.newInstance();

            try {
                File file = new File("craftbook-data", locationKey + ".json");
                if(!file.exists())
                    return data;

                BufferedReader reader = new BufferedReader(new FileReader(file));

                data = jsonConverter.deserialize(reader, clazz);

                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void saveToDisk(Class<MechanicData> clazz, String locationKey, MechanicData data) {
        try {
            File file = new File("craftbook-data", locationKey + ".json");

            PrintWriter writer = new PrintWriter(file);

            writer.print(jsonConverter.serialize(data));

            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
