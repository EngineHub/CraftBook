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

import com.sk89q.craftbook.gates.logic.*;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;

import com.sk89q.craftbook.CircuitsConfiguration;
import com.sk89q.craftbook.LanguageManager;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.circuits.GlowStone;
import com.sk89q.craftbook.circuits.JackOLantern;
import com.sk89q.craftbook.circuits.Netherrack;
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
import com.sk89q.craftbook.gates.world.DaySensor;
import com.sk89q.craftbook.gates.world.DaySensorST;
import com.sk89q.craftbook.gates.world.Detection;
import com.sk89q.craftbook.gates.world.DetectionST;
import com.sk89q.craftbook.gates.world.EntitySpawner;
import com.sk89q.craftbook.gates.world.EntityTrap;
import com.sk89q.craftbook.gates.world.EntityTrapST;
import com.sk89q.craftbook.gates.world.FireBarrage;
import com.sk89q.craftbook.gates.world.FireShooter;
import com.sk89q.craftbook.gates.world.FlexibleSetBlock;
import com.sk89q.craftbook.gates.world.ItemDispenser;
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
import com.sk89q.craftbook.gates.world.PotionInducer;
import com.sk89q.craftbook.gates.world.RangedOutput;
import com.sk89q.craftbook.gates.world.ServerTimeModulus;
import com.sk89q.craftbook.gates.world.SetBlockAbove;
import com.sk89q.craftbook.gates.world.SetBlockAboveChest;
import com.sk89q.craftbook.gates.world.SetBlockBelow;
import com.sk89q.craftbook.gates.world.SetBlockBelowChest;
import com.sk89q.craftbook.gates.world.TimeControl;
import com.sk89q.craftbook.gates.world.TimeControlAdvanced;
import com.sk89q.craftbook.gates.world.TimeFaker;
import com.sk89q.craftbook.gates.world.WaterSensor;
import com.sk89q.craftbook.gates.world.WaterSensorST;
import com.sk89q.craftbook.gates.world.WirelessReceiver;
import com.sk89q.craftbook.gates.world.WirelessReceiverST;
import com.sk89q.craftbook.gates.world.WirelessTransmitter;
import com.sk89q.craftbook.ic.ICFamily;
import com.sk89q.craftbook.ic.ICManager;
import com.sk89q.craftbook.ic.ICMechanicFactory;
import com.sk89q.craftbook.ic.families.Family3ISO;
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
    protected ICManager icManager;
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
        if (!midi.exists())
            midi.mkdir();

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

        //SISOs
        icManager.register("MC1000", new Repeater.Factory(server), familySISO);
        icManager.register("MC1001", new Inverter.Factory(server), familySISO);
        icManager.register("MC1017", new ToggleFlipFlop.Factory(server, true), familySISO);
        icManager.register("MC1018", new ToggleFlipFlop.Factory(server, false), familySISO);
        icManager.register("MC1020", new RandomBit.Factory(server), familySISO);
        icManager.register("MC1025", new ServerTimeModulus.Factory(server), familySISO);
        icManager.register("MC1110", new WirelessTransmitter.Factory(server), familySISO);
        icManager.register("MC1111", new WirelessReceiver.Factory(server), familySISO);
        icManager.register("MC1200", new EntitySpawner.Factory(server), familySISO);     // Restricted
        icManager.register("MC1201", new ItemDispenser.Factory(server), familySISO);       // Restricted
        icManager.register("MC1202", new ChestDispenser.Factory(server), familySISO);      // Restricted
        icManager.register("MC1203", new LightningSummon.Factory(server), familySISO);     // Restricted
        icManager.register("MC1204", new EntityTrap.Factory(server), familySISO);     // Restricted
        icManager.register("MC1205", new SetBlockAbove.Factory(server), familySISO);             // Restricted
        icManager.register("MC1206", new SetBlockBelow.Factory(server), familySISO);             // Restricted
        icManager.register("MC1207", new FlexibleSetBlock.Factory(server), familySISO);          // Restricted
        icManager.register("MC1208", new MultipleSetBlock.Factory(server), familySISO);
        icManager.register("MC1209", new ChestCollector.Factory(server), familySISO);
        icManager.register("MC1210", new ParticleEffect.Factory(server), familySISO);                  // Restricted
        icManager.register("MC1215", new SetBlockAboveChest.Factory(server), familySISO);             // Restricted
        icManager.register("MC1216", new SetBlockBelowChest.Factory(server), familySISO);             // Restricted
        icManager.register("MC1217", new PotionInducer.Factory(server), familySISO);
        icManager.register("MC1230", new DaySensor.Factory(server), familySISO);
        icManager.register("MC1231", new TimeControl.Factory(server), familySISO);         // Restricted
        icManager.register("MC1236", new WeatherFaker.Factory(server), familySISO);        // Restricted
        icManager.register("MC1237", new TimeFaker.Factory(server), familySISO);           // Restricted
        icManager.register("MC1240", new ArrowShooter.Factory(server), familySISO);        // Restricted
        icManager.register("MC1241", new ArrowBarrage.Factory(server), familySISO);        // Restricted
        icManager.register("MC1250", new FireShooter.Factory(server), familySISO);         // Restricted
        icManager.register("MC1251", new FireBarrage.Factory(server), familySISO);         // Restricted
        icManager.register("MC1260", new WaterSensor.Factory(server), familySISO);
        icManager.register("MC1261", new LavaSensor.Factory(server), familySISO);
        icManager.register("MC1262", new LightSensor.Factory(server), familySISO);
        icManager.register("MC1263", new BlockSensor.Factory(server), familySISO);
        icManager.register("MC1270", new Melody.Factory(server), familySISO);
        icManager.register("MC1271", new Detection.Factory(server), familySISO);          // Restricted
        icManager.register("MC1420", new ClockDivider.Factory(server), familySISO);
        icManager.register("MC1510", new MessageSender.Factory(server), familySISO);
        icManager.register("MC2100", new Delayer.Factory(server), familySISO);
        icManager.register("MC2101", new NotDelayer.Factory(server), familySISO);
        icManager.register("MC2110", new LowDelayer.Factory(server), familySISO);
        icManager.register("MC2111", new NotLowDelayer.Factory(server), familySISO);
	    icManager.register("MC2500", new Pulser.Factory(server), familySISO);
	    icManager.register("MC2501", new NotPulser.Factory(server), familySISO);
	    icManager.register("MC2510", new LowPulser.Factory(server), familySISO);
	    icManager.register("MC2511", new LowNotPulser.Factory(server), familySISO);

        //SI3Os
        icManager.register("MC2020", new Random3Bit.Factory(server), familySI3O);
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
        icManager.register("MC3050", new CombinationLock.Factory(server), family3ISO);
        icManager.register("MC3101", new DownCounter.Factory(server), family3ISO);
        icManager.register("MC3102", new Counter.Factory(server), family3ISO);
        icManager.register("MC3231", new TimeControlAdvanced.Factory(server), family3ISO);        // Restricted
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
        icManager.register("MC0210", new ParticleEffectST.Factory(server), familySISO);
        icManager.register("MC0230", new DaySensorST.Factory(server), familySISO);
        icManager.register("MC0260", new WaterSensorST.Factory(server), familySISO);
        icManager.register("MC0261", new LavaSensorST.Factory(server), familySISO);
        icManager.register("MC0262", new LightSensorST.Factory(server), familySISO);
        icManager.register("MC0263", new BlockSensorST.Factory(server), familySISO);
        icManager.register("MC0271", new DetectionST.Factory(server), familySISO);      // Restricted
        icManager.register("MC0420", new Clock.Factory(server), familySISO);
        icManager.register("MC0421", new Monostable.Factory(server), familySISO);
        icManager.register("MC0500", new RangedOutput.Factory(server), familySISO);
        //Missing: 0020 self-triggered RNG (may cause server load issues)
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

    public CircuitsConfiguration getLocalConfiguration() {
        return config;
    }

    public PermissionsResolverManager getPermissionsResolver() {
        return perms;
    }
}
