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

package com.sk89q.craftbook.access;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Stores the configuration for Craftbook.
 *
 * @author Lymia
 */
public interface Configuration {

    void load() throws IOException;

    boolean hasKey(String key);

    boolean getBoolean(String key, boolean def);

    int getInt(String key, int def);

    float getFloat(String key, float def);

    String getString(String key, String def);

    Boolean getBoolean(String key);

    Integer getInt(String key);

    Float getFloat(String key);

    String getString(String key);

    void setString(String key, Object target);

    String getItemName(int id);

    int getItemId(String name);

    Logger getLogger();

    boolean isValidMob(String mobName);
}
