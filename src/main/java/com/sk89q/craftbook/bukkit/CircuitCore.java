package com.sk89q.craftbook.bukkit;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.LocalComponent;
import com.sk89q.craftbook.bukkit.commands.CircuitCommands;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.GlowStone;
import com.sk89q.craftbook.circuits.JackOLantern;
import com.sk89q.craftbook.circuits.Netherrack;
import com.sk89q.craftbook.circuits.Pipes;
import com.sk89q.craftbook.circuits.gates.logic.AndGate;
import com.sk89q.craftbook.circuits.gates.logic.Clock;
import com.sk89q.craftbook.circuits.gates.logic.ClockDivider;
import com.sk89q.craftbook.circuits.gates.logic.CombinationLock;
import com.sk89q.craftbook.circuits.gates.logic.Counter;
import com.sk89q.craftbook.circuits.gates.logic.DeMultiplexer;
import com.sk89q.craftbook.circuits.gates.logic.Delayer;
import com.sk89q.craftbook.circuits.gates.logic.Dispatcher;
import com.sk89q.craftbook.circuits.gates.logic.DownCounter;
import com.sk89q.craftbook.circuits.gates.logic.EdgeTriggerDFlipFlop;
import com.sk89q.craftbook.circuits.gates.logic.FullAdder;
import com.sk89q.craftbook.circuits.gates.logic.FullSubtractor;
import com.sk89q.craftbook.circuits.gates.logic.HalfAdder;
import com.sk89q.craftbook.circuits.gates.logic.HalfSubtractor;
import com.sk89q.craftbook.circuits.gates.logic.InvertedRsNandLatch;
import com.sk89q.craftbook.circuits.gates.logic.Inverter;
import com.sk89q.craftbook.circuits.gates.logic.JkFlipFlop;
import com.sk89q.craftbook.circuits.gates.logic.LevelTriggeredDFlipFlop;
import com.sk89q.craftbook.circuits.gates.logic.LowDelayer;
import com.sk89q.craftbook.circuits.gates.logic.LowNotPulser;
import com.sk89q.craftbook.circuits.gates.logic.LowPulser;
import com.sk89q.craftbook.circuits.gates.logic.Marquee;
import com.sk89q.craftbook.circuits.gates.logic.MemoryAccess;
import com.sk89q.craftbook.circuits.gates.logic.MemorySetter;
import com.sk89q.craftbook.circuits.gates.logic.Monostable;
import com.sk89q.craftbook.circuits.gates.logic.Multiplexer;
import com.sk89q.craftbook.circuits.gates.logic.NandGate;
import com.sk89q.craftbook.circuits.gates.logic.NotDelayer;
import com.sk89q.craftbook.circuits.gates.logic.NotLowDelayer;
import com.sk89q.craftbook.circuits.gates.logic.NotPulser;
import com.sk89q.craftbook.circuits.gates.logic.Pulser;
import com.sk89q.craftbook.circuits.gates.logic.Random3Bit;
import com.sk89q.craftbook.circuits.gates.logic.Random5Bit;
import com.sk89q.craftbook.circuits.gates.logic.RandomBit;
import com.sk89q.craftbook.circuits.gates.logic.RangedOutput;
import com.sk89q.craftbook.circuits.gates.logic.Repeater;
import com.sk89q.craftbook.circuits.gates.logic.RsNandLatch;
import com.sk89q.craftbook.circuits.gates.logic.RsNorFlipFlop;
import com.sk89q.craftbook.circuits.gates.logic.ToggleFlipFlop;
import com.sk89q.craftbook.circuits.gates.logic.XnorGate;
import com.sk89q.craftbook.circuits.gates.logic.XorGate;
import com.sk89q.craftbook.circuits.gates.world.blocks.BlockBreaker;
import com.sk89q.craftbook.circuits.gates.world.blocks.BlockLauncher;
import com.sk89q.craftbook.circuits.gates.world.blocks.BlockReplacer;
import com.sk89q.craftbook.circuits.gates.world.blocks.BonemealTerraformer;
import com.sk89q.craftbook.circuits.gates.world.blocks.CombineHarvester;
import com.sk89q.craftbook.circuits.gates.world.blocks.Cultivator;
import com.sk89q.craftbook.circuits.gates.world.blocks.Driller;
import com.sk89q.craftbook.circuits.gates.world.blocks.FlexibleSetBlock;
import com.sk89q.craftbook.circuits.gates.world.blocks.Irrigator;
import com.sk89q.craftbook.circuits.gates.world.blocks.LiquidFlood;
import com.sk89q.craftbook.circuits.gates.world.blocks.MultipleSetBlock;
import com.sk89q.craftbook.circuits.gates.world.blocks.Planter;
import com.sk89q.craftbook.circuits.gates.world.blocks.Pump;
import com.sk89q.craftbook.circuits.gates.world.blocks.SetBlockAbove;
import com.sk89q.craftbook.circuits.gates.world.blocks.SetBlockAboveChest;
import com.sk89q.craftbook.circuits.gates.world.blocks.SetBlockBelow;
import com.sk89q.craftbook.circuits.gates.world.blocks.SetBlockBelowChest;
import com.sk89q.craftbook.circuits.gates.world.blocks.SetBridge;
import com.sk89q.craftbook.circuits.gates.world.blocks.SetDoor;
import com.sk89q.craftbook.circuits.gates.world.blocks.Spigot;
import com.sk89q.craftbook.circuits.gates.world.entity.AdvancedEntitySpawner;
import com.sk89q.craftbook.circuits.gates.world.entity.AnimalBreeder;
import com.sk89q.craftbook.circuits.gates.world.entity.AnimalHarvester;
import com.sk89q.craftbook.circuits.gates.world.entity.CreatureSpawner;
import com.sk89q.craftbook.circuits.gates.world.entity.EntityCannon;
import com.sk89q.craftbook.circuits.gates.world.entity.EntityTrap;
import com.sk89q.craftbook.circuits.gates.world.entity.TeleportReciever;
import com.sk89q.craftbook.circuits.gates.world.entity.TeleportTransmitter;
import com.sk89q.craftbook.circuits.gates.world.items.AutomaticCrafter;
import com.sk89q.craftbook.circuits.gates.world.items.ChestStocker;
import com.sk89q.craftbook.circuits.gates.world.items.ContainerCollector;
import com.sk89q.craftbook.circuits.gates.world.items.ContainerDispenser;
import com.sk89q.craftbook.circuits.gates.world.items.ContainerStacker;
import com.sk89q.craftbook.circuits.gates.world.items.Distributer;
import com.sk89q.craftbook.circuits.gates.world.items.ItemDispenser;
import com.sk89q.craftbook.circuits.gates.world.items.ItemFan;
import com.sk89q.craftbook.circuits.gates.world.items.RangedCollector;
import com.sk89q.craftbook.circuits.gates.world.items.Sorter;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.ArrowBarrage;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.ArrowShooter;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.FireBarrage;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.FireShooter;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.FlameThrower;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.Jukebox;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.LightningSummon;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.Melody;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.MessageSender;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.ParticleEffect;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.PotionInducer;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.ProgrammableFireworkShow;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.RadioPlayer;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.RadioStation;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.SentryGun;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.SoundEffect;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.TimedExplosion;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.Tune;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.WirelessReceiver;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.WirelessTransmitter;
import com.sk89q.craftbook.circuits.gates.world.miscellaneous.XPSpawner;
import com.sk89q.craftbook.circuits.gates.world.sensors.BlockSensor;
import com.sk89q.craftbook.circuits.gates.world.sensors.ContentsSensor;
import com.sk89q.craftbook.circuits.gates.world.sensors.DaySensor;
import com.sk89q.craftbook.circuits.gates.world.sensors.EntitySensor;
import com.sk89q.craftbook.circuits.gates.world.sensors.ItemNotSensor;
import com.sk89q.craftbook.circuits.gates.world.sensors.ItemSensor;
import com.sk89q.craftbook.circuits.gates.world.sensors.LavaSensor;
import com.sk89q.craftbook.circuits.gates.world.sensors.LightSensor;
import com.sk89q.craftbook.circuits.gates.world.sensors.PlayerSensor;
import com.sk89q.craftbook.circuits.gates.world.sensors.PowerSensor;
import com.sk89q.craftbook.circuits.gates.world.sensors.WaterSensor;
import com.sk89q.craftbook.circuits.gates.world.weather.RainSensor;
import com.sk89q.craftbook.circuits.gates.world.weather.ServerTimeModulus;
import com.sk89q.craftbook.circuits.gates.world.weather.TStormSensor;
import com.sk89q.craftbook.circuits.gates.world.weather.TimeControl;
import com.sk89q.craftbook.circuits.gates.world.weather.TimeControlAdvanced;
import com.sk89q.craftbook.circuits.gates.world.weather.TimeFaker;
import com.sk89q.craftbook.circuits.gates.world.weather.TimeSet;
import com.sk89q.craftbook.circuits.gates.world.weather.WeatherControl;
import com.sk89q.craftbook.circuits.gates.world.weather.WeatherControlAdvanced;
import com.sk89q.craftbook.circuits.gates.world.weather.WeatherFaker;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICFamily;
import com.sk89q.craftbook.circuits.ic.ICManager;
import com.sk89q.craftbook.circuits.ic.ICMechanicFactory;
import com.sk89q.craftbook.circuits.ic.RegisteredICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.families.Family3I3O;
import com.sk89q.craftbook.circuits.ic.families.Family3ISO;
import com.sk89q.craftbook.circuits.ic.families.FamilyAISO;
import com.sk89q.craftbook.circuits.ic.families.FamilySI3O;
import com.sk89q.craftbook.circuits.ic.families.FamilySI5O;
import com.sk89q.craftbook.circuits.ic.families.FamilySISO;
import com.sk89q.craftbook.circuits.ic.families.FamilyVIVO;
import com.sk89q.craftbook.circuits.plc.PlcFactory;
import com.sk89q.craftbook.circuits.plc.lang.Perlstone;
import com.sk89q.craftbook.util.config.YAMLICConfiguration;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * Author: Turtle9598
 */
public class CircuitCore implements LocalComponent {

    private static CircuitCore instance;
    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    private ICManager icManager;

    private YAMLICConfiguration icConfiguration;

    private ICMechanicFactory ICFactory;
    private Pipes.Factory pipeFactory;

    private File romFolder;
    private File midiFolder;
    private File fireworkFolder;

    public static final ICFamily FAMILY_SISO = new FamilySISO();
    public static final ICFamily FAMILY_3ISO = new Family3ISO();
    public static final ICFamily FAMILY_SI3O = new FamilySI3O();
    public static final ICFamily FAMILY_AISO = new FamilyAISO();
    public static final ICFamily FAMILY_3I3O = new Family3I3O();
    public static final ICFamily FAMILY_VIVO = new FamilyVIVO();
    public static final ICFamily FAMILY_SI5O = new FamilySI5O();

    public static boolean isEnabled() {

        return instance != null;
    }

    public CircuitCore() {

        instance = this;
    }

    public static CircuitCore inst() {

        return instance;
    }

    @Override
    public void enable() {

        plugin.registerCommands(CircuitCommands.class);

        plugin.createDefaultConfiguration(new File(plugin.getDataFolder(), "ic-config.yml"), "ic-config.yml", false);
        icConfiguration = new YAMLICConfiguration(new YAMLProcessor(new File(plugin.getDataFolder(), "ic-config.yml"), true, YAMLFormat.EXTENDED), plugin.getLogger());

        midiFolder = new File(plugin.getDataFolder(), "midi/");
        new File(getMidiFolder(), "playlists").mkdirs();

        romFolder = new File(plugin.getDataFolder(), "rom/");

        fireworkFolder = new File(plugin.getDataFolder(), "fireworks/");
        getFireworkFolder();

        registerMechanics();

        try {
            icConfiguration.load();
        } catch (Throwable e) {
            BukkitUtil.printStacktrace(e);
        }
    }

    @Override
    public void disable() {

        for(RegisteredICFactory factory : icManager.registered.values()) {
            factory.getFactory().unload();
        }
        ICManager.emptyCache();
        icManager.registered.clear();
        instance = null;
    }

    public File getFireworkFolder() {

        if (!fireworkFolder.exists()) fireworkFolder.mkdir();
        return fireworkFolder;
    }

    public File getRomFolder() {

        if (!romFolder.exists()) romFolder.mkdir();
        return romFolder;
    }

    public File getMidiFolder() {

        if (!midiFolder.exists()) midiFolder.mkdir();
        return midiFolder;
    }

    public ICMechanicFactory getICFactory() {

        return ICFactory;
    }

    public Pipes.Factory getPipeFactory() {

        return pipeFactory;
    }

    private void registerMechanics() {

        BukkitConfiguration config = CraftBookPlugin.inst().getConfiguration();

        if (config.ICEnabled) {
            registerICs();
            plugin.registerMechanic(ICFactory = new ICMechanicFactory(getIcManager()));
        }

        // Let's register mechanics!
        if (config.netherrackEnabled) plugin.registerMechanic(new Netherrack.Factory());
        if (config.pumpkinsEnabled) plugin.registerMechanic(new JackOLantern.Factory());
        if (config.glowstoneEnabled) plugin.registerMechanic(new GlowStone.Factory());
        if (config.pipesEnabled) plugin.registerMechanic(pipeFactory = new Pipes.Factory());
    }

    private void registerICs() {

        Server server = plugin.getServer();

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
        registerIC("MC1202", "c dispense", new ContainerDispenser.Factory(server), familySISO, familyAISO); // Restricted
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
        registerIC("MC1214", "range coll", new RangedCollector.Factory(server), familySISO, familyAISO);
        registerIC("MC1215", "set a chest", new SetBlockAboveChest.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1216", "set b chest", new SetBlockBelowChest.Factory(server), familySISO, familyAISO); // Restricted
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
        registerIC("MC1227", "avd spawner", new AdvancedEntitySpawner.Factory(server), familySISO, familyAISO); // Restricted
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
        registerIC("MC1243", "distributer", new Distributer.Factory(server), familySISO, familyAISO);
        registerIC("MC1244", "animal harvest", new AnimalHarvester.Factory(server), familySISO, familyAISO);
        registerIC("MC1245", "cont stacker", new ContainerStacker.Factory(server), familySISO, familyAISO);
        registerIC("MC1246", "xp spawner", new XPSpawner.Factory(server), familySISO, familyAISO); //Restricted
        //TODO Dyed Armour Spawner (MC1247) (Sign Title: DYE ARMOUR)
        registerIC("MC1248", "driller", new Driller.Factory(server), familySISO, familyAISO); //Restricted
        registerIC("MC1249", "replacer", new BlockReplacer.Factory(server), familySISO, familyAISO); //Restricted
        registerIC("MC1250", "shoot fire", new FireShooter.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1251", "shoot fires", new FireBarrage.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1252", "flame thower", new FlameThrower.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1253", "firework show", new ProgrammableFireworkShow.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1260", "sense water", new WaterSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1261", "sense lava", new LavaSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1262", "sense light", new LightSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1263", "sense block", new BlockSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1264", "sense item", new ItemSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1265", "inv sense item", new ItemNotSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1266", "sense power", new PowerSensor.Factory(server), familySISO, familyAISO);
        //FIXME registerIC("MC1267", "sense move", new MovementSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1268", "sense contents", new ContentsSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1270", "melody", new Melody.Factory(server), familySISO, familyAISO);
        registerIC("MC1271", "sense entity", new EntitySensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1272", "sense player", new PlayerSensor.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1273", "jukebox", new Jukebox.Factory(server), familySISO, familyAISO);
        registerIC("MC1275", "tune", new Tune.Factory(server), familySISO, familyAISO);
        registerIC("MC1276", "radio station", new RadioStation.Factory(server), familySISO, familyAISO);
        registerIC("MC1277", "radio player", new RadioPlayer.Factory(server), familySISO, familyAISO);
        registerIC("MC1278", "sentry gun", new SentryGun.Factory(server), familySISO, familyAISO); //Restricted
        registerIC("MC1280", "animal breed", new AnimalBreeder.Factory(server), familySISO, familyAISO);
        registerIC("MC1420", "divide clock", new ClockDivider.Factory(server), familySISO, familyAISO);
        registerIC("MC1421", "clock", new Clock.Factory(server), familySISO, familyAISO);
        registerIC("MC1422", "monostable", new Monostable.Factory(server), familySISO, familyAISO);
        registerIC("MC1500", "range output", new RangedOutput.Factory(server), familySISO, familyAISO);
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
        registerIC("MC4040", "demultiplexer", new DeMultiplexer.Factory(server), family3I3O);
        registerIC("MC4100", "full subtr", new FullSubtractor.Factory(server), family3I3O);
        registerIC("MC4110", "half subtr", new HalfSubtractor.Factory(server), family3I3O);
        registerIC("MC4200", "dispatcher", new Dispatcher.Factory(server), family3I3O);

        // SI5O's
        registerIC("MC6020", "random 5", new Random5Bit.Factory(server), familySI5O);

        // PLCs
        registerIC("MC5000", "perlstone", PlcFactory.fromLang(server, new Perlstone(), false), familyVIVO);
        registerIC("MC5001", "perlstone 3i3o", PlcFactory.fromLang(server, new Perlstone(), false), family3I3O);

        // Xtra ICs
        // SISOs
        registerIC("MCX230", "rain sense", new RainSensor.Factory(server), familySISO, familyAISO);
        registerIC("MCX231", "storm sense", new TStormSensor.Factory(server), familySISO, familyAISO);
        registerIC("MCX233", "weather set", new WeatherControl.Factory(server), familySISO, familyAISO);
        // 3ISOs
        registerIC("MCT233", "weather set ad", new WeatherControlAdvanced.Factory(server), family3ISO);
    }

    /**
     * Register an ic if possible
     *
     * @param name
     * @param factory
     * @param families
     */
    public boolean registerIC(String name, String longName, ICFactory factory, ICFamily... families) {

        for(String ic : CraftBookPlugin.inst().getConfiguration().ICsDisabled)
            if(ic.equalsIgnoreCase(name))
                return false;
        return getIcManager().register(name, longName, factory, families);
    }

    public List<RegisteredICFactory> getICList() {

        if(getIcManager() == null)
            return new ArrayList<RegisteredICFactory>();
        List<RegisteredICFactory> ics = new ArrayList<RegisteredICFactory>();
        for (Map.Entry<String, RegisteredICFactory> e : getIcManager().registered.entrySet()) {
            ics.add(e.getValue());
        }
        return ics;
    }

    public String getSearchID(Player p, String search) {

        ArrayList<String> icNameList = new ArrayList<String>();
        icNameList.addAll(getIcManager().registered.keySet());

        Collections.sort(icNameList);

        for (String ic : icNameList) {
            try {
                RegisteredICFactory ric = getIcManager().registered.get(ic);
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
     * Used for the /ic list command.
     *
     * @param p
     *
     * @return
     */
    public String[] generateICText(Player p, String search, char[] parameters) {

        ArrayList<String> icNameList = new ArrayList<String>();
        icNameList.addAll(getIcManager().registered.keySet());

        Collections.sort(icNameList);

        ArrayList<String> strings = new ArrayList<String>();
        boolean col = true;
        for (String ic : icNameList) {
            try {
                thisIC:
                {
                RegisteredICFactory ric = getIcManager().registered.get(ic);
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

                if (!ICMechanicFactory.checkPermissionsBoolean(CraftBookPlugin.inst().wrapPlayer(p), ric.getFactory(), ic.toLowerCase())) {
                    colour = col ? ChatColor.RED : ChatColor.DARK_RED;
                }
                strings.add(colour + tic.getTitle() + " (" + ric.getId() + ")"
                        + ": " + (tic instanceof SelfTriggeredIC ? "ST " : "T ")
                        + (ric.getFactory() instanceof RestrictedIC ? ChatColor.DARK_RED + "R " : ""));
                }
            } catch (Throwable e) {
                plugin.getLogger().warning("An error occurred generating the docs for IC: " + ic + ".");
                plugin.getLogger().warning("Please report this error on: http://youtrack.sk89q.com/.");
            }
        }

        return strings.toArray(new String[strings.size()]);
    }

    public ICManager getIcManager () {
        return icManager;
    }
}