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

import java.io.File;

import com.sk89q.craftbook.gates.LogicICSet;
import com.sk89q.craftbook.ic.core.ICFamily;
import com.sk89q.craftbook.ic.mechanic.ICManager;
import com.sk89q.craftbook.ic.mechanic.ICMechanicFactory;
import com.sk89q.craftbook.ic.set.AbstractICTemplate;
import com.sk89q.craftbook.ic.set.ICSet;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
// import com.sk89q.bukkit.migration.*;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.circuits.*;
import com.sk89q.craftbook.gates.logic.*;
import com.sk89q.craftbook.gates.weather.*;
import com.sk89q.craftbook.gates.world.*;
import com.sk89q.craftbook.ic.families.*;

/**
 * Plugin for CraftBook's redstone additions.
 * 
 * @author sk89q
 */
public class CircuitsPlugin extends BaseBukkitPlugin {
    
    protected CircuitsConfiguration config;
    protected ICManager icManager;
    private MechanicManager manager;
    private static CircuitsPlugin instance;
    
    public static Server server;
    
    public static CircuitsPlugin getInst()
    {
    	return instance;
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        instance = this;
        server = getServer();
        
        createDefaultConfiguration("config.yml");
        createDefaultConfiguration("custom-ics.txt");
        config = new CircuitsConfiguration(getConfig(), getDataFolder());
                
        PermissionsResolverManager.initialize(this);
        PermissionsResolverManager perms = PermissionsResolverManager.getInstance();
                
        manager = new MechanicManager(this);
        MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
        adapter.register(manager);
        
        File midi = new File(getDataFolder(),"midi/");
        if(!midi.exists())
        	if(!midi.mkdir())
                logger.warning("Failed to create midi directory");
        
        registerICs();
        
        // Let's register mechanics!
        if (config.enableNetherstone) {
            manager.register(new Netherrack.Factory());
        }
        if (config.enablePumpkins) {
            manager.register(new JackOLantern.Factory());
        }
        if (config.enableGlowStone) {
            manager.register(new GlowStone.Factory());
        }
        if (config.enableICs) {
            manager.register(new ICMechanicFactory(this, icManager));
            setupSelfTriggered();
        }
    }
    
    /**
     * Register ICs.
     */
    private void registerICs() {
        Server server = getServer();
        
        // Let's register ICs!
        icManager = new ICManager();
        ICFamily familySISO = new FamilySISO();
        ICFamily family3ISO = new Family3ISO();
        ICFamily familySI3O = new FamilySI3O();
        //ICFamily family3I3O = new Family3I3O();

        registerIcSet(new LogicICSet(server));
        
        //SISOs
        icManager.register("MC1025", new ServerTimeModulus.Factory(server), familySISO);
        icManager.register("MC1110", new WirelessTransmitter.Factory(server), familySISO);
        icManager.register("MC1111", new WirelessReceiver.Factory(server), familySISO);
        icManager.register("MC1200", new EntitySpawner.Factory(server), familySISO);     // Restricted
        icManager.register("MC1201", new ItemDispenser.Factory(server), familySISO);  	 // Restricted
        icManager.register("MC1202", new ChestDispenser.Factory(server), familySISO);                                               // Restricted
        icManager.register("MC1203", new LightningSummon.Factory(server), familySISO);     // Restricted
        icManager.register("MC1204", new EntityTrap.Factory(server), familySISO);     // Restricted
        icManager.register("MC1205", new SetBlockAbove.Factory(server), familySISO);             // Restricted
        icManager.register("MC1206", new SetBlockBelow.Factory(server), familySISO);             // Restricted
        icManager.register("MC1207", new FlexibleSetBlock.Factory(server), familySISO);          // Restricted
        icManager.register("MC1208", new MultipleSetBlock.Factory(server), familySISO);
        icManager.register("MC1209", new ChestCollector.Factory(server), familySISO);
        icManager.register("MC1215", new SetBlockAboveChest.Factory(server), familySISO);             // Restricted
        icManager.register("MC1216", new SetBlockBelowChest.Factory(server), familySISO);             // Restricted
        icManager.register("MC1230", new DaySensor.Factory(server), familySISO);
        icManager.register("MC1231", new TimeControl.Factory(server), familySISO);         // Restricted
        icManager.register("MC1240", new ArrowShooter.Factory(server), familySISO);        // Restricted
        icManager.register("MC1241", new ArrowBarrage.Factory(server), familySISO);        // Restricted
        icManager.register("MC1250", new FireShooter.Factory(server), familySISO);        // Restricted
        icManager.register("MC1251", new FireBarrage.Factory(server), familySISO);        // Restricted
        icManager.register("MC1260", new WaterSensor.Factory(server), familySISO);
        icManager.register("MC1261", new LavaSensor.Factory(server), familySISO);
        icManager.register("MC1262", new LightSensor.Factory(server), familySISO);
        icManager.register("MC1263", new BlockSensor.Factory(server), familySISO);
        icManager.register("MC1270", new Melody.Factory(server), familySISO);
        icManager.register("MC1510", new MessageSender.Factory(server), familySISO);
        
        //SI3Os
        icManager.register("MC2999", new Marquee.Factory(server), familySI3O);
        
        //3ISOs
        icManager.register("MC3101", new DownCounter.Factory(server), family3ISO);
        icManager.register("MC3231", new TimeControlAdvanced.Factory(server), family3ISO);		// Restricted

        //Missing: 3231                                                                         // Restricted        
        //3I3Os
        //Missing: 4000
        //Missing: 4010
        //Missing: 4100
        //Missing: 4110
        //Missing: 4200
        
        //Self triggered
        icManager.register("MC0111", new WirelessReceiverST.Factory(server), familySISO);
        icManager.register("MC0204", new EntityTrapST.Factory(server), familySISO);     // Restricted
        icManager.register("MC0209", new ChestCollectorST.Factory(server), familySISO);
        icManager.register("MC0230", new DaySensorST.Factory(server), familySISO);
        icManager.register("MC0260", new WaterSensorST.Factory(server), familySISO);
        icManager.register("MC0261", new LavaSensorST.Factory(server), familySISO);
        icManager.register("MC0262", new LightSensorST.Factory(server), familySISO);
        icManager.register("MC0263", new BlockSensorST.Factory(server), familySISO);
        icManager.register("MC0420", new Clock.Factory(server), familySISO);
        icManager.register("MC0421", new Monostable.Factory(server), familySISO);
        //Missing: 0020 self-triggered RNG (may cause server load issues)
	//Missing: 0262
	//Missing: 0420     
        //Xtra ICs
        //SISOs
        icManager.register("MCX230", new RainSensor.Factory(server), familySISO);
        icManager.register("MCX231", new TStormSensor.Factory(server), familySISO);
        icManager.register("MCX233", new WeatherControl.Factory(server), familySISO);
        //3ISOs
        icManager.register("MCT233", new WeatherControlAdvanced.Factory(server), family3ISO);
        //Self triggered
        icManager.register("MCZ230", new RainSensorST.Factory(server), familySISO);
        icManager.register("MCZ231", new TStormSensorST.Factory(server), familySISO);

    }
    
    /**
     * Setup the required components of self-triggered ICs.
     */
    private void setupSelfTriggered() {
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
        
        // Set up the clock for self-triggered ICs.
        getServer().getScheduler().scheduleSyncRepeatingTask(this,
                new MechanicClock(manager), 0, 2);
    }
    
    @Override
    protected void registerEvents() {
    }

    public void registerIcSet(ICSet icSet) {
        for(String key:icSet.getIcList()) {
            icManager.register(key,
                               new AbstractICTemplate.TemplateFactory(icSet.getIcTemplate(key)),
                               icSet.getIcFamily(key));
        }
    }
}
