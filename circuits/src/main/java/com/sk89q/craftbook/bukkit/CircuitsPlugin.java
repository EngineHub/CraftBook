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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.CircuitsConfiguration;
import com.sk89q.craftbook.ICConfiguration;
import com.sk89q.craftbook.LanguageManager;
import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.MechanicFactory;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.bukkit.Metrics.Graph;
import com.sk89q.craftbook.bukkit.commands.CircuitCommands;
import com.sk89q.craftbook.circuits.GlowStone;
import com.sk89q.craftbook.circuits.JackOLantern;
import com.sk89q.craftbook.circuits.Netherrack;
import com.sk89q.craftbook.gates.logic.AndGate;
import com.sk89q.craftbook.gates.logic.Clock;
import com.sk89q.craftbook.gates.logic.ClockDivider;
import com.sk89q.craftbook.gates.logic.ClockST;
import com.sk89q.craftbook.gates.logic.CombinationLock;
import com.sk89q.craftbook.gates.logic.Counter;
import com.sk89q.craftbook.gates.logic.Delayer;
import com.sk89q.craftbook.gates.logic.Dispatcher;
import com.sk89q.craftbook.gates.logic.DownCounter;
import com.sk89q.craftbook.gates.logic.EdgeTriggerDFlipFlop;
import com.sk89q.craftbook.gates.logic.FullAdder;
import com.sk89q.craftbook.gates.logic.FullSubtractor;
import com.sk89q.craftbook.gates.logic.HalfAdder;
import com.sk89q.craftbook.gates.logic.HalfSubtractor;
import com.sk89q.craftbook.gates.logic.InvertedRsNandLatch;
import com.sk89q.craftbook.gates.logic.Inverter;
import com.sk89q.craftbook.gates.logic.JkFlipFlop;
import com.sk89q.craftbook.gates.logic.LevelTriggeredDFlipFlop;
import com.sk89q.craftbook.gates.logic.LowDelayer;
import com.sk89q.craftbook.gates.logic.LowNotPulser;
import com.sk89q.craftbook.gates.logic.LowPulser;
import com.sk89q.craftbook.gates.logic.Marquee;
import com.sk89q.craftbook.gates.logic.MemoryAccess;
import com.sk89q.craftbook.gates.logic.MemorySetter;
import com.sk89q.craftbook.gates.logic.Monostable;
import com.sk89q.craftbook.gates.logic.Multiplexer;
import com.sk89q.craftbook.gates.logic.NandGate;
import com.sk89q.craftbook.gates.logic.NotDelayer;
import com.sk89q.craftbook.gates.logic.NotLowDelayer;
import com.sk89q.craftbook.gates.logic.NotPulser;
import com.sk89q.craftbook.gates.logic.Pulser;
import com.sk89q.craftbook.gates.logic.Random3Bit;
import com.sk89q.craftbook.gates.logic.Random5Bit;
import com.sk89q.craftbook.gates.logic.RandomBit;
import com.sk89q.craftbook.gates.logic.RandomBitST;
import com.sk89q.craftbook.gates.logic.RangedOutput;
import com.sk89q.craftbook.gates.logic.Repeater;
import com.sk89q.craftbook.gates.logic.RsNandLatch;
import com.sk89q.craftbook.gates.logic.RsNorFlipFlop;
import com.sk89q.craftbook.gates.logic.ToggleFlipFlop;
import com.sk89q.craftbook.gates.logic.XnorGate;
import com.sk89q.craftbook.gates.logic.XorGate;
import com.sk89q.craftbook.gates.world.blocks.BlockBreaker;
import com.sk89q.craftbook.gates.world.blocks.BlockBreakerST;
import com.sk89q.craftbook.gates.world.blocks.BlockLauncher;
import com.sk89q.craftbook.gates.world.blocks.BlockSensor;
import com.sk89q.craftbook.gates.world.blocks.BlockSensorST;
import com.sk89q.craftbook.gates.world.blocks.BonemealTerraformer;
import com.sk89q.craftbook.gates.world.blocks.BonemealTerraformerST;
import com.sk89q.craftbook.gates.world.blocks.Cultivator;
import com.sk89q.craftbook.gates.world.blocks.CultivatorST;
import com.sk89q.craftbook.gates.world.blocks.FlexibleSetBlock;
import com.sk89q.craftbook.gates.world.blocks.LavaSensor;
import com.sk89q.craftbook.gates.world.blocks.LavaSensorST;
import com.sk89q.craftbook.gates.world.blocks.LiquidFlood;
import com.sk89q.craftbook.gates.world.blocks.LiquidFloodST;
import com.sk89q.craftbook.gates.world.blocks.MultipleSetBlock;
import com.sk89q.craftbook.gates.world.blocks.Pump;
import com.sk89q.craftbook.gates.world.blocks.PumpST;
import com.sk89q.craftbook.gates.world.blocks.SetBlockAbove;
import com.sk89q.craftbook.gates.world.blocks.SetBlockAboveChest;
import com.sk89q.craftbook.gates.world.blocks.SetBlockBelow;
import com.sk89q.craftbook.gates.world.blocks.SetBlockBelowChest;
import com.sk89q.craftbook.gates.world.blocks.SetBridge;
import com.sk89q.craftbook.gates.world.blocks.SetDoor;
import com.sk89q.craftbook.gates.world.blocks.Spigot;
import com.sk89q.craftbook.gates.world.blocks.WaterSensor;
import com.sk89q.craftbook.gates.world.blocks.WaterSensorST;
import com.sk89q.craftbook.gates.world.entity.AdvancedEntitySpawner;
import com.sk89q.craftbook.gates.world.entity.CreatureSpawner;
import com.sk89q.craftbook.gates.world.entity.EntityCannon;
import com.sk89q.craftbook.gates.world.entity.EntityCannonST;
import com.sk89q.craftbook.gates.world.entity.EntitySensor;
import com.sk89q.craftbook.gates.world.entity.EntitySensorST;
import com.sk89q.craftbook.gates.world.entity.EntityTrap;
import com.sk89q.craftbook.gates.world.entity.EntityTrapST;
import com.sk89q.craftbook.gates.world.entity.PlayerDetection;
import com.sk89q.craftbook.gates.world.entity.PlayerDetectionST;
import com.sk89q.craftbook.gates.world.items.AutomaticCrafter;
import com.sk89q.craftbook.gates.world.items.AutomaticCrafterST;
import com.sk89q.craftbook.gates.world.items.ContainerCollector;
import com.sk89q.craftbook.gates.world.items.ContainerCollectorST;
import com.sk89q.craftbook.gates.world.items.ContainerDispenser;
import com.sk89q.craftbook.gates.world.items.ItemDispenser;
import com.sk89q.craftbook.gates.world.items.ItemFan;
import com.sk89q.craftbook.gates.world.items.ItemFanST;
import com.sk89q.craftbook.gates.world.items.Planter;
import com.sk89q.craftbook.gates.world.items.PlanterST;
import com.sk89q.craftbook.gates.world.items.Sorter;
import com.sk89q.craftbook.gates.world.items.SorterST;
import com.sk89q.craftbook.gates.world.miscellaneous.ArrowBarrage;
import com.sk89q.craftbook.gates.world.miscellaneous.ArrowShooter;
import com.sk89q.craftbook.gates.world.miscellaneous.FireBarrage;
import com.sk89q.craftbook.gates.world.miscellaneous.FireShooter;
import com.sk89q.craftbook.gates.world.miscellaneous.FlameThrower;
import com.sk89q.craftbook.gates.world.miscellaneous.LightningSummon;
import com.sk89q.craftbook.gates.world.miscellaneous.Melody;
import com.sk89q.craftbook.gates.world.miscellaneous.MessageSender;
import com.sk89q.craftbook.gates.world.miscellaneous.ParticleEffect;
import com.sk89q.craftbook.gates.world.miscellaneous.ParticleEffectST;
import com.sk89q.craftbook.gates.world.miscellaneous.PotionInducer;
import com.sk89q.craftbook.gates.world.miscellaneous.SoundEffect;
import com.sk89q.craftbook.gates.world.miscellaneous.TimedExplosion;
import com.sk89q.craftbook.gates.world.miscellaneous.WirelessReceiver;
import com.sk89q.craftbook.gates.world.miscellaneous.WirelessReceiverST;
import com.sk89q.craftbook.gates.world.miscellaneous.WirelessTransmitter;
import com.sk89q.craftbook.gates.world.sensors.DaySensor;
import com.sk89q.craftbook.gates.world.sensors.DaySensorST;
import com.sk89q.craftbook.gates.world.sensors.ItemNotSensor;
import com.sk89q.craftbook.gates.world.sensors.ItemNotSensorST;
import com.sk89q.craftbook.gates.world.sensors.ItemSensor;
import com.sk89q.craftbook.gates.world.sensors.ItemSensorST;
import com.sk89q.craftbook.gates.world.sensors.LightSensor;
import com.sk89q.craftbook.gates.world.sensors.LightSensorST;
import com.sk89q.craftbook.gates.world.sensors.PowerSensor;
import com.sk89q.craftbook.gates.world.sensors.PowerSensorST;
import com.sk89q.craftbook.gates.world.weather.RainSensor;
import com.sk89q.craftbook.gates.world.weather.RainSensorST;
import com.sk89q.craftbook.gates.world.weather.ServerTimeModulus;
import com.sk89q.craftbook.gates.world.weather.TStormSensor;
import com.sk89q.craftbook.gates.world.weather.TStormSensorST;
import com.sk89q.craftbook.gates.world.weather.TimeControl;
import com.sk89q.craftbook.gates.world.weather.TimeControlAdvanced;
import com.sk89q.craftbook.gates.world.weather.TimeFaker;
import com.sk89q.craftbook.gates.world.weather.TimeSet;
import com.sk89q.craftbook.gates.world.weather.TimeSetST;
import com.sk89q.craftbook.gates.world.weather.WeatherControl;
import com.sk89q.craftbook.gates.world.weather.WeatherControlAdvanced;
import com.sk89q.craftbook.gates.world.weather.WeatherFaker;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICFamily;
import com.sk89q.craftbook.ic.ICManager;
import com.sk89q.craftbook.ic.ICMechanicFactory;
import com.sk89q.craftbook.ic.RegisteredICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import com.sk89q.craftbook.ic.families.Family3I3O;
import com.sk89q.craftbook.ic.families.Family3ISO;
import com.sk89q.craftbook.ic.families.FamilyAISO;
import com.sk89q.craftbook.ic.families.FamilySI3O;
import com.sk89q.craftbook.ic.families.FamilySI5O;
import com.sk89q.craftbook.ic.families.FamilySISO;
import com.sk89q.craftbook.ic.families.FamilyVIVO;
import com.sk89q.craftbook.plc.PlcFactory;
import com.sk89q.craftbook.plc.lang.Perlstone;
import com.sk89q.wepif.PermissionsResolverManager;
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

        File midi = new File(getDataFolder(), "midi/");
        if (!midi.exists()) {
            midi.mkdir();
        }

        if (config.enableICs) {
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
        if (config.enableICs) {
            registerMechanic(new ICMechanicFactory(this, icManager));
            setupSelfTriggered();
        }

        // Register events
        registerEvents();

        languageManager = new LanguageManager(this);

        try {
            Metrics metrics = new Metrics(this);

            Graph graph = metrics.createGraph("Language");
            for (String lan : languageManager.getLanguages()) {
                graph.addPlotter(new Metrics.Plotter(lan) {

                    @Override
                    public int getValue() {

                        return 1;
                    }
                });
            }

            metrics.start();
        } catch (Exception ignored) {
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

        //SISOs
        registerIC("MC1000", "repeater",    new Repeater.Factory(server), familySISO, familyAISO);
        registerIC("MC1001", "inverter",    new Inverter.Factory(server), familySISO, familyAISO);
        registerIC("MC1017", "re t flip",   new ToggleFlipFlop.Factory(server, true), familySISO, familyAISO);
        registerIC("MC1018", "fe t flip",   new ToggleFlipFlop.Factory(server, false), familySISO, familyAISO);
        registerIC("MC1020", "random bit",  new RandomBit.Factory(server), familySISO, familyAISO);
        registerIC("MC1025", "server time", new ServerTimeModulus.Factory(server), familySISO, familyAISO);
        registerIC("MC1110", "transmitter", new WirelessTransmitter.Factory(server), familySISO, familyAISO);
        registerIC("MC1111", "receiver",    new WirelessReceiver.Factory(server), familySISO, familyAISO);
        registerIC("MC1200", "spawner",     new CreatureSpawner.Factory(server), familySISO, familyAISO);     // Restricted
        registerIC("MC1201", "dispenser",   new ItemDispenser.Factory(server), familySISO,familyAISO);       // Restricted
        registerIC("MC1202", "c dispense",  new ContainerDispenser.Factory(server), familySISO, familyAISO);      // Restricted
        registerIC("MC1203", "strike",      new LightningSummon.Factory(server), familySISO, familyAISO);     // Restricted
        registerIC("MC1204", "trap",        new EntityTrap.Factory(server), familySISO, familyAISO);          // Restricted
        registerIC("MC1205", "set above",   new SetBlockAbove.Factory(server), familySISO, familyAISO);       // Restricted
        registerIC("MC1206", "set below",   new SetBlockBelow.Factory(server), familySISO, familyAISO);       // Restricted
        registerIC("MC1207", "flex set",    new FlexibleSetBlock.Factory(server), familySISO, familyAISO);    // Restricted
        registerIC("MC1208", "mult set",    new MultipleSetBlock.Factory(server), familySISO, familyAISO);
        registerIC("MC1209", "collector",   new ContainerCollector.Factory(server), familySISO, familyAISO);
        registerIC("MC1210", "emitter",     new ParticleEffect.Factory(server), familySISO, familyAISO);      // Restricted
        registerIC("MC1211", "set bridge",  new SetBridge.Factory(server), familySISO, familyAISO);           // Restricted
        registerIC("MC1212", "set door",    new SetDoor.Factory(server), familySISO, familyAISO);             // Restricted
        registerIC("MC1213", "sound",       new SoundEffect.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1215", "set a chest", new SetBlockAboveChest.Factory(server), familySISO, familyAISO);  // Restricted
        registerIC("MC1216", "set b chest", new SetBlockBelowChest.Factory(server), familySISO, familyAISO);  // Restricted
        registerIC("MC1217", "pot induce",  new PotionInducer.Factory(server), familySISO, familyAISO);
        registerIC("MC1218", "block launch",new BlockLauncher.Factory(server), familySISO, familyAISO);
        registerIC("MC1219", "auto craft",  new AutomaticCrafter.Factory(server), familySISO, familyAISO);
        registerIC("MC1220", "a b break",   new BlockBreaker.Factory(server, false), familySISO, familyAISO);
        registerIC("MC1221", "b b break",   new BlockBreaker.Factory(server, true), familySISO, familyAISO);
        registerIC("MC1222", "liquid flood",new LiquidFlood.Factory(server), familySISO, familyAISO);    //Restricted
        registerIC("MC1223", "terraform",   new BonemealTerraformer.Factory(server), familySISO, familyAISO);
        registerIC("MC1224", "time bomb",   new TimedExplosion.Factory(server), familySISO, familyAISO); //Restricted
        registerIC("MC1225", "pump",        new Pump.Factory(server), familySISO, familyAISO);
        registerIC("MC1226", "spigot",      new Spigot.Factory(server), familySISO, familyAISO);
        registerIC("MC1227", "avd spawner", new AdvancedEntitySpawner.Factory(server), familySISO, familyAISO);     // Restricted
        registerIC("MC1228", "ent cannon",  new EntityCannon.Factory(server), familySISO, familyAISO);     // Restricted
        registerIC("MC1229", "sorter",      new Sorter.Factory(server), familySISO, familyAISO);
        registerIC("MC1230", "sense day",   new DaySensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1231", "t control",   new TimeControl.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1232", "time set",    new TimeSet.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1233", "item fan",    new ItemFan.Factory(server), familySISO, familyAISO);
        registerIC("MC1234", "planter",     new Planter.Factory(server), familySISO, familyAISO);
        registerIC("MC1235", "cultivator",  new Cultivator.Factory(server), familySISO, familyAISO);
        registerIC("MC1236", "fake weather",new WeatherFaker.Factory(server), familySISO, familyAISO);        // Restricted
        registerIC("MC1237", "fake time",   new TimeFaker.Factory(server), familySISO, familyAISO);           // Restricted
        registerIC("MC1240", "shoot arrow", new ArrowShooter.Factory(server), familySISO, familyAISO);        // Restricted
        registerIC("MC1241", "shoot arrows",new ArrowBarrage.Factory(server), familySISO, familyAISO);        // Restricted
        registerIC("MC1250", "shoot fire",  new FireShooter.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1251", "shoot fires", new FireBarrage.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1252", "flame thower",new FlameThrower.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1260", "sense water", new WaterSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1261", "sense lava",  new LavaSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1262", "sense light", new LightSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1263", "sense block", new BlockSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1264", "sense item",  new ItemSensor.Factory(server), familySISO, familyAISO);          // Restricted
        registerIC("MC1265", "inv sense item",new ItemNotSensor.Factory(server), familySISO, familyAISO);       // Restricted
        registerIC("MC1266", "sense power", new PowerSensor.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC1270", "melody",      new Melody.Factory(server), familySISO, familyAISO);
        registerIC("MC1271", "sense entity",new EntitySensor.Factory(server), familySISO, familyAISO);        // Restricted
        registerIC("MC1272", "sense player",new PlayerDetection.Factory(server), familySISO, familyAISO);     // Restricted
        registerIC("MC1420", "divide clock",new ClockDivider.Factory(server), familySISO, familyAISO);
        registerIC("MC1421", "clock",       new Clock.Factory(server), familySISO, familyAISO);
        registerIC("MC1510", "send message",new MessageSender.Factory(server), familySISO, familyAISO);
        registerIC("MC2100", "delayer",     new Delayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2101", "inv delayer", new NotDelayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2110", "fe delayer",  new LowDelayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2111", "inv fe delayer",new NotLowDelayer.Factory(server), familySISO, familyAISO);
        registerIC("MC2500", "pulser",      new Pulser.Factory(server), familySISO, familyAISO);
        registerIC("MC2501", "inv pulser",  new NotPulser.Factory(server), familySISO, familyAISO);
        registerIC("MC2510", "fe pulser",   new LowPulser.Factory(server), familySISO, familyAISO);
        registerIC("MC2511", "inv fe pulser",new LowNotPulser.Factory(server), familySISO, familyAISO);

        //SI3Os
        registerIC("MC2020", "random 3",    new Random3Bit.Factory(server), familySI3O);
        registerIC("MC2999", "marquee",     new Marquee.Factory(server), familySI3O);

        //3ISOs
        registerIC("MC3002", "and",         new AndGate.Factory(server), family3ISO);
        registerIC("MC3003", "nand",        new NandGate.Factory(server), family3ISO);
        registerIC("MC3020", "xor",         new XorGate.Factory(server), family3ISO);
        registerIC("MC3021", "xnor",        new XnorGate.Factory(server), family3ISO);
        registerIC("MC3030", "nor flip",    new RsNorFlipFlop.Factory(server), family3ISO);
        registerIC("MC3031", "inv nand latch",new InvertedRsNandLatch.Factory(server), family3ISO);
        registerIC("MC3032", "jk flip",     new JkFlipFlop.Factory(server), family3ISO);
        registerIC("MC3033", "nand latch",  new RsNandLatch.Factory(server), family3ISO);
        registerIC("MC3034", "edge df flip",new EdgeTriggerDFlipFlop.Factory(server), family3ISO);
        registerIC("MC3036", "level df flip",new LevelTriggeredDFlipFlop.Factory(server), family3ISO);
        registerIC("MC3040", "multiplexer", new Multiplexer.Factory(server), family3ISO);
        registerIC("MC3050", "combo",       new CombinationLock.Factory(server), family3ISO);
        registerIC("MC3101", "down counter",new DownCounter.Factory(server), family3ISO);
        registerIC("MC3102", "counter",     new Counter.Factory(server), family3ISO);
        registerIC("MC3231", "t control adva",new TimeControlAdvanced.Factory(server), family3ISO);             // Restricted
        registerIC("MC3300", "ROM set",     new MemorySetter.Factory(server), family3ISO);          // Restricted
        registerIC("MC3301", "ROM get",     new MemoryAccess.Factory(server), familySI3O);          // Restricted
        //3I3Os
        registerIC("MC4000", "full adder",  new FullAdder.Factory(server), family3I3O);
        registerIC("MC4010", "half adder",  new HalfAdder.Factory(server), family3I3O);
        registerIC("MC4100", "full subtr",  new FullSubtractor.Factory(server), family3I3O);
        registerIC("MC4110", "half subtr",  new HalfSubtractor.Factory(server), family3I3O);
        registerIC("MC4200", "dispatcher",  new Dispatcher.Factory(server), family3I3O);

        //SI5O's
        registerIC("MC6020", "random 5",    new Random5Bit.Factory(server), familySI5O);

        //PLCs
        registerIC("MC5000", "perlstone",   PlcFactory.fromLang(server, new Perlstone(), false), familyVIVO);
        registerIC("MC5001", "perlstone 3i3o",PlcFactory.fromLang(server, new Perlstone(), false), family3I3O);

        //Self triggered
        registerIC("MC0020", "random 1 st", new RandomBitST.Factory(server), familySISO, familyAISO);                     //Restricted
        registerIC("MC0111", "receiver st", new WirelessReceiverST.Factory(server), familySISO, familyAISO);
        registerIC("MC0204", "trap st",     new EntityTrapST.Factory(server), familySISO, familyAISO);                    // Restricted
        registerIC("MC0209", "collector st",new ContainerCollectorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0210", "emitter st",  new ParticleEffectST.Factory(server), familySISO, familyAISO);
        registerIC("MC0219", "auto craft st",new AutomaticCrafterST.Factory(server), familySISO, familyAISO);
        registerIC("MC0220", "a bl break st",new BlockBreakerST.Factory(server, false), familySISO, familyAISO);
        registerIC("MC0221", "b bl break st",new BlockBreakerST.Factory(server, true), familySISO, familyAISO);
        registerIC("MC0222", "liq flood st",new LiquidFloodST.Factory(server), familySISO, familyAISO);    //Restricted
        registerIC("MC0223", "terraform st",new BonemealTerraformerST.Factory(server), familySISO, familyAISO);
        registerIC("MC0225", "pump st",     new PumpST.Factory(server), familySISO, familyAISO);
        registerIC("MC0228", "ent cannon st",new EntityCannonST.Factory(server), familySISO, familyAISO);     // Restricted
        registerIC("MC0229", "sorter st",    new SorterST.Factory(server), familySISO, familyAISO);
        registerIC("MC0230", "sense day st",new DaySensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0232", "time set st", new TimeSetST.Factory(server), familySISO, familyAISO);         // Restricted
        registerIC("MC0233", "item fan st",  new ItemFanST.Factory(server), familySISO, familyAISO);
        registerIC("MC0234", "planter st",   new PlanterST.Factory(server), familySISO, familyAISO);
        registerIC("MC0235", "cultivator st",new CultivatorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0260", "sense water st",new WaterSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0261", "sense lava st",new LavaSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0262", "sense light st",new LightSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0263", "sense block st",new BlockSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0264", "sense item st",new ItemSensorST.Factory(server), familySISO, familyAISO);                    // Restricted
        registerIC("MC0265", "sense n item s",new ItemNotSensorST.Factory(server), familySISO, familyAISO);                 // Restricted
        registerIC("MC0266", "sense power st",new PowerSensorST.Factory(server), familySISO, familyAISO);                   // Restricted
        registerIC("MC0270", "sense power st",new PowerSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MC0271", "sense entit st",new EntitySensorST.Factory(server), familySISO, familyAISO);                  // Restricted
        registerIC("MC0272", "sense playe st",new PlayerDetectionST.Factory(server), familySISO, familyAISO);               // Restricted
        registerIC("MC0420", "clock st",    new ClockST.Factory(server), familySISO, familyAISO);
        registerIC("MC0421", "monostable",  new Monostable.Factory(server), familySISO, familyAISO);
        registerIC("MC0500", "range output",new RangedOutput.Factory(server), familySISO, familyAISO);
        //Xtra ICs
        //SISOs
        registerIC("MCX230", "rain sense",  new RainSensor.Factory(server), familySISO, familyAISO);
        registerIC("MCX231", "storm sense", new TStormSensor.Factory(server), familySISO, familyAISO);
        registerIC("MCX233", "weather set", new WeatherControl.Factory(server), familySISO, familyAISO);
        //3ISOs
        registerIC("MCT233", "weather set ad",new WeatherControlAdvanced.Factory(server), family3ISO);
        //Self triggered
        registerIC("MCZ230", "rain sense st",new RainSensorST.Factory(server), familySISO, familyAISO);
        registerIC("MCZ231", "storm sense st",new TStormSensorST.Factory(server), familySISO, familyAISO);
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

        logger.info("CraftBook Circuits: " + numChunks + " chunk(s) for " + numWorlds + " world(s) processed "
                + "(" + Math.round(time / 1000.0 * 10) / 10 + "s elapsed)");

        // Set up the clock for self-triggered ICs.
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new MechanicClock(manager), 0, 2);
    }

    @Override
    protected void registerEvents() {

        //if (getLocalConfiguration().enableICs) {
        //TODO reimplement torches    getServer().getPluginManager().registerEvents(new ICUtil.ICListener(), this);
        //}
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
     * Unregister a mechanic if possible
     * TODO Ensure no remnants are left behind
     *
     * @param factory
     *
     * @return true if the mechanic was successfully unregistered.
     */
    protected boolean unregisterMechanic(MechanicFactory<? extends Mechanic> factory) {

        return manager.unregister(factory);
    }

    public void generateICDocs(Player player, String id) {
        RegisteredICFactory ric = icManager.registered.get(id.toLowerCase());
        /*TODO continue work on all docs for(Map.Entry<String, RegisteredICFactory> rc : icManager.registered.entrySet()) {
            if(rc.getValue().getFactory().getDescription().equalsIgnoreCase("No Description.")) {
                Bukkit.getLogger().severe("IC " + rc.getValue().getId() + " MISSING DOCS!");
            }
        }*/
        if (ric == null) {
            try {
                ric = icManager.registered.get(getSearchID(player, id));
                if (ric == null) {
                    player.sendMessage(ChatColor.RED + "Invalid IC!");
                    return;
                }
            }
            catch(Exception e) {
                player.sendMessage(ChatColor.RED + "Invalid IC!");
                return;
            }
        }
        try {
            IC ic = ric.getFactory().create(null);
            player.sendMessage("    "); //To space the area
            player.sendMessage(ChatColor.BLUE + ic.getTitle() + " (" + ric.getId() + ") Documentation");
            if (getLocalConfiguration().enableShorthandIcs && ric.getShorthand() != null) {
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
                if(search != null && !tic.getTitle().toLowerCase().contains(search.toLowerCase()) &&
                        !ric.getId().toLowerCase().contains(search.toLowerCase()))
                    continue;

                return ic;
            }
            catch(Exception e){
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
                thisIC: {
                RegisteredICFactory ric = icManager.registered.get(ic);
                IC tic = ric.getFactory().create(null);
                if(search != null && !tic.getTitle().toLowerCase().contains(search.toLowerCase()) &&
                        !ric.getId().toLowerCase().contains(search.toLowerCase()))
                    continue;
                if(parameters != null) {
                    for(char c : parameters) {
                        if(c == 'r' && !(ric.getFactory() instanceof RestrictedIC))
                            break thisIC;
                        else if(c == 's' && ric.getFactory() instanceof RestrictedIC)
                            break thisIC;
                        else if(c == 'b' && !ric.getFactory().getClass().getPackage().getName().endsWith("blocks"))
                            break thisIC;
                        else if(c == 'i' && !ric.getFactory().getClass().getPackage().getName().endsWith("items"))
                            break thisIC;
                        else if(c == 'e' && !ric.getFactory().getClass().getPackage().getName().endsWith("entity"))
                            break thisIC;
                        else if(c == 'w' && !ric.getFactory().getClass().getPackage().getName().endsWith("weather"))
                            break thisIC;
                        else if(c == 'l' && !ric.getFactory().getClass().getPackage().getName().endsWith("logic"))
                            break thisIC;
                        else if(c == 'm' && !ric.getFactory().getClass().getPackage().getName().endsWith("miscellaneous"))
                            break thisIC;
                        else if(c == 'c' && !ric.getFactory().getClass().getPackage().getName().endsWith("sensors"))
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
                        SelfTriggeredIC ? "ST " : "T ") + (ric.getFactory() instanceof RestrictedIC ? ChatColor
                                .DARK_RED + "R " : ""));
            }
            } catch (Exception e) {
                if (ic.endsWith("5001") || ic.endsWith("5000")) {
                    // TODO
                } else {
                    Bukkit.getLogger().severe("An error occured generating the docs for IC: " + ic + ". Please report" +
                            " it to Me4502");
                }
            }
        }

        return strings.toArray(new String[strings.size()]);
    }

    @Override
    public void reloadConfiguration() {
        config = new CircuitsConfiguration(getConfig(), getDataFolder());
        saveConfig();
    }
}
