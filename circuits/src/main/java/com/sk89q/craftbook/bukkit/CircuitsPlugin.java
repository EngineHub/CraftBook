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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.sql.*;

import java.util.HashSet;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.util.config.ConfigurationNode;

import com.sk89q.bukkit.migration.*;
import com.sk89q.craftbook.*;
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
        Server server = getServer();
        createDefaultConfiguration("config.yml");
        createDefaultConfiguration("custom-ics.txt");
        createDefaultConfiguration("transmitter.db");
        config = new CircuitsConfiguration(getConfiguration(), getDataFolder());
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
        if (config.enableNetherstone) {
            manager.register(new Netherrack.Factory());
        }
        if (config.enablePumpkins) {
            manager.register(new JackOLantern.Factory());
        }
        if (config.enableICs) {
            manager.register(new ICMechanicFactory(this, icManager));
            setupSelfTriggered();
        }
        //Let's load the old transmitter states!
        loadTransmitterState(config.transmitterstates);
    }
    
    @Override
    public void onDisable() {
    	saveTransmitterState(config.transmitterstates);
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
        ICFamily family3I3O = new Family3I3O();
        
        //SISOs
        icManager.register("MC1000", new Repeater.Factory(server), familySISO);
        icManager.register("MC1001", new Inverter.Factory(server), familySISO);
        icManager.register("MC1020", new RandomBit.Factory(server, true), familySISO);
        icManager.register("MC1025", new ServerTimeModulus.Factory(server, true), familySISO);
        icManager.register("MC1110", new WirelessTransmitter.Factory(server), familySISO);
        icManager.register("MC1111", new WirelessReceiver.Factory(server, true), familySISO);
        icManager.register("MC1200", new CreatureSpawner.Factory(server, true), familySISO);    // REQ PERM
        icManager.register("MC1201", new ItemDispenser.Factory(server, true), familySISO);  
        icManager.register("MC1207", new FlexibleSetBlock.Factory(server), familySISO);         // REQ PERM
        icManager.register("MC1208", new MultipleSetBlock.Factory(server), familySISO);         // REQ PERM
        //Missing: 1202 (replaced by dispenser?)                                                // REQ PERM
        icManager.register("MC1203", new LightningSummon.Factory(server, true), familySISO);    // REQ PERM
        icManager.register("MC1205", new SetBlockAbove.Factory(server, true), familySISO);      // REQ PERM
        icManager.register("MC1206", new SetBlockBelow.Factory(server, true), familySISO);      // REQ PERM
        icManager.register("MC1230", new DaySensor.Factory(server, true), familySISO);
        icManager.register("MC1231", new SimpleTimeControl.Factory(server, true), familySISO);        // REQ PERM
        icManager.register("MC1260", new WaterSensor.Factory(server, true), familySISO);
        icManager.register("MC1261", new LavaSensor.Factory(server, true), familySISO);
        icManager.register("MC1262", new LightSensor.Factory(server, true), familySISO);
        icManager.register("MC1263", new RainSensor.Factory(server, true), familySISO); 
        icManager.register("MC1264", new ThunderSensor.Factory(server, true), familySISO);
        //Missing: 1240 (replaced by dispenser?)                                                // REQ PERM
        //Missing: 1241 (replaced by dispenser?)                                                // REQ PERM
        icManager.register("MC1420", new DivideByN.Factory(server), familySISO);
        icManager.register("MC1421", new Clock.Factory(server, null), familySISO);
        icManager.register("MC1422", new Clock.Factory(server, true), familySISO);
        icManager.register("MC1423", new Clock.Factory(server, false), familySISO);
        icManager.register("MC1510", new MessageSender.Factory(server, true), familySISO);
        
        //SI3Os
        icManager.register("MC2020", new Random3Bit.Factory(server, true), familySI3O);
        icManager.register("MC2999", new Marquee.Factory(server), familySI3O);
        
        //3ISOs
        //Gates
        icManager.register("MC3000", new AndGate.Factory(server), family3ISO);
        icManager.register("MC3001", new NandGate.Factory(server), family3ISO);
        
        icManager.register("MC3002", new OrGate.Factory(server), family3ISO);
        icManager.register("MC3003", new NorGate.Factory(server), family3ISO);
        
        icManager.register("MC3004", new XorGate.Factory(server), family3ISO);
        icManager.register("MC3005", new XnorGate.Factory(server), family3ISO);
        //RS Flip Flops
        icManager.register("MC3010", new RsNorFlipFlop.Factory(server), family3ISO);
        icManager.register("MC3011", new RsNandLatch.Factory(server), family3ISO);
        icManager.register("MC3012", new InvertedRsNandLatch.Factory(server), family3ISO);
        //JK Flip Flops        
        icManager.register("MC3020", new JkFlipFlop.Factory(server), family3ISO);
        //D Flip Flops
        icManager.register("MC3030", new EdgeTriggerDFlipFlop.Factory(server), family3ISO);
        icManager.register("MC3031", new LevelTriggeredDFlipFlop.Factory(server), family3ISO);
        //T Flip Flops
        icManager.register("MC3040", new ToggleFlipFlopEdge.Factory(server, true), familySISO);
        icManager.register("MC3041", new ToggleFlipFlopEdge.Factory(server, false), familySISO);
        icManager.register("MC3042", new ToggleFlipFlopClock.Factory(server), family3ISO);
        //rest
        icManager.register("MC3100", new Multiplexer.Factory(server), family3ISO);
        icManager.register("MC3101", new DownCounter.Factory(server), family3ISO);
        icManager.register("MC3102", new ChangeDetector.Factory(server), family3ISO);
        icManager.register("MC3231", new TimeControl.Factory(server, true), family3ISO);		// REQ PERM
        icManager.register("MC3232", new WeatherControl.Factory(server, true), family3ISO);		// REQ PERM (?) //FIXME: CHECK THAT!
        
        //3I3Os
        icManager.register("MC4000", new FullAdder.Factory(server), family3I3O);
        icManager.register("MC4010", new HalfAdder.Factory(server), family3I3O);
        
        icManager.register("MC4100", new FullSubtractor.Factory(server), family3I3O);
        icManager.register("MC4110", new HalfSubtractor.Factory(server), family3I3O);
        icManager.register("MC4200", new Dispatcher.Factory(server), family3I3O);
        
        //Self triggered
        icManager.register("MC0111", new WirelessReceiverST.Factory(server), familySISO);
        icManager.register("MC0230", new DaySensorST.Factory(server), familySISO);
        icManager.register("MC0260", new WaterSensorST.Factory(server), familySISO);
        icManager.register("MC0261", new LavaSensorST.Factory(server), familySISO);
        icManager.register("MC0262", new LightSensorST.Factory(server), familySISO);
        icManager.register("MC0263", new RainSensorST.Factory(server), familySISO);
        icManager.register("MC0264", new ThunderSensorST.Factory(server), familySISO);
        icManager.register("MC0420", new ClockST.Factory(server), familySISO);
        icManager.register("MC0421", new Monostable.Factory(server), familySISO);
        icManager.register("MC0020", new RandomBitST.Factory(server), familySISO);
        icManager.register("MC9", new TEST.Factory(server), family3I3O);
        
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
    
    private void loadTransmitterState(ConfigurationNode ts) {
    	if (ts.getBoolean("use", false)) {
    		
    		final boolean DEBUG = ts.getBoolean("debug", false);
    		String fileName;
    		
	    	    if (!ts.getString("path", "default").equalsIgnoreCase("default")) { //! cause if it returns null, it will be with the ! true and default is loaded! :)
	    	    	fileName = ts.getString("path");
	    	    } else {
	    	    	fileName = getDataFolder().getPath();
	    	    }
	    	    if (!ts.getString("filename", "default").equalsIgnoreCase("default")) {
	    	    	fileName += File.separator + ts.getString("filename");
	    	    } else {
	    	    	if (ts.getString("method", "sqlite").equalsIgnoreCase("sqlite")) {
	    	    		fileName += File.separator + "transmitter.db";
	    	    	} else {
	    	    		fileName += File.separator + "transmitter.txt";
	    	    	}
	    	    }
    		
	    	    HashSet<String> excludeWorlds = new HashSet<String>();
    			for (String tmp : ts.getString("exclude-worlds", "").split(",")) {
    				excludeWorlds.add(tmp.trim());
    			}
    			
    		if (ts.getString("method").equalsIgnoreCase("sqlite")) {
    			/*
    			 * necessary for SQLITE
    			 * use: true
    			 * method: SQLite
    			 * 
    			 * optional for SQLITE
    			 * path: default
    			 * filename: default
    			 * exclude-worlds:
    			 * 
    			 * DEBUG MODE
    			 * debug: false
    			 */
    			Connection connection = null;  
        	    ResultSet result = null;
        	    Statement statement = null;
        	    String worldName = "";

        	    try {
    				Class.forName("org.sqlite.JDBC"); //checks, if the driver is in the bukkit jar
    				connection  = DriverManager.getConnection("jdbc:sqlite:" + fileName);
    				statement = connection.createStatement();
    				for (World world : getServer().getWorlds()) {

    					worldName = world.getName();
    					
    					if (excludeWorlds.contains(worldName)) { continue; } //skip this world, if it should be excluded!
    					
    					try { //if the table doesn't exist, then here will a SQLException be throwned, and we can create the Table and return true :)
    						result = statement.executeQuery("SELECT * FROM "+ worldName +";");
    					} catch (SQLException e) {
    						if (DEBUG) { logger.warning(e.toString()); }
    						if (e.toString().contains("no such table:")) { //if the error is no such table: $tablename
    							try {
    								statement.execute("CREATE TABLE " + worldName + "(BandID VARCHAR, Bool BOOLEAN);"); //not executeQuery, cause it hasn't a result and throws an error otherwise!
    								logger.info("Craftbook: Table " + worldName + " created.");
    								continue;
    							} catch (SQLException ex) { //irrelevant
    								if (DEBUG) { logger.warning(ex.toString()); }
    							}
    						}
    					}
    					int c = 0;
    					long start = System.currentTimeMillis();
    					if (result.next()) { //prevents exception when there are no datas in the table
    						do {
    							c++;
    							WirelessTransmitter.setValue(worldName, result.getString("BandID"), result.getBoolean("Bool")); //normal "declaration" of transmitter states
    						} while (result.next());
    					}
    					long time = System.currentTimeMillis() - start;
    					logger.info("Craftbook: "+c+" Transmitterstates successfully for world \"" + worldName + "\" restored. (" + Math.round(time / 1000.0 * 10) / 10 + "s elapsed)");
    				}
    			} catch (SQLException e) {
    				if (DEBUG) { logger.warning(e.toString()); }
    			} catch (ClassNotFoundException e) {
    				if (DEBUG) { logger.warning(e.toString()); }
    				logger.severe("Craftbook: Cannot find SQLite libaries. Is Bukkit actual?");
    				logger.warning("Craftbook: Transmitter states NOT loaded!");
    				return;
    			} finally {
	    			try {
	    				connection.close();
	    				statement.close();
	    				result.close();
	    			} catch (Exception e) {} //irrelevant
    			}
    		} else if (ts.getString("method").equalsIgnoreCase("mysql")) {
    			/*
    			 * necessary for MYSQL
    			 * use: true
    			 * method: MYSQL
    			 * host: localhost
    			 * user: root
    			 * password: 
    			 * database: minecraft
    			 * 
    			 * optional for MYSQL
    			 * table_prefix: 
    			 * port:
    			 * exclude-worlds:
    			 * 
    			 * DEBUG MODE
    			 * debug: false
    			 */
    			Connection connection = null;  
        	    ResultSet result = null;
        	    Statement statement = null;
        	    String worldName = "";
        	    
        	    final String HOST = ts.getString("host", "localhost");
        	    final int	 PORT = ts.getInt("port", 3306);
        	    final String DABA = ts.getString("database", "minecraft");
        	    final String USER = ts.getString("user", "root");
        	    final String PASS = ts.getString("password", "");
        	    
        	    final String TPRE = ts.getString("table-prefix", "");
        	    
        	    try {
    				Class.forName("com.mysql.jdbc.Driver");
    				connection  = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DABA, USER, PASS);
    				statement = connection.createStatement();
    				for (World world : getServer().getWorlds()) {

    					worldName = world.getName();
    					
    					if (excludeWorlds.contains(worldName)) { continue; } //skip this world, if it should be excluded!
    					
    					try { //if the table doesn't exist, then here will a SQLException be throwned, and we can create the Table and return true :)
    						result = statement.executeQuery("SELECT * FROM "+ TPRE + worldName +";");
    					} catch (SQLException e) {
    						if (DEBUG) { logger.warning(e.toString()); }
    						if (e.toString().matches("(.*): Table '(.*)' doesn't exist")) { //if the error is " Table '$database.$tablename' doesn't exist"
    							try {
    								statement.execute(	"CREATE TABLE `" + TPRE + worldName + "`(" + //not executeQuery, cause it hasn't a result and throws an error otherwise!
    													" `BandID` varchar(15) NOT NULL, " +
    													"`BOOL` tinyint(1) NOT NULL," +
    													" PRIMARY KEY (`BandID`)" +
    													") ENGINE=MyISAM DEFAULT CHARSET=utf8;");
    								logger.info("Craftbook: Table " + worldName + " created.");
    								continue;
    							} catch (SQLException ex) { //irrelevant
    								if (DEBUG) { logger.warning(ex.toString()); }
    							}
    						}
    					}
    					int c = 0;
    					long start = System.currentTimeMillis();
    					if (result.next()) { //prevents exception when there are no datas in the table
    						do {
    							c++;
    							WirelessTransmitter.setValue(worldName, result.getString("BandID"), result.getBoolean("Bool")); //normal "declaration" of transmitter states
    						} while (result.next());
    					}
    					long time = System.currentTimeMillis() - start;
    					logger.info("Craftbook: "+c+" Transmitterstates successfully for world \"" + worldName + "\" restored. (" + Math.round(time / 1000.0 * 10) / 10 + "s elapsed)");
    				}
    			} catch (SQLException e) {
    				if (DEBUG) { logger.warning(e.toString()); }
    				logger.severe("Craftbook: Problem with Database or Table. Check your config!");
    				return;
    			} catch (ClassNotFoundException e) {
    				if (DEBUG) { logger.warning(e.toString()); }
    				logger.severe("Craftbook: Cannot find MYSQL libaries. Is Bukkit actual?");
    				logger.warning("Craftbook: Transmitter states NOT loaded!");
    				return;
    			} finally {
	    			try {
	    				connection.close();
	    				statement.close();
	    				result.close();
	    			} catch (Exception e) {} //irrelevant
    			}
    		} else {
    			/*
    			 * necessary for file
    			 * use: true
    			 * method: file
    			 * 
    			 * optional for file
    			 * path: default
    			 * filename: default
    			 * exclude-worlds:
    			 * 
    			 * DEBUG MODE
    			 * debug: false
    			 */
    			RandomAccessFile file = null;
    			for (World world : getServer().getWorlds()) {
    				String localFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "-" + world.getName()+".txt";
	    			try {
	    				String[] tmp;
	    				String temp;
    					file = new RandomAccessFile(localFileName, "r");
    					
    					String worldName = world.getName();
    					if (excludeWorlds.contains(worldName)) { continue; }
    					
    					int c = 0;
    					long start = System.currentTimeMillis();
	    				while (true) {
	    					temp = file.readLine();
	    					if (temp == null || !temp.contains(":")) { break; }
	    					tmp = temp.split(":");
	    					WirelessTransmitter.setValue(worldName, tmp[0].trim(), (tmp[1].trim().equalsIgnoreCase("true") ? true : false));
	    					c++;
	    				}
	    				long time = System.currentTimeMillis() - start;
    					logger.info("Craftbook: " + c + " Transmitterstates successfully for world \"" + worldName + "\" restored. (" + Math.round(time / 1000.0 * 10) / 10 + "s elapsed)");
    					file.close();
	    			} catch (FileNotFoundException e) {
						File tmp = new File(localFileName);
						try {
							tmp.createNewFile();
							logger.info("Craftbook: File " + localFileName + " successfully created.");
						} catch (IOException e1) {
							if (DEBUG) { logger.warning(e1.toString()); }
							logger.warning("Craftbook: File " + localFileName + " cannot be created. Please check your right management.");
						}					
					} catch (IOException e) {
						if (DEBUG) { logger.warning(e.toString()); }
						logger.warning("Craftbook: Something went terribly wrong with the File writing. Check that!");
					}
    			}
    		}
    	}
	}
    
    private void saveTransmitterState(ConfigurationNode ts) {
    	if (ts.getBoolean("use", false)) {
    		logger.info("Craftbook: Begin saving.");
    		final boolean DEBUG = ts.getBoolean("debug", false);
    		
    		String fileName;
    		
    	    if (!ts.getString("path", "default").equalsIgnoreCase("default")) { //! cause if it returns null, it will be with the ! true and default is loaded! :)
    	    	fileName = ts.getString("path");
    	    } else {
    	    	fileName = getDataFolder().getPath();
    	    }
    	    if (!ts.getString("filename", "default").equalsIgnoreCase("default")) {
    	    	fileName += File.separator + ts.getString("filename");
    	    } else {
    	    	if (ts.getString("method", "sqlite").equalsIgnoreCase("sqlite")) {
    	    		fileName += File.separator + "transmitter.db";
    	    	} else {
    	    		fileName += File.separator + "transmitter.txt";
    	    	}
    	    }
		
    	    HashSet<String> excludeWorlds = new HashSet<String>();
			for (String tmp : ts.getString("exclude-worlds", "").split(",")) {
				excludeWorlds.add(tmp.trim());
			}
			
			
			if (ts.getString("method").equalsIgnoreCase("sqlite")) {
				Connection connection = null;  
        	    ResultSet result = null;
        	    Statement statement = null;
        	    String worldName = "";

        	    try {
    				Class.forName("org.sqlite.JDBC"); //checks, if the driver is in the bukkit jar
    				connection  = DriverManager.getConnection("jdbc:sqlite:" + fileName);
    				statement = connection.createStatement();
    				for (World world : getServer().getWorlds()) {

    					worldName = world.getName();
    					
    					if (excludeWorlds.contains(worldName)) { continue; } //skip this world, if it should be excluded!
    					
    					try {
    						result = statement.executeQuery("SELECT * FROM "+ worldName +";"); //checks if the table exists
    					} catch (SQLException e) {
    						if (DEBUG) { logger.warning(e.toString()); }
    						if (e.toString().contains("no such table:")) { //if the error is no such table: $tablename
    							try {
    								statement.execute("CREATE TABLE " + worldName + "(BandID VARCHAR, Bool BOOLEAN);"); //not executeQuery, cause it hasn't a result and throws an error otherwise!
    								logger.info("Craftbook: Table " + worldName + " created.");
    								continue;
    							} catch (SQLException ex) { //irrelevant
    								if (DEBUG) { logger.warning(ex.toString()); }
    							}
    						}
    					}
    					int c = 0;
						long start = System.currentTimeMillis();
						statement.execute("DELETE FROM \"" + worldName + "\";"); //truncates the table :)
    					for (String key : WirelessTransmitter.getKeys(worldName)) {
    						statement.execute("INSERT INTO \"" + worldName + "\" " +
    										  "VALUES (\"" + key + "\", \"" +
    										  "" + WirelessTransmitter.getValue(worldName, key) + "\");");
    						c++;
    					}
    					long time = System.currentTimeMillis() - start;
    					logger.info("Craftbook: "+c+" Transmitterstates successfully for world \"" + worldName + "\" saved. (" + Math.round(time / 1000.0 * 10) / 10 + "s elapsed)");
    				}
    			} catch (SQLException e) {
    				if (DEBUG) { logger.warning(e.toString()); }
    			} catch (ClassNotFoundException e) {
    				if (DEBUG) { logger.warning(e.toString()); }
    				logger.severe("Craftbook: Cannot find SQLite libaries. Is Bukkit actual?");
    				logger.warning("Craftbook: Transmitter states NOT saved!");
    				return;
    			} finally {
	    			try {
	    				connection.close();
	    				statement.close();
	    				result.close();
	    			} catch (Exception e) {} //irrelevant
    			}
			}  else if (ts.getString("method").equalsIgnoreCase("mysql")) {
    			/*
    			 * necessary for MYSQL
    			 * use: true
    			 * method: MYSQL
    			 * host: localhost
    			 * user: root
    			 * password: 
    			 * database: minecraft
    			 * 
    			 * optional for MYSQL
    			 * table_prefix: 
    			 * port:
    			 * exclude-worlds:
    			 * 
    			 * DEBUG MODE
    			 * debug: false
    			 */
    			Connection connection = null;  
        	    ResultSet result = null;
        	    Statement statement = null;
        	    String worldName = "";
        	    
        	    final String HOST = ts.getString("host", "localhost");
        	    final int	 PORT = ts.getInt("port", 3306);
        	    final String DABA = ts.getString("database", "minecraft");
        	    final String USER = ts.getString("user", "root");
        	    final String PASS = ts.getString("password", "");
        	    
        	    final String TPRE = ts.getString("table-prefix", "");
        	    
        	    try {
    				Class.forName("com.mysql.jdbc.Driver");
    				connection  = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DABA, USER, PASS);
    				statement = connection.createStatement();
    				for (World world : getServer().getWorlds()) {

    					worldName = world.getName();
    					
    					if (excludeWorlds.contains(worldName)) { continue; } //skip this world, if it should be excluded!
    					
    					try { //if the table doesn't exist, then here will a SQLException be throwned, and we can create the Table and return true :)
    						result = statement.executeQuery("SELECT * FROM "+ TPRE + worldName +";");
    					} catch (SQLException e) {
    						if (DEBUG) { logger.warning(e.toString()); }
    						if (e.toString().matches("(.*): Table '(.*)' doesn't exist")) { //if the error is " Table '$database.$tablename' doesn't exist"
    							try {
    								statement.execute(	"CREATE TABLE `" + TPRE + worldName + "`(" + //not executeQuery, cause it hasn't a result and throws an error otherwise!
    													" `BandID` varchar(15) NOT NULL, " +
    													"`BOOL` tinyint(1) NOT NULL," +
    													" PRIMARY KEY (`BandID`)" +
    													") ENGINE=MyISAM DEFAULT CHARSET=utf8;");
    								logger.info("Craftbook: Table " + worldName + " created.");
    								continue;
    							} catch (SQLException ex) { //irrelevant
    								if (DEBUG) { logger.warning(ex.toString()); }
    							}
    						}
    					}
    					int c = 0;
    					long start = System.currentTimeMillis();
    					statement.execute("TRUNCATE TABLE `" + worldName + "`;"); //clears the table, cause else there will be error, because of double primary keys and so on :)
    					for (String key : WirelessTransmitter.getKeys(worldName)) {
    						statement.execute("INSERT INTO `" + TPRE + worldName + "` VALUES ('" + key + "', '" + boolToInt(WirelessTransmitter.getValue(worldName, key)) + "');");
    						c++;
    						//INSERT INTO `world` VALUES ('asdasdkasd', '1');
    					}
    					long time = System.currentTimeMillis() - start;
    					logger.info("Craftbook: "+c+" Transmitterstates successfully for world \"" + worldName + "\" saved. (" + Math.round(time / 1000.0 * 10) / 10 + "s elapsed)");
    				}
    			} catch (SQLException e) {
    				if (DEBUG) { logger.warning(e.toString()); }
    				logger.severe("Craftbook: Problem with Database or Table. Check your config!");
    				return;
    			} catch (ClassNotFoundException e) {
    				if (DEBUG) { logger.warning(e.toString()); }
    				logger.severe("Craftbook: Cannot find MYSQL libaries. Is Bukkit actual?");
    				logger.warning("Craftbook: Transmitter states NOT saved!");
    				return;
    			} finally {
	    			try {
	    				connection.close();
	    				statement.close();
	    				result.close();
	    			} catch (Exception e) {} //irrelevant
    			}
			} else {
				RandomAccessFile file = null;
    			for (World world : getServer().getWorlds()) {
    				String localFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "-" + world.getName()+".txt";
	    			try {
	    				File file2 = new File(localFileName);
    					if (!file2.delete()) { //else it will just append all things, but i want them to override!
    						logger.warning("File cannot be deletet. Check the file and use it on your own risk!");
    					}
	    				file = new RandomAccessFile(file2, "rw");
    					
    					String worldName = world.getName();
    					if (excludeWorlds.contains(worldName)) { continue; }
    					
    					long start = System.currentTimeMillis();
    					int c = 0;
    					for (String key : WirelessTransmitter.getKeys(worldName)) {
    						file.writeUTF(key + ": " + WirelessTransmitter.getValue(worldName, key) + System.getProperty("line.separator"));
    						c++;
    					}
	    				long time = System.currentTimeMillis() - start;
    					logger.info("Craftbook: " + c + " Transmitterstates successfully for world \"" + worldName + "\" saved. (" + Math.round(time / 1000.0 * 10) / 10 + "s elapsed)");
    					file.close();
	    			} catch (FileNotFoundException e) {
						File tmp = new File(localFileName);
						try {
							tmp.createNewFile();
						} catch (IOException e1) {
							if (DEBUG) { logger.warning(e1.toString()); }
						}					
					} catch (IOException e) {
						if (DEBUG) { logger.warning(e.toString()); }
						logger.warning("Craftbook: Something went terribly wrong with the File writing. Check that!");
					}
    			}
			}
    	} //end use if
    }
    
    private int boolToInt (Boolean b) {
    	return (b) ? 1 : 0;
    }
}
