package com.sk89q.craftbook.bukkit;

import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.MechanicFactory;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICFamily;
import com.sk89q.craftbook.ic.ICManager;
import com.sk89q.craftbook.ic.families.*;

/**
 * This class acts as a API interface to the diffrent mechanic managers
 * and makes it possible for other plugins to use the CraftBook API.
 *
 * Custom ICs from different plugins should be registered via this class.
 *
 * @author Silthus
 */
public final class CraftBook {

	public static final ICFamily FAMILY_SISO = new FamilySISO();
	public static final ICFamily FAMILY_3ISO = new Family3ISO();
	public static final ICFamily FAMILY_SI3O = new FamilySI3O();
	public static final ICFamily FAMILY_AISO = new FamilyAISO();
	public static final ICFamily FAMILY_3I3O = new Family3I3O();
	public static final ICFamily FAMILY_VIVO = new FamilyVIVO();
	public static final ICFamily FAMILY_SI5O = new FamilySI5O();

	private static ICManager icManager;
	private static MechanicManager manager;

	protected static void init(ICManager icManager, MechanicManager manager) {

		CraftBook.icManager = icManager;
		CraftBook.manager = manager;
	}

	/**
	 * Registers the given mechanic if possible. If the ID is already taking it will not be registered.
	 *
	 * @param name representing the ID of the IC. Must not be named MCXXXX or contains MC in its ID.
	 * @param factory if the IC
	 * @param families to register the IC with
	 * @return false if IC ID is taken or invalid
	 */
	public static boolean registerIC(String name, String longName, ICFactory factory, ICFamily... families) {

		return icManager.registerCustomIC(name, longName, factory, families);
	}

	/**
	 * Register a mechanic if possible
	 *
	 * @param factory
	 */
	public static void registerMechanic(MechanicFactory<? extends Mechanic> factory) {

		manager.register(factory);
	}
}
