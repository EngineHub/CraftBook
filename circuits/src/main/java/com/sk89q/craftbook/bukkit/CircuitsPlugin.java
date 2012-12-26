// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.bukkit;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.BukkitMetrics.Graph;
import com.sk89q.craftbook.bukkit.commands.CircuitCommands;
import com.sk89q.craftbook.circuits.GlowStone;
import com.sk89q.craftbook.circuits.JackOLantern;
import com.sk89q.craftbook.circuits.Netherrack;
import com.sk89q.craftbook.circuits.Pipes;
import com.sk89q.craftbook.gates.logic.*;
import com.sk89q.craftbook.gates.world.blocks.*;
import com.sk89q.craftbook.gates.world.entity.*;
import com.sk89q.craftbook.gates.world.items.*;
import com.sk89q.craftbook.gates.world.miscellaneous.*;
import com.sk89q.craftbook.gates.world.sensors.*;
import com.sk89q.craftbook.gates.world.weather.*;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.ic.families.*;
import com.sk89q.craftbook.plc.PlcFactory;
import com.sk89q.craftbook.plc.lang.Perlstone;
import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.wepif.PermissionsResolverManager;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

// import com.sk89q.bukkit.migration.*;

/**
 * Plugin for CraftBook's redstone additions.
 *
 * @author sk89q
 */
public class CircuitsPlugin extends BaseBukkitPlugin {

    public static final ICFamily FAMILY_SISO = new FamilySISO();
    public static final ICFamily FAMILY_3ISO = new Family3ISO();
    public static final ICFamily FAMILY_SI3O = new FamilySI3O();
    public static final ICFamily FAMILY_AISO = new FamilyAISO();
    public static final ICFamily FAMILY_3I3O = new Family3I3O();
    public static final ICFamily FAMILY_VIVO = new FamilyVIVO();
    public static final ICFamily FAMILY_SI5O = new FamilySI5O();

    protected CircuitsConfiguration config;
    protected ICConfiguration icConfig;
    public ICManager icManager;
    private PermissionsResolverManager perms;
    private MechanicManager manager;
    private static CircuitsPlugin instance;

    public static Server server;

    public static CircuitsPlugin getInst() {

        return instance;
    }

    public ICMechanicFactory icFactory;

    public Pipes.Factory pipeFactory;

    public File romFolder;
    public File midiFolder;

    @Override
    public void onEnable() {

        super.onEnable();

        instance = this;
        server = getServer();

        registerCommand(CircuitCommands.class);

        createDefaultConfiguration("config.yml", false);
        createDefaultConfiguration("ic-config.yml", false);
        createDefaultConfiguration("custom-ics.txt", false);
        config = new CircuitsConfiguration(getConfig(), getDataFolder());
        saveConfig();

        PermissionsResolverManager.initialize(this);
        perms = PermissionsResolverManager.getInstance();

        manager = new MechanicManager(this);
        MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
        adapter.register(manager);

        midiFolder = new File(getDataFolder(), "midi/");
        if (!midiFolder.exists()) {
            midiFolder.mkdir();
        }

        romFolder = new File(getDataFolder(), "rom/");
        if (!romFolder.exists()) {
            romFolder.mkdir();
        }

        registerMechanics();

        // Register events
        registerEvents();

        languageManager = new LanguageManager(this);

        try {
            BukkitMetrics metrics = new BukkitMetrics(this);

            Graph graph = metrics.createGraph("Language");
            for (String lan : languageManager.getLanguages()) {
                graph.addPlotter(new BukkitMetrics.Plotter(lan) {

                    @Override
                    public int getValue() {

                        return 1;
                    }
                });
            }

            metrics.start();
        } catch (Exception e) {
            getLogger().severe(GeneralUtil.getStackTrace(e));
        }
    }

    private void registerMechanics() {

        if (config.icSettings.enabled) {
            registerICs();
            icConfig = new ICConfiguration(YamlConfiguration.loadConfiguration(new File(getDataFolder(),
                    "ic-config.yml")), getDataFolder());
            try {
                icConfig.cfg.save(new File(getDataFolder(), "ic-config.yml"));
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Could not save IC Config", ex);
            }
        }

        // Let's register mechanics!
        if (config.enableNetherstone) {
            registerMechanic(new Netherrack.Factory());
        }
        if (config.enablePumpkins) {
            registerMechanic(new JackOLantern.Factory(this));
        }
        if (config.enableGlowStone) {
            registerMechanic(new GlowStone.Factory(this));
        }
        if (config.icSettings.enabled) {
            registerMechanic(icFactory = new ICMechanicFactory(this, icManager));
            setupSelfTriggered();
        }
        if (config.pipeSettings.enabled) {
            registerMechanic(pipeFactory = new Pipes.Factory(this));
        }
    }

    /**
     * Register ICs.
     */
    private void registerICs() {

        Server server = getServer();

        // Let's register ICs!
        icManager = new ICManager();
        ICFamily familySISO = FAMILY_SISO;
        ICFamily family3ISO = FAMILY_3ISO;
        ICFamily familySI3O = FAMILY_SI3O;
        ICFamily familyAISO = FAMILY_AISO;
        ICFamily family3I3O = FAMILY_3I3O;
        ICFamily familyVIVO = FAMILY_VIVO;
        ICFamily familySI5O = FAMILY_SI5O;

        // SISOs
        registerIC("MC1000", "repeater", new Repeater.Factory(server), familySISO, familyAISO);
        registerIC("MC1001", "inverter", new Inverter.Factory(server), familySISO, familyAISO);
        registerIC("MC1017", "re t flip", new ToggleFlipFlop.Factory(server, true), familySISO, familyAISO);
        registerIC("MC1018", "fe t flip", new ToggleFlipFlop.Factory(server, false), familySISO, familyAISO);
        registerIC("MC1020", "random bit", new RandomBit.Factory(server), familySISO, familyAISO);
        registerIC("MC1025", "server time", new ServerTimeModulus.Factory(server), familySISO, familyAISO);
        registerIC("MC1110", "transmitter", new WirelessTransmitter.Factory(server), familySISO, familyAISO);
        registerIC("MC1111", "receiver", new WirelessReceiver.Factory(server), familySISO, familyAISO);
        registerIC("MC1112", "tele-out", new TeleportTransmitter.Factory(server), familySISO, familyAISO);
        registerIC("MC1113", "tele-in", new TeleportReciever.Factory(server), familySISO, familyAISO);
        registerIC("MC1200", "spawner", new CreatureSpawner.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1201", "dispenser", new ItemDispenser.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1202", "c dispense", new ContainerDispenser.Factory(server), familySISO,
                familyAISO); // Restricted
        registerIC("MC1203", "strike", new LightningSummon.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1204", "trap", new EntityTrap.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1205", "set above", new SetBlockAbove.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1206", "set below", new SetBlockBelow.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1207", "flex set", new FlexibleSetBlock.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1208", "mult set", new MultipleSetBlock.Factory(server), familySISO, familyAISO);
        registerIC("MC1209", "collector", new ContainerCollector.Factory(server), familySISO, familyAISO);
        registerIC("MC1210", "emitter", new ParticleEffect.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1211", "set bridge", new SetBridge.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1212", "set door", new SetDoor.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1213", "sound", new SoundEffect.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1215", "set a chest", new SetBlockAboveChest.Factory(server), familySISO,
                familyAISO); // Restricted
        registerIC("MC1216", "set b chest", new SetBlockBelowChest.Factory(server), familySISO,
                familyAISO); // Restricted
        registerIC("MC1217", "pot induce", new PotionInducer.Factory(server), familySISO, familyAISO);
        registerIC("MC1218", "block launch", new BlockLauncher.Factory(server), familySISO, familyAISO);
        registerIC("MC1219", "auto craft", new AutomaticCrafter.Factory(server), familySISO, familyAISO);
        registerIC("MC1220", "a b break", new BlockBreaker.Factory(server, false), familySISO, familyAISO);
        registerIC("MC1221", "b b break", new BlockBreaker.Factory(server, true), familySISO, familyAISO);
        registerIC("MC1222", "liquid flood", new LiquidFlood.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1223", "terraform", new BonemealTerraformer.Factory(server), familySISO, familyAISO);
        registerIC("MC1224", "time bomb", new TimedExplosion.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1225", "pump", new Pump.Factory(server), familySISO, familyAISO);
        registerIC("MC1226", "spigot", new Spigot.Factory(server), familySISO, familyAISO);
        registerIC("MC1227", "avd spawner", new AdvancedEntitySpawner.Factory(server), familySISO,
                familyAISO); // Restricted
        registerIC("MC1228", "ent cannon", new EntityCannon.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1229", "sorter", new Sorter.Factory(server), familySISO, familyAISO);
        registerIC("MC1230", "sense day", new DaySensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1231", "t control", new TimeControl.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1232", "time set", new TimeSet.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1233", "item fan", new ItemFan.Factory(server), familySISO, familyAISO);
        registerIC("MC1234", "planter", new Planter.Factory(server), familySISO, familyAISO);
        registerIC("MC1235", "cultivator", new Cultivator.Factory(server), familySISO, familyAISO);
        registerIC("MC1236", "fake weather", new WeatherFaker.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1237", "fake time", new TimeFaker.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1238", "irrigate", new Irrigator.Factory(server), familySISO, familyAISO);
        registerIC("MC1239", "harvester", new CombineHarvester.Factory(server), familySISO, familyAISO);
        registerIC("MC1240", "shoot arrow", new ArrowShooter.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1241", "shoot arrows", new ArrowBarrage.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1242", "stocker", new ChestStocker.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1250", "shoot fire", new FireShooter.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1251", "shoot fires", new FireBarrage.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1252", "flame thower", new FlameThrower.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1260", "sense water", new WaterSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1261", "sense lava", new LavaSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1262", "sense light", new LightSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1263", "sense block", new BlockSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1264", "sense item", new ItemSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1265", "inv sense item", new ItemNotSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1266", "sense power", new PowerSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1267", "sense move", new MovementSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1270", "melody", new Melody.Factory(server), familySISO, familyAISO);
        registerIC("MC1271", "sense entity", new EntitySensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1272", "sense player", new PlayerSensor.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1420", "divide clock", new ClockDivider.Factory(server), familySISO, familyAISO);
        registerIC("MC1421", "clock", new Clock.Factory(server), familySISO, familyAISO);
        registerIC("MC1510", "send message", new MessageSender.Factory(server), familySISO, familyAISO);
        registerIC("MC2100", "delayer", new Delayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2101", "inv delayer", new NotDelayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2110", "fe delayer", new LowDelayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2111", "inv fe delayer", new NotLowDelayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2500", "pulser", new Pulser.Factory(server), familySISO, familyAISO);
        registerIC("MC2501", "inv pulser", new NotPulser.Factory(server), familySISO, familyAISO);
        registerIC("MC2510", "fe pulser", new LowPulser.Factory(server), familySISO, familyAISO);
        registerIC("MC2511", "inv fe pulser", new LowNotPulser.Factory(server), familySISO, familyAISO);

        // SI3Os
        registerIC("MC2020", "random 3", new Random3Bit.Factory(server), familySI3O);
        registerIC("MC2999", "marquee", new Marquee.Factory(server), familySI3O);

        // 3ISOs
        registerIC("MC3002", "and", new AndGate.Factory(server), family3ISO);
        registerIC("MC3003", "nand", new NandGate.Factory(server), family3ISO);
        registerIC("MC3020", "xor", new XorGate.Factory(server), family3ISO);
        registerIC("MC3021", "xnor", new XnorGate.Factory(server), family3ISO);
        registerIC("MC3030", "nor flip", new RsNorFlipFlop.Factory(server), family3ISO);
        registerIC("MC3031", "inv nand latch", new InvertedRsNandLatch.Factory(server), family3ISO);
        registerIC("MC3032", "jk flip", new JkFlipFlop.Factory(server), family3ISO);
        registerIC("MC3033", "nand latch", new RsNandLatch.Factory(server), family3ISO);
        registerIC("MC3034", "edge df flip", new EdgeTriggerDFlipFlop.Factory(server), family3ISO);
        registerIC("MC3036", "level df flip", new LevelTriggeredDFlipFlop.Factory(server), family3ISO);
        registerIC("MC3040", "multiplexer", new Multiplexer.Factory(server), family3ISO);
        registerIC("MC3050", "combo", new CombinationLock.Factory(server), family3ISO);
        registerIC("MC3101", "down counter", new DownCounter.Factory(server), family3ISO);
        registerIC("MC3102", "counter", new Counter.Factory(server), family3ISO);
        registerIC("MC3231", "t control adva", new TimeControlAdvanced.Factory(server), family3ISO); // Restricted
        registerIC("MC3300", "ROM set", new MemorySetter.Factory(server), family3ISO); // Restricted
        registerIC("MC3301", "ROM get", new MemoryAccess.Factory(server), familySI3O); // Restricted
        // 3I3Os
        registerIC("MC4000", "full adder", new FullAdder.Factory(server), family3I3O);
        registerIC("MC4010", "half adder", new HalfAdder.Factory(server), family3I3O);
        registerIC("MC4100", "full subtr", new FullSubtractor.Factory(server), family3I3O);
        registerIC("MC4110", "half subtr", new HalfSubtractor.Factory(server), family3I3O);
        registerIC("MC4200", "dispatcher", new Dispatcher.Factory(server), family3I3O);

        // SI5O's
        registerIC("MC6020", "random 5", new Random5Bit.Factory(server), familySI5O);

        // PLCs
        registerIC("MC5000", "perlstone", PlcFactory.fromLang(server, new Perlstone(), false), familyVIVO);
        registerIC("MC5001", "perlstone 3i3o", PlcFactory.fromLang(server, new Perlstone(), false), family3I3O);

        // Self triggered
        registerIC("MC0020", "random 1 st", new RandomBitST.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC0111", "receiver st", new WirelessReceiverST.Factory(server), familySISO, familyAISO);
        registerIC("MC0113", "tele-in st", new TeleportRecieverST.Factory(server), familySISO, familyAISO);
        registerIC("MC0202", "c dispense st", new ContainerDispenserST.Factory(server), familySISO, familyAISO);
        registerIC("MC0204", "trap st", new EntityTrapST.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC0205", "set above st", new SetBlockAboveST.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC0206", "set below st", new SetBlockBelowST.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC0209", "collector st", new ContainerCollectorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0210", "emitter st", new ParticleEffectST.Factory(server), familySISO, familyAISO);
        registerIC("MC0215", "set a chest st", new SetBlockAboveChestST.Factory(server), familySISO, familyAISO);
        registerIC("MC0216", "set b chest st", new SetBlockBelowChestST.Factory(server), familySISO, familyAISO);
        registerIC("MC0217", "pot induce st", new PotionInducerST.Factory(server), familySISO,
                familyAISO); // Restricted
        registerIC("MC0219", "auto craft st", new AutomaticCrafterST.Factory(server), familySISO, familyAISO);
        registerIC("MC0220", "a bl break st", new BlockBreakerST.Factory(server, false), familySISO, familyAISO);
        registerIC("MC0221", "b bl break st", new BlockBreakerST.Factory(server, true), familySISO, familyAISO);
        registerIC("MC0222", "liq flood st", new LiquidFloodST.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC0223", "terraform st", new BonemealTerraformerST.Factory(server), familySISO, familyAISO);
        registerIC("MC0225", "pump st", new PumpST.Factory(server), familySISO, familyAISO);
        registerIC("MC0228", "ent cannon st", new EntityCannonST.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC0229", "sorter st", new SorterST.Factory(server), familySISO, familyAISO);
        registerIC("MC0230", "sense day st", new DaySensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0232", "time set st", new TimeSetST.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC0233", "item fan st", new ItemFanST.Factory(server), familySISO, familyAISO);
        registerIC("MC0234", "planter st", new PlanterST.Factory(server), familySISO, familyAISO);
        registerIC("MC0235", "cultivator st", new CultivatorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0238", "irrigate st", new IrrigatorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0239", "harvester st", new CombineHarvesterST.Factory(server), familySISO, familyAISO);
        registerIC("MC0242", "stocker st", new ChestStockerST.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC0260", "sense water st", new WaterSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0261", "sense lava st", new LavaSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0262", "sense light st", new LightSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0263", "sense block st", new BlockSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0264", "sense item st", new ItemSensorST.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC0265", "sense n item s", new ItemNotSensorST.Factory(server), familySISO,
                familyAISO); // Restricted
        registerIC("MC0266", "sense power st", new PowerSensorST.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC0267", "sense move st", new MovementSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0270", "sense power st", new PowerSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0271", "sense entit st", new EntitySensorST.Factory(server), familySISO,
                familyAISO); // Restricted
        registerIC("MC0272", "sense playe st", new PlayerSensorST.Factory(server), familySISO,
                familyAISO); // Restricted
        registerIC("MC0420", "clock st", new ClockST.Factory(server), familySISO, familyAISO);
        registerIC("MC0421", "monostable", new Monostable.Factory(server), familySISO, familyAISO);
        registerIC("MC0500", "range output", new RangedOutput.Factory(server), familySISO, familyAISO);
        // Xtra ICs
        // SISOs
        registerIC("MCX230", "rain sense", new RainSensor.Factory(server), familySISO, familyAISO);
        registerIC("MCX231", "storm sense", new TStormSensor.Factory(server), familySISO, familyAISO);
        registerIC("MCX233", "weather set", new WeatherControl.Factory(server), familySISO, familyAISO);
        // 3ISOs
        registerIC("MCT233", "weather set ad", new WeatherControlAdvanced.Factory(server), family3ISO);
        // Self triggered
        registerIC("MCZ230", "rain sense st", new RainSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MCZ231", "storm sense st", new TStormSensorST.Factory(server), familySISO, familyAISO);
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

        logger.info("CraftBook Circuits: " + numChunks + " chunk(s) for " + numWorlds + " world(s) processed " + "("
                + Math.round(time / 1000.0 * 10)
                / 10 + "s elapsed)");

        // Set up the clock for self-triggered ICs.
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new MechanicClock(manager), 0, 2);
    }

    @Override
    protected void registerEvents() {

        // if (getLocalConfiguration().enableICs) {
        // TODO reimplement torches getServer().getPluginManager().registerEvents(new ICUtil.ICListener(), this);
        // }
    }

    @Override
    public CircuitsConfiguration getLocalConfiguration() {

        return config;
    }

    public ICConfiguration getICConfiguration() {

        return icConfig;
    }

    public PermissionsResolverManager getPermissionsResolver() {

        return perms;
    }

    public List<RegisteredICFactory> getICList() {

        List<RegisteredICFactory> ics = new ArrayList<RegisteredICFactory>();
        for (Entry<String, RegisteredICFactory> e : icManager.registered.entrySet()) {
            ics.add(e.getValue());
        }
        return ics;
    }

    /**
     * Register a mechanic if possible
     *
     * @param name
     * @param factory
     * @param families
     */
    public boolean registerIC(String name, String longName, ICFactory factory, ICFamily... families) {

        if (config.icSettings.disabledICs.contains(name)) return false;
        return icManager.register(name, longName, factory, families);
    }

    /**
     * Register a mechanic if possible
     *
     * @param factory
     */
    public void registerMechanic(MechanicFactory<? extends Mechanic> factory) {

        manager.register(factory);
    }

    /**
     * Register a array of mechanics if possible
     *
     * @param factories
     */
    protected void registerMechanic(MechanicFactory<? extends Mechanic>[] factories) {

        for (MechanicFactory<? extends Mechanic> aFactory : factories) {
            registerMechanic(aFactory);
        }
    }

    /**
     * Unregister a mechanic if possible TODO Ensure no remnants are left behind
     *
     * @param factory
     *
     * @return true if the mechanic was successfully unregistered.
     */
    protected boolean unregisterMechanic(MechanicFactory<? extends Mechanic> factory) {

        return manager.unregister(factory);
    }

    protected boolean unregisterAllMechanics() {

        boolean ret = true;

        Iterator<MechanicFactory<? extends Mechanic>> iterator = manager.factories.iterator();
        ;
        while (iterator.hasNext()) {
            if (!unregisterMechanic(iterator.next())) ret = false;
        }

        return ret;
    }

    public void generateICDocs(Player player, String id) {

        RegisteredICFactory ric = icManager.registered.get(id.toLowerCase());
        if (ric == null) {
            try {
                ric = icManager.registered.get(getSearchID(player, id));
                if (ric == null) {
                    player.sendMessage(ChatColor.RED + "Invalid IC!");
                    return;
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Invalid IC!");
                return;
            }
        }
        try {
            IC ic = ric.getFactory().create(null);
            player.sendMessage("    "); // To space the area
            player.sendMessage(ChatColor.BLUE + ic.getTitle() + " (" + ric.getId() + ") Documentation");
            if (getLocalConfiguration().icSettings.shorthand && ric.getShorthand() != null) {
                player.sendMessage(ChatColor.YELLOW + "Shorthand: =" + ric.getShorthand());
            }
            player.sendMessage(ChatColor.YELLOW + "Desc: " + ric.getFactory().getDescription());
            if (ric.getFactory().getLineHelp()[0] != null) {
                player.sendMessage(ChatColor.YELLOW + "Line 3: " + ric.getFactory().getLineHelp()[0]);
            } else {
                player.sendMessage(ChatColor.YELLOW + "Line 3: Blank.");
            }
            if (ric.getFactory().getLineHelp()[1] != null) {
                player.sendMessage(ChatColor.YELLOW + "Line 4: " + ric.getFactory().getLineHelp()[1]);
            } else {
                player.sendMessage(ChatColor.YELLOW + "Line 4: Blank.");
            }
            player.sendMessage(ChatColor.AQUA + "Wiki: " + "http://wiki.sk89q.com/wiki/CraftBook/" + ric.getId()
                    .toUpperCase());
        } catch (Exception ignored) {
        }
    }

    public String getSearchID(Player p, String search) {

        ArrayList<String> icNameList = new ArrayList<String>();
        icNameList.addAll(icManager.registered.keySet());

        Collections.sort(icNameList);

        for (String ic : icNameList) {
            try {
                RegisteredICFactory ric = icManager.registered.get(ic);
                IC tic = ric.getFactory().create(null);
                if (search != null && !tic.getTitle().toLowerCase().contains(search.toLowerCase())
                        && !ric.getId().toLowerCase().contains(search.toLowerCase())) continue;

                return ic;
            } catch (Exception ignored) {
            }
        }

        return "";
    }

    /**
     * Used for the /listics command.
     *
     * @param p
     *
     * @return
     */
    public String[] generateICText(Player p, String search, char[] parameters) {

        ArrayList<String> icNameList = new ArrayList<String>();
        icNameList.addAll(icManager.registered.keySet());

        Collections.sort(icNameList);

        ArrayList<String> strings = new ArrayList<String>();
        boolean col = true;
        for (String ic : icNameList) {
            try {
                thisIC:
                {
                    RegisteredICFactory ric = icManager.registered.get(ic);
                    IC tic = ric.getFactory().create(null);
                    if (search != null && !tic.getTitle().toLowerCase().contains(search.toLowerCase())
                            && !ric.getId().toLowerCase().contains(search.toLowerCase())) continue;
                    if (parameters != null) {
                        for (char c : parameters) {
                            if (c == 'r' && !(ric.getFactory() instanceof RestrictedIC)) break thisIC;
                            else if (c == 's' && ric.getFactory() instanceof RestrictedIC) break thisIC;
                            else if (c == 'b' && !ric.getFactory().getClass().getPackage().getName().endsWith("blocks"))
                                break thisIC;
                            else if (c == 'i' && !ric.getFactory().getClass().getPackage().getName().endsWith("items"))
                                break thisIC;
                            else if (c == 'e' && !ric.getFactory().getClass().getPackage().getName().endsWith("entity"))
                                break thisIC;
                            else if (c == 'w' && !ric.getFactory().getClass().getPackage().getName().endsWith
                                    ("weather"))
                                break thisIC;
                            else if (c == 'l' && !ric.getFactory().getClass().getPackage().getName().endsWith("logic"))
                                break thisIC;
                            else if (c == 'm' && !ric.getFactory().getClass().getPackage().getName().endsWith
                                    ("miscellaneous"))
                                break thisIC;
                            else if (c == 'c' && !ric.getFactory().getClass().getPackage().getName().endsWith
                                    ("sensors"))
                                break thisIC;

                        }
                    }
                    col = !col;
                    ChatColor colour = col ? ChatColor.YELLOW : ChatColor.GOLD;

                    if (ric.getFactory() instanceof RestrictedIC) {
                        if (!p.hasPermission("craftbook.ic.restricted." + ic.toLowerCase())) {
                            colour = col ? ChatColor.RED : ChatColor.DARK_RED;
                        }
                    } else if (!p.hasPermission("craftbook.ic.safe." + ic.toLowerCase())) {
                        colour = col ? ChatColor.RED : ChatColor.DARK_RED;
                    }
                    strings.add(colour + tic.getTitle() + " (" + ric.getId() + ")" + ": " + (tic instanceof
                                                                                                     SelfTriggeredIC
                                                                                             ? "ST " : "T ")
                            + (ric.getFactory() instanceof RestrictedIC ? ChatColor.DARK_RED + "R " : ""));
                }
            } catch (Throwable e) {
                Bukkit.getLogger().severe("An error occured generating the docs for IC: " + ic + ". Please report" +
                        " it to Me4502");
            }
        }

        return strings.toArray(new String[strings.size()]);
    }

    public void reloadICConfiguration() {

        icConfig = new ICConfiguration(YamlConfiguration.loadConfiguration(new File(getDataFolder(),
                "ic-config.yml")), getDataFolder());
        try {
            icConfig.cfg.save(new File(getDataFolder(), "ic-config.yml"));
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save IC Config", ex);
        }
    }

    @Override
    public void reloadConfiguration() {

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

            @Override
            public void run() {
                // Unload everything.
                unregisterAllMechanics();
                HandlerList.unregisterAll(CircuitsPlugin.getInst());

                // Reload the config
                reloadConfig();
                config = new CircuitsConfiguration(getConfig(), getDataFolder());
                saveConfig();

                // Load everything
                registerMechanics();
                registerEvents();
            }
        });
    }
}
