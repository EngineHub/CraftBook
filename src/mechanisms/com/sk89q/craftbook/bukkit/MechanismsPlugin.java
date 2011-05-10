// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.bukkit;

import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.MechanismsConfiguration;
import com.sk89q.craftbook.bukkit.BaseBukkitPlugin;
import com.sk89q.craftbook.mech.*;

/**
 * Plugin for CraftBook's mechanisms.
 * 
 * @author sk89q
 */
public class MechanismsPlugin extends BaseBukkitPlugin {
    
    protected MechanismsConfiguration config;
    
    @Override
    public void onEnable() {
        super.onEnable();

        createDefaultConfiguration("books.txt");
        createDefaultConfiguration("cauldron-recipes.txt");
        
        config = new MechanismsConfiguration(getConfiguration(), getDataFolder());
        
        MechanicManager manager = new MechanicManager(this);
        MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
        adapter.register(manager);
        
        // Let's register mechanics!
        manager.register(new Bookcase.Factory(this));
        manager.register(new Gate.Factory(this));
        manager.register(new Bridge.Factory(this));
        manager.register(new Elevator.Factory(this));
        manager.register(new LightSwitch.Factory(this));
        manager.register(new Cauldron.Factory(this));
    }
    
    @Override
    protected void registerEvents() {
    }
    
    public MechanismsConfiguration getLocalConfiguration() {
        return config;
    }
}
