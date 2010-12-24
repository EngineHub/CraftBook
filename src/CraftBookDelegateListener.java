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

import java.util.logging.Logger;

import com.sk89q.craftbook.InsufficientArgumentsException;
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
     * Logger instance.
     */
    protected static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    
    /**
     * CraftBook.
     */
    protected CraftBook craftBook;
    
    /**
     * Reference to the parent listener.
     */
    protected CraftBookListener listener;
    
    /**
     * Properties file for CraftBook.
     */
    protected PropertiesFile properties;
    
    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     */
    public CraftBookDelegateListener(
    		CraftBook craftBook,
    		CraftBookListener listener) {
    	this.craftBook = craftBook;
    	this.listener = listener;
    	this.properties = listener.getProperties();
    }
    
    /**
     * Reads the configuration from the properties file.
     */
    public abstract void loadConfiguration();

    /**
     * Get a block bag.
     * 
     * @param origin
     * @return
     */
    protected BlockBag getBlockBag(Vector origin) {
    	return listener.getBlockBag(origin);
    }
    
    /**
     * Called when a block has been given directed Redstone input. "Directed"
     * applies in the context of wires -- if a wire merely passes by a block,
     * it is not considered directed.
     *
     * @param pt
     * @param isOn
     * @param changed
     */
    public void onDirectWireInput(Vector pt, boolean isOn, Vector changed) {
    }
    
    /**
     * Called on a command that will be checked.
     * 
     * @param player
     * @param split
     * @return
     * @throws InsufficientArgumentsException
     * @throws LocalWorldEditBridgeException
     */
    public boolean onCheckedCommand(Player player, String[] split)
    		throws InsufficientArgumentsException, LocalWorldEditBridgeException {
    	return false;
    }
    
    /**
     * Called on plugin unload.
     */
    public void disable() {
    	
    }
}
