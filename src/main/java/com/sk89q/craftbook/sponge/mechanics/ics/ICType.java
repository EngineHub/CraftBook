package com.sk89q.craftbook.sponge.mechanics.ics;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.spongepowered.api.block.BlockLoc;

public class ICType<T extends IC> {

    String modelId;
    String shorthandId;
    String defaultPinset;

    Class<T> icClass;
    Object[] extraArguments = new Object[0];
    Class<?>[] argumentTypes = new Class<?>[2];

    public ICType(String modelId, String shorthandId, Class<T> icClass) {
        this.modelId = modelId;
        this.shorthandId = shorthandId;
        this.icClass = icClass;
        argumentTypes[0] = ICType.class;
        argumentTypes[1] = BlockLoc.class;
    }

    public ICType(String modelId, String shorthandId, Class<T> icClass, String defaultPinset) {
        this(modelId, shorthandId, icClass);
        this.defaultPinset = defaultPinset;
    }

    public ICType<T> setExtraArguments(Object... args) {
        extraArguments = args;

        argumentTypes = new Class<?>[extraArguments.length + 2];
        argumentTypes[0] = ICType.class;
        argumentTypes[1] = BlockLoc.class;
        int num = 2;
        for (Object obj : args)
            argumentTypes[num++] = obj.getClass();

        return this;
    }

    public String getDefaultPinSet() {
        return defaultPinset != null ? defaultPinset : "SISO";
    }

    public IC buildIC(BlockLoc block) {

        try {
            Constructor<? extends IC> construct = icClass.getConstructor(argumentTypes);
            IC ic = null;
            if (extraArguments.length > 0)
                ic = construct.newInstance(this, block, extraArguments);
            else ic = construct.newInstance(this, block);

            ic.load();

            return ic;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        System.out.println("FAILED TO CREATE IC: " + icClass.getName() + ". WITH ARGS: " + Arrays.toString(extraArguments));

        return null;
    }
}
