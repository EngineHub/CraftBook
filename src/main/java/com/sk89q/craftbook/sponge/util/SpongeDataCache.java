package com.sk89q.craftbook.sponge.util;

import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.MechanicDataCache;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpongeDataCache extends MechanicDataCache {

    @Override
    protected <T extends MechanicData> T loadFromDisk(Class<T> clazz, String locationKey) {
        try {
            T data = clazz.newInstance();

            try {
                ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File("craftbook-data", locationKey + ".cbd"))));

                while (true) {
                    try {
                        Object object = inputStream.readObject();
                        if (!(object instanceof Pair))
                            continue;
                        Pair<String, Object> pair = (Pair<String, Object>) object;

                        Object value = pair.getValue();
                        if (value instanceof WrappedDataNode)
                            value = ((WrappedDataNode) value).deserialize();

                        clazz.getField(pair.getKey()).set(data, value);
                    } catch(NoSuchFieldException e) {
                        System.out.println("Loading class: " + clazz.getName());
                        e.printStackTrace();
                    } catch(EOFException e) {
                        break;
                    }
                }

                inputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return data;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    protected void saveToDisk(Class<MechanicData> clazz, String locationKey, MechanicData data) {

        Map<String, Object> output = data.save();

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(new File("craftbook-data", locationKey + ".cbd")));
            for(Map.Entry<String, Object> entry : output.entrySet()) {
                if(entry.getValue() instanceof MechanicData) {
                    outputStream.writeObject(new ImmutablePair<String, Object>(entry.getKey(), new WrappedDataNode(entry.getValue().getClass().getName(), ((MechanicData) entry.getValue()).save())));
                } else if(entry.getValue() instanceof Object && !(entry.getValue() instanceof Serializable)) {
                    outputStream.writeObject(new ImmutablePair<String, Object>(entry.getKey(), new WrappedDataNode(entry.getValue())));
                } else
                    outputStream.writeObject(new ImmutablePair<String, Object>(entry.getKey(), entry.getValue()));
            }
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class WrappedDataNode implements Serializable {

        private static Objenesis objenesis = new ObjenesisStd();

        static final long serialVersionUID = 1L;

        String typeName;
        List<Pair<String, Object>> values;

        public WrappedDataNode(String typeName, Map<String, Object> values) {
            this.typeName = typeName;
            this.values = new ArrayList<Pair<String, Object>>();
            for(Map.Entry<String, Object> entry : values.entrySet()) {
                if(entry.getValue() instanceof MechanicData) {
                    this.values.add(new ImmutablePair<String, Object>(entry.getKey(), new WrappedDataNode(entry.getValue().getClass().getName(), ((MechanicData) entry.getValue()).save())));
                } else if(entry.getValue() instanceof Object && !(entry.getValue() instanceof Serializable)) {
                    this.values.add(new ImmutablePair<String, Object>(entry.getKey(), new WrappedDataNode(entry.getValue())));
                } else
                    this.values.add(new ImmutablePair<String, Object>(entry.getKey(), entry.getValue()));
            }
        }

        public WrappedDataNode(Object object) {
            this.typeName = object.getClass().getName();
            this.values = new ArrayList<Pair<String, Object>>();
            for(Field field : object.getClass().getFields()) {
                try {
                    Object value = field.get(object);
                    if (value instanceof MechanicData) {
                        this.values.add(new ImmutablePair<String, Object>(field.getName(), new WrappedDataNode(value.getClass().getName(), ((MechanicData) value).save())));
                    } else if(value instanceof Object && !(value instanceof Serializable)) {
                        this.values.add(new ImmutablePair<String, Object>(field.getName(), new WrappedDataNode(value)));
                    } else
                        this.values.add(new ImmutablePair<String, Object>(field.getName(), value));
                } catch(IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        public Object deserialize() {
            try {
                Class<?> clazz = Class.forName(typeName);

                Object object = null;

                if(clazz.isPrimitive()) {
                    object = 0;
                } else {
                    ObjectInstantiator instantiator = objenesis.getInstantiatorOf(clazz);
                    object = instantiator.newInstance();
                }

                for (Pair<String, Object> value : values) {
                    try {
                        if (value.getValue() instanceof WrappedDataNode) {
                            object.getClass().getField(value.getKey()).set(object, ((WrappedDataNode) value.getValue()).deserialize());
                        } else {
                            object.getClass().getField(value.getKey()).set(object, value.getValue());
                        }
                    } catch (NoSuchFieldException e) {
                        System.out.println("CLASS OF TYPE: " + object.getClass().getName());
                        e.printStackTrace();
                    }
                }

                return object;
            } catch(Throwable e) {
                System.out.println("Creating " + typeName);
                e.printStackTrace();
            }

            return null;
        }
    }
}
