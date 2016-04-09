package com.sk89q.craftbook.util.persistent;

import java.util.HashMap;
import java.util.Map;

public class DummyPersistentStorage extends PersistentStorage {

    private Map<String, Object> map;

    @Override
    public void open () {
        map = new HashMap<String, Object>();
    }

    @Override
    public void close () {
        map = null; //Dummy doesn't save.
    }

    @Override
    public String getType () {
        return "DUMMY";
    }

    @Override
    public Object get (String location) {
        return map.get(location);
    }

    @Override
    public void set (String location, Object data) {
        map.put(location, data);
    }

    @Override
    public boolean has(String location) {
        return map.containsKey(location);
    }

    @Override
    public boolean isValid () {
        return true;
    }

    @Override
    public int getVersion () {
        return 0;
    }

    @Override
    public int getCurrentVersion () {
        return 0;
    }

    @Override
    public void convertVersion (int version) {

    }

    @Override
    public void importData (Map<String, Object> data, boolean replace) {
        if(replace)
            map = data;
        else
            map.putAll(data);
    }

    @Override
    public Map<String, Object> exportData () {
        return map;
    }
}