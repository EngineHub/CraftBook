package com.sk89q.craftbook.sponge.util;

import com.sk89q.craftbook.core.mechanics.MechanicData;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class SpongeMechanicData implements MechanicData {

    public void load(Map<String, Object> dataMap) {
        for(Field field : getClass().getFields()) {
            try {
                Object obj = dataMap.get(field.getName());
                if (obj != null)
                    field.set(this, obj);
            } catch(IllegalArgumentException e) {
                e.printStackTrace();
            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Object> save() {

        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        for(Field field : getClass().getFields()) {
            try {
                dataMap.put(field.getName(), field.get(this));
            } catch(IllegalArgumentException e) {
                e.printStackTrace();
            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return dataMap;
    }
}
