/*    
Craftbook 
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.IOException;

import com.sk89q.craftbook.access.Configuration;

public class HmodConfigurationImpl implements Configuration {
    private PropertiesFile f;
    private DataSource s = etc.getDataSource();
    
    public HmodConfigurationImpl(PropertiesFile f) throws IOException {
        this.f = f;
        f.load();
    }
    
    public boolean hasKey(String key) {
        return f.containsKey(key);
    }
    
    public boolean getBoolean(String key, boolean def) {
        Boolean b = getBoolean(key);
        if(b==null) {
            setString(key,def);
            return def;
        } else {
            return b;
        }
    }
    public int getInt(String key, int def) {
        return f.getInt(key,def);
    }
    public float getFloat(String key, float def) {
        return (float)f.getDouble(key,def);
    }
    public String getString(String key, String def) {
        return f.getString(key,def);
    }
    
    public Boolean getBoolean(String key) {
        String s = getString(key);
        if(s==null) return null;
        else if(s.equals("true")) return true;
        else if(s.equals("false")) return false;
        else return null;
    }
    public Integer getInt(String key) {
        String s = getString(key);
        if(s==null) return null;
        else try {
            return Integer.parseInt(key);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public Float getFloat(String key) {
        String s = getString(key);
        if(s==null) return null;
        else try {
            return Float.parseFloat(key);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public String getString(String key) {
        if(!f.containsKey(key)) return null;
        else return f.getString(key);
    }
    
    public void setString(String key, Object target) {
        f.setString(key, target.toString());
    }
    
    public void load() throws IOException {
        f.load();
    }

    public String getItemName(int id) {
        return s.getItem(id);
    }
    public int getItemId(String name) {
        return s.getItem(name);
    }
}
