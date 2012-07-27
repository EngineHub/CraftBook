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

import com.sk89q.craftbook.CircuitsConfiguration;
import com.sk89q.craftbook.LanguageManager;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.circuits.GlowStone;
import com.sk89q.craftbook.circuits.JackOLantern;
import com.sk89q.craftbook.circuits.Netherrack;
import com.sk89q.craftbook.gates.logic.*;
import com.sk89q.craftbook.gates.weather.*;
import com.sk89q.craftbook.gates.world.*;
import com.sk89q.craftbook.ic.ICFamily;
import com.sk89q.craftbook.ic.ICManager;
import com.sk89q.craftbook.ic.ICMechanicFactory;
import com.sk89q.craftbook.ic.families.Family3ISO;
import com.sk89q.craftbook.ic.families.FamilyAISO;
import com.sk89q.craftbook.ic.families.FamilySI3O;
import com.sk89q.craftbook.ic.families.FamilySISO;
import com.sk89q.wepif.PermissionsResolverManager;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;

import java.io.File;
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
        if (!midi.exists()) midi.mkdir();

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
        ICFamily familyAISO = new FamilyAISO();
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
        icManager.register("MC1211", new SetBridge.Factory(server), familySISO);                  // Restricted
        icManager.register("MC1212", new SetDoor.Factory(server), familySISO);                  // Restricted
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
        icManager.register("MC1272", new PlayerDetection.Factory(server), familySISO);          // Restricted
        icManager.register("MC1299", new ParticleEffect.Factory(server), familySISO);       // Restricted
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
        icManager.register("MC0272", new PlayerDetectionST.Factory(server), familySISO);      // Restricted
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

        // AISOs - copied from the above, SISOs
        icManager.register("MCA1000", new Repeater.Factory(server), familyAISO);
        icManager.register("MCA1001", new Inverter.Factory(server), familyAISO);
        icManager.register("MCA1017", new ToggleFlipFlop.Factory(server, true), familyAISO);
        icManager.register("MCA1018", new ToggleFlipFlop.Factory(server, false), familyAISO);
        icManager.register("MCA1020", new RandomBit.Factory(server), familyAISO);
        icManager.register("MCA1025", new ServerTimeModulus.Factory(server), familyAISO);
        icManager.register("MCA1110", new WirelessTransmitter.Factory(server), familyAISO);
        icManager.register("MCA1111", new WirelessReceiver.Factory(server), familyAISO);
        icManager.register("MCA1200", new EntitySpawner.Factory(server), familyAISO);     // Restricted
        icManager.register("MCA1201", new ItemDispenser.Factory(server), familyAISO);       // Restricted
        icManager.register("MCA1202", new ChestDispenser.Factory(server), familyAISO);      // Restricted
        icManager.register("MCA1203", new LightningSummon.Factory(server), familyAISO);     // Restricted
        icManager.register("MCA1204", new EntityTrap.Factory(server), familyAISO);     // Restricted
        icManager.register("MCA1205", new SetBlockAbove.Factory(server), familyAISO);             // Restricted
        icManager.register("MCA1206", new SetBlockBelow.Factory(server), familyAISO);             // Restricted
        icManager.register("MCA1207", new FlexibleSetBlock.Factory(server), familyAISO);          // Restricted
        icManager.register("MCA1208", new MultipleSetBlock.Factory(server), familyAISO);
        icManager.register("MCA1209", new ChestCollector.Factory(server), familyAISO);
        icManager.register("MCA1210", new ParticleEffect.Factory(server), familyAISO);                  // Restricted
        icManager.register("MCA1211", new SetBridge.Factory(server), familyAISO);                  // Restricted
        icManager.register("MCA1212", new SetDoor.Factory(server), familyAISO);                  // Restricted
        icManager.register("MCA1215", new SetBlockAboveChest.Factory(server), familyAISO);             // Restricted
        icManager.register("MCA1216", new SetBlockBelowChest.Factory(server), familyAISO);             // Restricted
        icManager.register("MCA1217", new PotionInducer.Factory(server), familyAISO);
        icManager.register("MCA1230", new DaySensor.Factory(server), familyAISO);
        icManager.register("MCA1231", new TimeControl.Factory(server), familyAISO);         // Restricted
        icManager.register("MCA1236", new WeatherFaker.Factory(server), familyAISO);        // Restricted
        icManager.register("MCA1237", new TimeFaker.Factory(server), familyAISO);           // Restricted
        icManager.register("MCA1240", new ArrowShooter.Factory(server), familyAISO);        // Restricted
        icManager.register("MCA1241", new ArrowBarrage.Factory(server), familyAISO);        // Restricted
        icManager.register("MCA1250", new FireShooter.Factory(server), familyAISO);         // Restricted
        icManager.register("MCA1251", new FireBarrage.Factory(server), familyAISO);         // Restricted
        icManager.register("MCA1260", new WaterSensor.Factory(server), familyAISO);
        icManager.register("MCA1261", new LavaSensor.Factory(server), familyAISO);
        icManager.register("MCA1262", new LightSensor.Factory(server), familyAISO);
        icManager.register("MCA1263", new BlockSensor.Factory(server), familyAISO);
        icManager.register("MCA1270", new Melody.Factory(server), familyAISO);
        icManager.register("MCA1271", new Detection.Factory(server), familyAISO);          // Restricted
        icManager.register("MCA1272", new PlayerDetection.Factory(server), familySISO);          // Restricted
        icManager.register("MCA1299", new ParticleEffect.Factory(server), familyAISO);      // Restricted
        icManager.register("MCA1420", new ClockDivider.Factory(server), familyAISO);
        icManager.register("MCA1510", new MessageSender.Factory(server), familyAISO);
        icManager.register("MCA2100", new Delayer.Factory(server), familyAISO);
        icManager.register("MCA2101", new NotDelayer.Factory(server), familyAISO);
        icManager.register("MCA2110", new LowDelayer.Factory(server), familyAISO);
        icManager.register("MCA2111", new NotLowDelayer.Factory(server), familyAISO);
        icManager.register("MCA2500", new Pulser.Factory(server), familyAISO);
        icManager.register("MCA2501", new NotPulser.Factory(server), familyAISO);
        icManager.register("MCA2510", new LowPulser.Factory(server), familyAISO);
        icManager.register("MCA2511", new LowNotPulser.Factory(server), familyAISO);
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
