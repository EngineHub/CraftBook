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

import org.bukkit.Chunk;
import org.bukkit.World;

import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.MechanismsConfiguration;
import com.sk89q.craftbook.mech.Ammeter;
import com.sk89q.craftbook.mech.Bookcase;
import com.sk89q.craftbook.mech.Bridge;
import com.sk89q.craftbook.mech.Cauldron;
import com.sk89q.craftbook.mech.CookingPot;
import com.sk89q.craftbook.mech.CustomDrops;
import com.sk89q.craftbook.mech.Door;
import com.sk89q.craftbook.mech.Elevator;
import com.sk89q.craftbook.mech.Gate;
import com.sk89q.craftbook.mech.HiddenSwitch;
import com.sk89q.craftbook.mech.LightSwitch;
import com.sk89q.craftbook.mech.Snow;
import com.sk89q.craftbook.mech.area.Area;
import com.sk89q.craftbook.mech.area.CopyManager;


/**
 * Plugin for CraftBook's mechanisms.
 * 
 * @author sk89q
 */
public class MechanismsPlugin extends BaseBukkitPlugin {
    
    protected MechanismsConfiguration config;
    
    public CommandParser commandExecutor;
    
    public CopyManager copyManager = new CopyManager();
    
    @Override
    public void onEnable() {
        super.onEnable();

        createDefaultConfiguration("books.txt");
        createDefaultConfiguration("cauldron-recipes.txt");
        createDefaultConfiguration("config.yml");
        
        config = new MechanismsConfiguration(getConfig(), getDataFolder());
        
        MechanicManager manager = new MechanicManager(this);
        MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
        adapter.register(manager);

        // Let's register mechanics!
        manager.register(new Ammeter.Factory(this));
        //FIXME!!!! GAH!!!!!!!!!!!!!!!!!!!!!!!!! manager.register(new LightMeter.Factory(this));
        manager.register(new Bookcase.Factory(this));
        manager.register(new Gate.Factory(this));
        manager.register(new Bridge.Factory(this));
        manager.register(new Door.Factory(this));
        manager.register(new Elevator.Factory(this));
        manager.register(new Area.Factory(this));
        //manager.register(new LightStone.Factory(this));
        manager.register(new LightSwitch.Factory(this));
        manager.register(new HiddenSwitch.Factory(this));
        manager.register(new CookingPot.Factory(this));
        
        /*
         * Until fixed, Cauldron must be at the bottom of the registration list as 
         * it'll conflict with other mechanics
         */
        
        manager.register(new Cauldron.Factory(this));
        
        setupSelfTriggered(manager);
    }
    
    /**
     * Setup the required components of self-triggered Mechanics..
     */
    private void setupSelfTriggered(MechanicManager manager) {
        logger.info("CraftBook: Enumerating chunks for self-triggered components...");
        
        long start = System.currentTimeMillis();
        int numWorlds = 0;
        int numChunks = 0;
        
        for (World world : getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                manager.enumerate(chunk);
                numChunks++;
            }
            
            numWorlds++;
        }
        
        long time = System.currentTimeMillis() - start;
        
        logger.info("CraftBook: " + numChunks + " chunk(s) for " + numWorlds + " world(s) processed "
                + "(" + Math.round(time / 1000.0 * 10) / 10 + "s elapsed)");
        
        // Set up the clock for self-triggered Mechanics.
        getServer().getScheduler().scheduleSyncRepeatingTask(this,new MechanicClock(manager), 0, 2);
    }
    
    @Override
    protected void registerEvents() {
	getServer().getPluginManager().registerEvents(new Snow(this), this);
	getServer().getPluginManager().registerEvents(new CustomDrops(this), this);
	
	commandExecutor = new CommandParser(this);
	getCommand("savensarea").setExecutor(commandExecutor);
    }
    
    public void reloadLocalConfiguration() {
        config = new MechanismsConfiguration(getConfig(), getDataFolder());
    }
    
    public MechanismsConfiguration getLocalConfiguration() {
        return config;
    }
}