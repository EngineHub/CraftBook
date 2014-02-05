package com.sk89q.craftbook.util.developer;

import java.net.URL;
import java.net.URLClassLoader;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class ExternalUtilityManager {

    public static void performExternalUtility(String name) throws Exception {

        URLClassLoader loader = new URLClassLoader(new URL[]{new URL("file://" + CraftBookPlugin.inst().getDataFolder().getAbsolutePath() + "/")}, CraftBookPlugin.class.getClassLoader());
        Class<?> base = loader.loadClass(name);
        if(ExternalUtilityBase.class.isAssignableFrom(base))
            base.newInstance();
        else
            throw new ClassNotFoundException("Class found, but was wrong type!");
    }
}