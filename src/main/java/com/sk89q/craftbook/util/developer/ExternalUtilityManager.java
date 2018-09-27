package com.sk89q.craftbook.util.developer;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class ExternalUtilityManager {

    public static void performExternalUtility(String name, String[] args) throws Exception {

        URL[] url = new URL[3];
        /*url[0] = new URL("file://" + CraftBookPlugin.inst().getDataFolder().getAbsolutePath() + "/");
        url[1] = new URL("file://" + new File(CraftBookPlugin.inst().getDataFolder(), "developer").getAbsolutePath() + "/");
        url[2] = new URL("file://" + CraftBookPlugin.inst().getDataFolder().getAbsolutePath() + "/" + name);
        url[3] = new URL("file://" + new File(CraftBookPlugin.inst().getDataFolder(), "developer").getAbsolutePath() + "/" + name);*/

        url[0] = new File(CraftBookPlugin.inst().getDataFolder(), "developer").toURI().toURL();
        url[1] = new File(CraftBookPlugin.inst().getDataFolder(), "developer/").toURI().toURL();
        url[2] = CraftBookPlugin.inst().getDataFolder().toURI().toURL();

        URLClassLoader loader = new URLClassLoader(url, CraftBookPlugin.class.getClassLoader());
        Class<?> base = loader.loadClass(name);
        if(ExternalUtilityBase.class.isAssignableFrom(base))
            base.getConstructors()[0].newInstance(new Object[]{args});
        else
            throw new ClassNotFoundException("Class found, but was wrong type!");
    }
}