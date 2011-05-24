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
import org.bukkit.Server;
import org.bukkit.World;
import com.sk89q.bukkit.migration.*;
import com.sk89q.craftbook.CircuitsConfiguration;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.circuits.*;
import com.sk89q.craftbook.gates.logic.*;
import com.sk89q.craftbook.gates.world.*;
import com.sk89q.craftbook.ic.ICFamily;
import com.sk89q.craftbook.ic.ICManager;
import com.sk89q.craftbook.ic.ICMechanicFactory;
import com.sk89q.craftbook.ic.families.*;

/**
 * Plugin for CraftBook's redstone additions.
 * 
 * @author sk89q
 */
public class CircuitsPlugin extends BaseBukkitPlugin {
    
    protected CircuitsConfiguration config;
    protected ICManager icManager;
    private PermissionsResolverManager perms;
    private MechanicManager manager;
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        createDefaultConfiguration("custom-ics.txt");
        
        config = new CircuitsConfiguration() {
            @Override
            public void loadConfiguration() {
            }
        };
        
        config.loadConfiguration();
        
        Server server = getServer();
        
        // Prepare to answer permissions questions.
        perms = new PermissionsResolverManager(
                getConfiguration(),     //FIXME this uh, isn't right.
                server,
                getDescription().getName(),
                logger
        );
        new PermissionsResolverServerListener(perms).register(this);
        
        manager = new MechanicManager(this);
        MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
        adapter.register(manager);
        
        registerICs();
        
        // Let's register mechanics!
        manager.register(new Netherrack.Factory());
        manager.register(new JackOLantern.Factory());
        manager.register(new ICMechanicFactory(this, icManager));
        
        setupSelfTriggered();
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
        
        //SISOs
        icManager.register("MC9999", new ResurrectDumbledore.Factory(server, true), familySISO);
        icManager.register("MC1000", new Repeater.Factory(server), familySISO);
        icManager.register("MC1001", new Inverter.Factory(server), familySISO);
        icManager.register("MC1017", new ToggleFlipFlop.Factory(server, true), familySISO);
        icManager.register("MC1018", new ToggleFlipFlop.Factory(server, false), familySISO);
        icManager.register("MC1020", new RandomBit.Factory(server, true), familySISO);
        icManager.register("MC1025", new ServerTimeModulus.Factory(server, true), familySISO);
        icManager.register("MC1110", new WirelessTransmitter.Factory(server), familySISO);
        icManager.register("MC1111", new WirelessReceiver.Factory(server, true), familySISO);
        icManager.register("MC1200", new CreatureSpawner.Factory(server, true), familySISO);    // REQ PERM
        icManager.register("MC1201", new ItemDispenser.Factory(server, true), familySISO);  
        icManager.register("MC1207", new FlexibleSetBlock.Factory(server), familySISO);  // REQ PERM
        //Missing: 1202 (replaced by dispenser?)                                                // REQ PERM
        icManager.register("MC1203", new LightningSummon.Factory(server, true), familySISO);  // REQ PERM
        //Missing: 1205                                                                         // REQ PERM
        //Missing: 1206                                                                         // REQ PERM
        icManager.register("MC1230", new DaySensor.Factory(server, true), familySISO);
        icManager.register("MC1231", new TimeControl.Factory(server, true), familySISO);        // REQ PERM
        icManager.register("MC1260", new WaterSensor.Factory(server, true), familySISO);
        icManager.register("MC1261", new LavaSensor.Factory(server, true), familySISO);
        icManager.register("MC1262", new LightSensor.Factory(server, true), familySISO);
        //Missing: 1240 (replaced by dispenser?)                                                // REQ PERM
        //Missing: 1241 (replaced by dispenser?)                                                // REQ PERM
        //Missing: 1420
        icManager.register("MC1510", new MessageSender.Factory(server, true), familySISO);
        
        //SI3Os
        //Missing: 2020 (?)
        icManager.register("MC2999", new Marquee.Factory(server), familySI3O);
        
        //3ISOs
        icManager.register("MC3002", new AndGate.Factory(server), family3ISO);
        icManager.register("MC3003", new NandGate.Factory(server), family3ISO);
        icManager.register("MC3020", new XorGate.Factory(server), family3ISO);
        icManager.register("MC3021", new XnorGate.Factory(server), family3ISO);
        icManager.register("MC3030", new RsNorFlipFlop.Factory(server), family3ISO);
        icManager.register("MC3031", new InvertedRsNandLatch.Factory(server), family3ISO);
        icManager.register("MC3032", new JkFlipFlop.Factory(server), family3ISO);
        icManager.register("MC3033", new RsNandLatch.Factory(server), family3ISO);
        icManager.register("MC3034", new EdgeTriggerDFlipFlop.Factory(server), family3ISO);
        icManager.register("MC3036", new LevelTriggeredDFlipFlop.Factory(server), family3ISO);
        icManager.register("MC3040", new Multiplexer.Factory(server), family3ISO);
        icManager.register("MC3101", new DownCounter.Factory(server), family3ISO);
        //Missing: 3231                                                                         // REQ PERM
        
        //3I3Os
        //Missing: 4000
        //Missing: 4010
        //Missing: 4100
        //Missing: 4110
        //Missing: 4200
        
        //Self triggered
        icManager.register("MC0111", new WirelessReceiverST.Factory(server), familySISO);
        icManager.register("MC0260", new WaterSensorST.Factory(server), familySISO);
        icManager.register("MC0261", new LavaSensorST.Factory(server), familySISO);
        icManager.register("MC0420", new Clock.Factory(server), familySISO);
        icManager.register("MC0421", new Monostable.Factory(server), familySISO);
        
        //Missing: 0020
	    //Missing: 0230     
	    //Missing: 0262
	    //Missing: 0420     
        
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
    
    public CircuitsConfiguration getLocalConfiguration() {
        return config;
    }
    
    public PermissionsResolverManager getPermissionsResolver() {
        return perms;
    }
}
