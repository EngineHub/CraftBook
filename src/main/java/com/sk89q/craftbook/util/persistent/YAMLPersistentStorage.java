package com.sk89q.craftbook.util.persistent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class YAMLPersistentStorage extends PersistentStorage {

    private YAMLProcessor processor;

    @Override
    public void open () {

        CraftBookPlugin.logger().info("Loading persistant data from YAML!");

        File file = new File(CraftBookPlugin.inst().getDataFolder(), "persistance.yml");
        try {
            if(!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        processor = new YAMLProcessor(file, true, YAMLFormat.COMPACT);

        try {
            processor.load();
        } catch (IOException e) {
            BukkitUtil.printStacktrace(e);
            CraftBookPlugin.logger().warning("Persistant Data Corrupt! Data will be reset!");
        }

        if(getVersion() != getCurrentVersion()) { //Convert.
            CraftBookPlugin.logger().info("Converting database of type: " + getType() + " from version " + getVersion() + " to " + getCurrentVersion());
            convertVersion(getCurrentVersion());
            processor.clear();
            try {
                processor.load();
            } catch (IOException e) {
                BukkitUtil.printStacktrace(e);
                CraftBookPlugin.logger().warning("Persistant Data Corrupt! Data will be reset!");
            }
        }
    }

    @Override
    public void close () {

        CraftBookPlugin.logger().info("Saving persistant data to YAML!");

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