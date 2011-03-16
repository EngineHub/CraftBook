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

import org.bukkit.Server;
import com.sk89q.craftbook.CircuitsConfiguration;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.circuits.*;
import com.sk89q.craftbook.gates.logic.*;
import com.sk89q.craftbook.gates.world.*;
import com.sk89q.craftbook.ic.ICFamily;
import com.sk89q.craftbook.ic.ICManager;
import com.sk89q.craftbook.ic.ICMechanicFactory;
import com.sk89q.craftbook.ic.families.FamilySISO;

/**
 * Plugin for CraftBook's redstone additions.
 * 
 * @author sk89q
 */
public class CircuitsPlugin extends BaseBukkitPlugin {
    
    protected CircuitsConfiguration config;
    protected ICManager icManager;
    
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
        
        MechanicManager manager = new MechanicManager();
        MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
        adapter.register(manager);
        
        // Let's register ICs!
        icManager = new ICManager();
        ICFamily familySISO = new FamilySISO();
        icManager.register("MC1000", new Repeater.Factory(server), familySISO);
        icManager.register("MC1001", new Inverter.Factory(server), familySISO);
        icManager.register("MC1017", new RisingToggleFlipFlop.Factory(server), familySISO);
        icManager.register("MC1018", new FallingToggleFlipFlop.Factory(server), familySISO);
        icManager.register("MC1020", new RisingRandomBit.Factory(server), familySISO);
        icManager.register("MC1025", new RisingServerTimeModulus.Factory(server), familySISO);
        icManager.register("MC1110", new WirelessTransmitter.Factory(server), familySISO);
        icManager.register("MC1111", new RisingWirelessReceiver.Factory(server), familySISO);
        
        // Let's register mechanics!
        manager.register(new Netherrack.Factory());
        manager.register(new JackOLantern.Factory());
        manager.register(new ICMechanicFactory(this, icManager));
    }
    
    @Override
    protected void registerEvents() {
    }
    
    public CircuitsConfiguration getLocalConfiguration() {
        return config;
    }
}
