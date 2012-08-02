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

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.craftbook.LanguageManager;
import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.MechanicFactory;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.MechanismsConfiguration;
import com.sk89q.craftbook.bukkit.commands.MechanismCommands;
import com.sk89q.craftbook.mech.Ammeter;
import com.sk89q.craftbook.mech.Bookcase;
import com.sk89q.craftbook.mech.Bridge;
import com.sk89q.craftbook.mech.Cauldron;
import com.sk89q.craftbook.mech.ChunkAnchor;
import com.sk89q.craftbook.mech.Command;
import com.sk89q.craftbook.mech.CookingPot;
import com.sk89q.craftbook.mech.CustomCrafting;
import com.sk89q.craftbook.mech.CustomDrops;
import com.sk89q.craftbook.mech.Door;
import com.sk89q.craftbook.mech.Elevator;
import com.sk89q.craftbook.mech.Gate;
import com.sk89q.craftbook.mech.HiddenSwitch;
import com.sk89q.craftbook.mech.LightStone;
import com.sk89q.craftbook.mech.LightSwitch;
import com.sk89q.craftbook.mech.Payment;
import com.sk89q.craftbook.mech.Snow;
import com.sk89q.craftbook.mech.Teleporter;
import com.sk89q.craftbook.mech.area.Area;
import com.sk89q.craftbook.mech.area.CopyManager;
import com.sk89q.craftbook.mech.cauldron.ImprovedCauldron;
import com.sk89q.craftbook.mech.dispenser.DispenserRecipes;


/**
 * Plugin for CraftBook's mechanisms.
 *
 * @author sk89q
 */
public class MechanismsPlugin extends BaseBukkitPlugin {

    public final CopyManager copyManager = new CopyManager();

    protected MechanismsConfiguration config;

    private MechanicManager manager;
    public static Economy economy = null;  //TODO this probably should be implemented differently

    @Override
    public void onEnable() {

	super.onEnable();

	// Register command classes
	final CommandsManagerRegistration reg = new CommandsManagerRegistration(this, commands);
	reg.register(MechanismCommands.class);

	createDefaultConfiguration("books.txt", false);
	createDefaultConfiguration("cauldron-recipes.txt", false);
	createDefaultConfiguration("config.yml", false);
	createDefaultConfiguration("custom-mob-drops.txt", false);
	createDefaultConfiguration("custom-block-drops.txt", false);
	createDefaultConfiguration("recipes.txt", false);
	createDefaultConfiguration("cauldron-recipes.yml", false);

	config = new MechanismsConfiguration(getConfig(), getDataFolder());
	saveConfig();

	languageManager = new LanguageManager(this);

	if (getServer().getPluginManager().isPluginEnabled("Vault"))
	    setupEconomy();

	manager = new MechanicManager(this);
	MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
	adapter.register(manager);

	// Let's register mechanics!
	registerMechanic(new Ammeter.Factory(this));
	registerMechanic(new Bookcase.Factory(this));
	registerMechanic(new Gate.Factory(this));
	registerMechanic(new Bridge.Factory(this));
	registerMechanic(new Door.Factory(this));
	registerMechanic(new Elevator.Factory(this));
	registerMechanic(new Teleporter.Factory(this));
	registerMechanic(new Area.Factory(this));
	registerMechanic(new Command.Factory(this));
	registerMechanic(new ChunkAnchor.Factory(this));
	registerMechanic(new LightStone.Factory(this));
	registerMechanic(new LightSwitch.Factory(this));
	registerMechanic(new HiddenSwitch.Factory(this));
	registerMechanic(new CookingPot.Factory(this));
	registerMechanic(new Cauldron.Factory(this));
	registerMechanic(new ImprovedCauldron.Factory(this));

	//Special mechanics.
	if (economy != null) manager.register(new Payment.Factory(this));

	setupSelfTriggered(manager);
    }

    /**
     * Setup the required components of INSTANCE-triggered Mechanics..
     */
    private void setupSelfTriggered(MechanicManager manager) {

	logger.info("CraftBook: Enumerating chunks for INSTANCE-triggered components...");

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

	// Set up the clock for INSTANCE-triggered Mechanics.
	getServer().getScheduler().scheduleSyncRepeatingTask(this, new MechanicClock(manager), 0, 2);
    }

    @Override
    protected void registerEvents() {

	CustomCrafting cc = new CustomCrafting(this);
	cc.addRecipes();
	getServer().getPluginManager().registerEvents(new DispenserRecipes(this), this);
	getServer().getPluginManager().registerEvents(new Snow(this), this);
	getServer().getPluginManager().registerEvents(new CustomDrops(this), this);
	getServer().getPluginManager().registerEvents(cc, this);
    }

    @Override
    public MechanismsConfiguration getLocalConfiguration() {

	return config;
    }

    private boolean setupEconomy() {

	RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net
		.milkbowl.vault.economy.Economy.class);
	if (economyProvider != null)
	    economy = economyProvider.getProvider();

	return economy != null;
    }

    public boolean reloadPlugin(CommandSender sender) { //XXX experimental
	try {
	    sender.sendMessage(ChatColor.RED + "Successfully reloaded configuration!");
	    getServer().getPluginManager().enablePlugin(new MechanismsPlugin());
	    getServer().getPluginManager().disablePlugin(this);
	} catch (Exception e) {
	    return false;
	}
	return true;
    }

    /*
     * Register a mechanic if possible
     *
     * @param factory
     */
    public void registerMechanic(MechanicFactory<? extends Mechanic> factory) {

	manager.register(factory);
    }

    /*
     * Register a array of mechanics if possible
     *
     * @param factory
     */
    public void registerMechanic(MechanicFactory<? extends Mechanic>[] factories) {

	for (MechanicFactory<? extends Mechanic> aFactory : factories) {
	    registerMechanic(aFactory);
	}
    }

    /*
     * Unregister a mechanic if possible
     *
     * TODO Ensure no remnants are left behind
     *
     * @param factory
     *
     * @return true if the mechanic was successfully unregistered.
     */
    @SuppressWarnings("unused")
    private boolean unregisterMechanic(MechanicFactory<? extends Mechanic> factory) {

	return manager.unregister(factory);
    }
}