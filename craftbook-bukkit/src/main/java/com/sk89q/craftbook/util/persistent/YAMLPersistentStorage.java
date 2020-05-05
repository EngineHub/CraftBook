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

package com.sk89q.craftbook.util.persistent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class YAMLPersistentStorage extends PersistentStorage {

    private YAMLProcessor processor;

    @Override
    public void open () {

        CraftBookPlugin.logger().info("Loading persistent data from YAML!");

        File oldFile = new File(CraftBookPlugin.inst().getDataFolder(), "persistance.yml");
        if(oldFile.exists()) {
            oldFile.renameTo(new File(CraftBookPlugin.inst().getDataFolder(), "persistence.yml"));
            oldFile.delete();
        }

        File file = new File(CraftBookPlugin.inst().getDataFolder(), "persistence.yml");
        try {
            if(!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        processor = new YAMLProcessor(file, true, YAMLFormat.COMPACT);

        try {
            processor.load();
        } catch (Throwable e) {
            e.printStackTrace();
            CraftBookPlugin.logger().warning("Persistent Data Corrupt! Data will be reset!");
        }

        if(getVersion() != getCurrentVersion()) { //Convert.
            CraftBookPlugin.logger().info("Converting database of type: " + getType() + " from version " + getVersion() + " to " + getCurrentVersion());
            convertVersion(getCurrentVersion());
            processor.clear();
            try {
                processor.load();
            } catch (Exception e) {
                e.printStackTrace();
                CraftBookPlugin.logger().warning("Persistent Data Corrupt! Data will be reset!");
            }
        }
    }

    @Override
    public void close () {

        CraftBookPlugin.logger().info("Saving persistent data to YAML!");

        processor.save();
        processor.clear();
    }

    @Override
    public Object get (String location) {
        return processor.getProperty(location);
    }

    @Override
    public void set (String location, Object data) {
        processor.setProperty(location, data);
    }

    @Override
    public boolean has (String location) {
        return processor.getProperty(location) != null;
    }

    @Override
    public boolean isValid () {
        return processor != null;
    }

    @Override
    public String getType () {
        return "YAML";
    }

    @Override
    public int getVersion () {
        return processor.getInt("version", getCurrentVersion());
    }

    @Override
    public void convertVersion (int version) {
        //Not yet needed.
    }

    @Override
    public int getCurrentVersion () {
        return 1;
    }

    @Override
    public void importData (Map<String, Object> data, boolean replace) {
        if(replace)
            processor.clear();
        for(Entry<String, Object> dat : data.entrySet())
            processor.setProperty(dat.getKey(), dat.getValue());
    }

    @Override
    public Map<String, Object> exportData () {
        return processor.getMap();
    }
}