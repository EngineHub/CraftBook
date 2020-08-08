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

package org.enginehub.craftbook.util.persistent;

import java.util.HashMap;
import java.util.Map;

public class DummyPersistentStorage extends PersistentStorage {

    private Map<String, Object> map;

    @Override
    public void open () {
        map = new HashMap<>();
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