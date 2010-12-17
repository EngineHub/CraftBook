// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import com.sk89q.craftbook.Vector;

/**
 * Proxy plugin listener called by CraftBook. It has additional hooks
 * that are called by the main CraftBook listener, namely the Redstone
 * input hook.
 * 
 * @author sk89q
 */
public abstract class CraftBookDelegateListener extends PluginListener {
    /**
     * CraftBook.
     */
    protected CraftBook craftBook;
    /**
     * Properties file for CraftBook.
     */
    protected PropertiesFile properties;
    
    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param properties
     */
    public CraftBookDelegateListener(
    		CraftBook craftBook,
    		PropertiesFile properties) {
    	this.properties = properties;
    }
    
    /**
     * Reads the configuration from the properties file.
     */
    public abstract void loadConfiguration();
    
    /**
     * Called when a block has been given directed Redstone input. "Directed"
     * applies in the context of wires -- if a wire merely passes by a block,
     * it is not considered directed.
     *
     * @param x
     * @param y
     * @param z
     * @param isOn
     */
    public void onDirectWireInput(Vector pt, boolean isOn) {
    }
}
