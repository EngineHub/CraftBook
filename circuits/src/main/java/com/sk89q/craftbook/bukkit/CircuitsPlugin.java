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
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;

import com.sk89q.craftbook.CircuitsConfiguration;
import com.sk89q.craftbook.LanguageManager;
import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.MechanicFactory;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.bukkit.commands.CircuitCommands;
import com.sk89q.craftbook.circuits.GlowStone;
import com.sk89q.craftbook.circuits.JackOLantern;
import com.sk89q.craftbook.circuits.Netherrack;
import com.sk89q.craftbook.gates.logic.AndGate;
import com.sk89q.craftbook.gates.logic.Clock;
import com.sk89q.craftbook.gates.logic.ClockDivider;
import com.sk89q.craftbook.gates.logic.Counter;
import com.sk89q.craftbook.gates.logic.Delayer;
import com.sk89q.craftbook.gates.logic.DownCounter;
import com.sk89q.craftbook.gates.logic.EdgeTriggerDFlipFlop;
import com.sk89q.craftbook.gates.logic.InvertedRsNandLatch;
import com.sk89q.craftbook.gates.logic.Inverter;
import com.sk89q.craftbook.gates.logic.JkFlipFlop;
import com.sk89q.craftbook.gates.logic.LevelTriggeredDFlipFlop;
import com.sk89q.craftbook.gates.logic.LowDelayer;
import com.sk89q.craftbook.gates.logic.LowNotPulser;
import com.sk89q.craftbook.gates.logic.LowPulser;
import com.sk89q.craftbook.gates.logic.Marquee;
import com.sk89q.craftbook.gates.logic.Monostable;
import com.sk89q.craftbook.gates.logic.Multiplexer;
import com.sk89q.craftbook.gates.logic.NandGate;
import com.sk89q.craftbook.gates.logic.NotDelayer;
import com.sk89q.craftbook.gates.logic.NotLowDelayer;
import com.sk89q.craftbook.gates.logic.NotPulser;
import com.sk89q.craftbook.gates.logic.Pulser;
import com.sk89q.craftbook.gates.logic.Random3Bit;
import com.sk89q.craftbook.gates.logic.RandomBit;
import com.sk89q.craftbook.gates.logic.Repeater;
import com.sk89q.craftbook.gates.logic.RsNandLatch;
import com.sk89q.craftbook.gates.logic.RsNorFlipFlop;
import com.sk89q.craftbook.gates.logic.ToggleFlipFlop;
import com.sk89q.craftbook.gates.logic.XnorGate;
import com.sk89q.craftbook.gates.logic.XorGate;
import com.sk89q.craftbook.gates.weather.RainSensor;
import com.sk89q.craftbook.gates.weather.RainSensorST;
import com.sk89q.craftbook.gates.weather.TStormSensor;
import com.sk89q.craftbook.gates.weather.TStormSensorST;
import com.sk89q.craftbook.gates.weather.WeatherControl;
import com.sk89q.craftbook.gates.weather.WeatherControlAdvanced;
import com.sk89q.craftbook.gates.weather.WeatherFaker;
import com.sk89q.craftbook.gates.world.ArrowBarrage;
import com.sk89q.craftbook.gates.world.ArrowShooter;
import com.sk89q.craftbook.gates.world.BlockSensor;
import com.sk89q.craftbook.gates.world.BlockSensorST;
import com.sk89q.craftbook.gates.world.ChestCollector;
import com.sk89q.craftbook.gates.world.ChestCollectorST;
import com.sk89q.craftbook.gates.world.ChestDispenser;
import com.sk89q.craftbook.gates.world.CombinationLock;
import com.sk89q.craftbook.gates.world.CreatureSpawner;
import com.sk89q.craftbook.gates.world.DaySensor;
import com.sk89q.craftbook.gates.world.DaySensorST;
import com.sk89q.craftbook.gates.world.EntitySensor;
import com.sk89q.craftbook.gates.world.EntitySensorST;
import com.sk89q.craftbook.gates.world.EntityTrap;
import com.sk89q.craftbook.gates.world.EntityTrapST;
import com.sk89q.craftbook.gates.world.FireBarrage;
import com.sk89q.craftbook.gates.world.FireShooter;
import com.sk89q.craftbook.gates.world.FlexibleSetBlock;
import com.sk89q.craftbook.gates.world.ItemDispenser;
import com.sk89q.craftbook.gates.world.ItemNotSensor;
import com.sk89q.craftbook.gates.world.ItemNotSensorST;
import com.sk89q.craftbook.gates.world.ItemSensor;
import com.sk89q.craftbook.gates.world.ItemSensorST;
import com.sk89q.craftbook.gates.world.LavaSensor;
import com.sk89q.craftbook.gates.world.LavaSensorST;
import com.sk89q.craftbook.gates.world.LightSensor;
import com.sk89q.craftbook.gates.world.LightSensorST;
import com.sk89q.craftbook.gates.world.LightningSummon;
import com.sk89q.craftbook.gates.world.Melody;
import com.sk89q.craftbook.gates.world.MessageSender;
import com.sk89q.craftbook.gates.world.MultipleSetBlock;
import com.sk89q.craftbook.gates.world.ParticleEffect;
import com.sk89q.craftbook.gates.world.ParticleEffectST;
import com.sk89q.craftbook.gates.world.PlayerDetection;
import com.sk89q.craftbook.gates.world.PlayerDetectionST;
import com.sk89q.craftbook.gates.world.PotionInducer;
import com.sk89q.craftbook.gates.world.PowerSensor;
import com.sk89q.craftbook.gates.world.PowerSensorST;
import com.sk89q.craftbook.gates.world.RangedOutput;
import com.sk89q.craftbook.gates.world.ServerTimeModulus;
import com.sk89q.craftbook.gates.world.SetBlockAbove;
import com.sk89q.craftbook.gates.world.SetBlockAboveChest;
import com.sk89q.craftbook.gates.world.SetBlockBelow;
import com.sk89q.craftbook.gates.world.SetBlockBelowChest;
import com.sk89q.craftbook.gates.world.SetBridge;
import com.sk89q.craftbook.gates.world.SetDoor;
import com.sk89q.craftbook.gates.world.SoundEffect;
import com.sk89q.craftbook.gates.world.TimeControl;
import com.sk89q.craftbook.gates.world.TimeControlAdvanced;
import com.sk89q.craftbook.gates.world.TimeFaker;
import com.sk89q.craftbook.gates.world.WaterSensor;
import com.sk89q.craftbook.gates.world.WaterSensorST;
import com.sk89q.craftbook.gates.world.WirelessReceiver;
import com.sk89q.craftbook.gates.world.WirelessReceiverST;
import com.sk89q.craftbook.gates.world.WirelessTransmitter;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICFamily;
import com.sk89q.craftbook.ic.ICManager;
import com.sk89q.craftbook.ic.ICMechanicFactory;
import com.sk89q.craftbook.ic.RegisteredICFactory;
import com.sk89q.craftbook.ic.families.Family3ISO;
import com.sk89q.craftbook.ic.families.FamilyAISO;
import com.sk89q.craftbook.ic.families.FamilySI3O;
import com.sk89q.craftbook.ic.families.FamilySISO;
import com.sk89q.wepif.PermissionsResolverManager;
// import com.sk89q.bukkit.migration.*;

/**
 * Plugin for CraftBook's redstone additions.
 *
 * @author sk89q
 */
public class CircuitsPlugin extends BaseBukkitPlugin {

    protected CircuitsConfiguration config;
    private ICManager icManager;
    private PermissionsResolverManager perms;
    private MechanicManager manager;
    private static CircuitsPlugin instance;

    public static Server server;

    public static CircuitsPlugin getInst() {

        return instance;
    }

    @Override
    public void onEnable() {

        super.onEnable();

        instance = this;
        server = getServer();

        registerCommand(CircuitCommands.class);

        createDefaultConfiguration("config.yml", false);
        createDefaultConfiguration("custom-ics.txt", false);
        config = new CircuitsConfiguration(getConfig(), getDataFolder());
        saveConfig();

        languageManager = new LanguageManager(this);

        PermissionsResolverManager.initialize(this);
        perms = PermissionsResolverManager.getInstance();

        manager = new MechanicManager(this);
        MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
        adapter.register(manager);

        File midi = new File(getDataFolder(), "midi/");
        if (!midi.exists()) midi.mkdir();

        registerICs();

        // Let's register mechanics!
        if (config.enableNetherstone) registerMechanic(new Netherrack.Factory());
        if (config.enablePumpkins) registerMechanic(new JackOLantern.Factory());
        if (config.enableGlowStone) registerMechanic(new GlowStone.Factory(this));
        if (config.enableICs) {
            registerMechanic(new ICMechanicFactory(this, icManager));
            setupSelfTriggered();
        }

        // Register events
        registerEvents();
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
        ICFamily familyAISO = new FamilyAISO();
        //ICFamily family3I3O = new Family3I3O();

        //SISOs
        registerIC("MC1000", new Repeater.Factory(server), familySISO, familyAISO);
        registerIC("MC1001", new Inverter.Factory(server), familySISO, familyAISO);
        registerIC("MC1017", new ToggleFlipFlop.Factory(server, true), familySISO, familyAISO);
        registerIC("MC1018", new ToggleFlipFlop.Factory(server, false), familySISO, familyAISO);
        registerIC("MC1020", new RandomBit.Factory(server), familySISO, familyAISO);
        registerIC("MC1025", new ServerTimeModulus.Factory(server), familySISO, familyAISO);
        registerIC("MC1110", new WirelessTransmitter.Factory(server), familySISO, familyAISO);
        registerIC("MC1111", new WirelessReceiver.Factory(server), familySISO, familyAISO);
        registerIC("MC1200", new CreatureSpawner.Factory(server), familySISO, familyAISO);     // Restricted
        registerIC("MC1201", new ItemDispenser.Factory(server), familySISO, familyAISO);       // Restricted
        registerIC("MC1202", new ChestDispenser.Factory(server), familySISO, familyAISO);      // Restricted
        registerIC("MC1203", new LightningSummon.Factory(server), familySISO, familyAISO);     // Restricted
        registerIC("MC1204", new EntityTrap.Factory(server), familySISO, familyAISO);          // Restricted
        registerIC("MC1205", new SetBlockAbove.Factory(server), familySISO, familyAISO);       // Restricted
        registerIC("MC1206", new SetBlockBelow.Factory(server), familySISO, familyAISO);       // Restricted
        registerIC("MC1207", new FlexibleSetBlock.Factory(server), familySISO, familyAISO);    // Restricted
        registerIC("MC1208", new MultipleSetBlock.Factory(server), familySISO, familyAISO);
        registerIC("MC1209", new ChestCollector.Factory(server), familySISO, familyAISO);
        registerIC("MC1210", new ParticleEffect.Factory(server), familySISO, familyAISO);      // Restricted
        registerIC("MC1211", new SetBridge.Factory(server), familySISO, familyAISO);           // Restricted
        registerIC("MC1212", new SetDoor.Factory(server), familySISO, familyAISO);             // Restricted
        registerIC("MC1213", new SoundEffect.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1215", new SetBlockAboveChest.Factory(server), familySISO, familyAISO);  // Restricted
        registerIC("MC1216", new SetBlockBelowChest.Factory(server), familySISO, familyAISO);  // Restricted
        registerIC("MC1217", new PotionInducer.Factory(server), familySISO, familyAISO);
        registerIC("MC1230", new DaySensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1231", new TimeControl.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1236", new WeatherFaker.Factory(server), familySISO, familyAISO);        // Restricted
        registerIC("MC1237", new TimeFaker.Factory(server), familySISO, familyAISO);           // Restricted
        registerIC("MC1240", new ArrowShooter.Factory(server), familySISO, familyAISO);        // Restricted
        registerIC("MC1241", new ArrowBarrage.Factory(server), familySISO, familyAISO);        // Restricted
        registerIC("MC1250", new FireShooter.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1251", new FireBarrage.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1260", new WaterSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1261", new LavaSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1262", new LightSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1263", new BlockSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1264", new ItemSensor.Factory(server), familySISO, familyAISO);          // Restricted
        registerIC("MC1265", new ItemNotSensor.Factory(server), familySISO, familyAISO);       // Restricted
        registerIC("MC1266", new PowerSensor.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1270", new Melody.Factory(server), familySISO, familyAISO);
        registerIC("MC1271", new EntitySensor.Factory(server), familySISO, familyAISO);        // Restricted
        registerIC("MC1272", new PlayerDetection.Factory(server), familySISO, familyAISO);     // Restricted
        //TODO Why was this here? registerIC("MC1299", new ParticleEffect.Factory(server), familySISO, familyAISO);      // Restricted
        registerIC("MC1420", new ClockDivider.Factory(server), familySISO, familyAISO);
        registerIC("MC1510", new MessageSender.Factory(server), familySISO, familyAISO);
        registerIC("MC2100", new Delayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2101", new NotDelayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2110", new LowDelayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2111", new NotLowDelayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2500", new Pulser.Factory(server), familySISO, familyAISO);
        registerIC("MC2501", new NotPulser.Factory(server), familySISO, familyAISO);
        registerIC("MC2510", new LowPulser.Factory(server), familySISO, familyAISO);
        registerIC("MC2511", new LowNotPulser.Factory(server), familySISO, familyAISO);

        //SI3Os
        registerIC("MC2020", new Random3Bit.Factory(server), familySI3O);
        registerIC("MC2999", new Marquee.Factory(server), familySI3O);

        //3ISOs
        registerIC("MC3002", new AndGate.Factory(server), family3ISO);
        registerIC("MC3003", new NandGate.Factory(server), family3ISO);
        registerIC("MC3020", new XorGate.Factory(server), family3ISO);
        registerIC("MC3021", new XnorGate.Factory(server), family3ISO);
        registerIC("MC3030", new RsNorFlipFlop.Factory(server), family3ISO);
        registerIC("MC3031", new InvertedRsNandLatch.Factory(server), family3ISO);
        registerIC("MC3032", new JkFlipFlop.Factory(server), family3ISO);
        registerIC("MC3033", new RsNandLatch.Factory(server), family3ISO);
        registerIC("MC3034", new EdgeTriggerDFlipFlop.Factory(server), family3ISO);
        registerIC("MC3036", new LevelTriggeredDFlipFlop.Factory(server), family3ISO);
        registerIC("MC3040", new Multiplexer.Factory(server), family3ISO);
        registerIC("MC3050", new CombinationLock.Factory(server), family3ISO);
        registerIC("MC3101", new DownCounter.Factory(server), family3ISO);
        registerIC("MC3102", new Counter.Factory(server), family3ISO);
        registerIC("MC3231", new TimeControlAdvanced.Factory(server), family3ISO);             // Restricted
        //Missing: 3231                                                                                // Restricted
        //3I3Os
        //Missing: 4000
        //Missing: 4010
        //Missing: 4100
        //Missing: 4110
        //Missing: 4200

        //Self triggered
        registerIC("MC0111", new WirelessReceiverST.Factory(server), familySISO);
        registerIC("MC0204", new EntityTrapST.Factory(server), familySISO);                    // Restricted
        registerIC("MC0209", new ChestCollectorST.Factory(server), familySISO);
        registerIC("MC0210", new ParticleEffectST.Factory(server), familySISO);
        registerIC("MC0230", new DaySensorST.Factory(server), familySISO);
        registerIC("MC0260", new WaterSensorST.Factory(server), familySISO);
        registerIC("MC0261", new LavaSensorST.Factory(server), familySISO);
        registerIC("MC0262", new LightSensorST.Factory(server), familySISO);
        registerIC("MC0263", new BlockSensorST.Factory(server), familySISO);
        registerIC("MC0264", new ItemSensorST.Factory(server), familySISO);                    // Restricted
        registerIC("MC0265", new ItemNotSensorST.Factory(server), familySISO);                 // Restricted
        registerIC("MC0266", new PowerSensorST.Factory(server), familySISO);                   // Restricted
        registerIC("MC0270", new PowerSensorST.Factory(server), familySISO);
        registerIC("MC0271", new EntitySensorST.Factory(server), familySISO);                  // Restricted
        registerIC("MC0272", new PlayerDetectionST.Factory(server), familySISO);               // Restricted
        registerIC("MC0420", new Clock.Factory(server), familySISO);
        registerIC("MC0421", new Monostable.Factory(server), familySISO);
        registerIC("MC0500", new RangedOutput.Factory(server), familySISO);
        //Missing: 0020 self-triggered RNG (may cause server load issues)
        //Xtra ICs
        //SISOs
        registerIC("MCX230", new RainSensor.Factory(server), familySISO);
        registerIC("MCX231", new TStormSensor.Factory(server), familySISO);
        registerIC("MCX233", new WeatherControl.Factory(server), familySISO);
        //3ISOs
        registerIC("MCT233", new WeatherControlAdvanced.Factory(server), family3ISO);
        //Self triggered
        registerIC("MCZ230", new RainSensorST.Factory(server), familySISO);
        registerIC("MCZ231", new TStormSensorST.Factory(server), familySISO);
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
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new MechanicClock(manager), 0, 2);
    }

    @Override
    protected void registerEvents() {

    }

    @Override
    public CircuitsConfiguration getLocalConfiguration() {

        return config;
    }

    public PermissionsResolverManager getPermissionsResolver() {

        return perms;
    }

    public String getICList() {

        String list = "";
        for (Entry<String, RegisteredICFactory> e : icManager.registered.entrySet()) {
            if (list.equalsIgnoreCase(""))
                list = e.getKey();
            else
                list = list + ", " + e.getKey();
        }
        return list;
    }

    /**
     * Register a mechanic if possible
     *
     * @param name
     * @param factory
     * @param family
     */
    private void registerIC(String name, ICFactory factory, ICFamily family) {

        icManager.register(name, factory, family);
    }

    /**
     * Register a mechanic if possible
     *
     * @param name
     * @param factory
     * @param family
     * @param family2
     */
    private void registerIC(String name, ICFactory factory, ICFamily family, ICFamily family2) {

        icManager.register(name, factory, family, family2);
    }

    /**
     * Register a mechanic if possible
     *
     * @param factory
     */
    private void registerMechanic(MechanicFactory<? extends Mechanic> factory) {

        manager.register(factory);
    }

    /**
     * Register a array of mechanics if possible
     *
     * @param factories
     */
    @SuppressWarnings("unused")
    private void registerMechanic(MechanicFactory<? extends Mechanic>[] factories) {

        for (MechanicFactory<? extends Mechanic> aFactory : factories) {
            registerMechanic(aFactory);
        }
    }

    /**
     * Unregister a mechanic if possible
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
