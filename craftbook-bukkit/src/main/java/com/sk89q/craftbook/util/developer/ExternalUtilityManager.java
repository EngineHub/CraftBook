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

package com.sk89q.craftbook.util.developer;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class ExternalUtilityManager {

    private ExternalUtilityManager() {
    }

    public static void performExternalUtility(String name) throws Exception {

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
            base.getConstructors()[0].newInstance(new Object[]{new String[]{}});
        else
            throw new ClassNotFoundException("Class found, but was wrong type!");
    }
}