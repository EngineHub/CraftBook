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

package com.sk89q.craftbook.mechanics.ic;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.families.*;
import com.sk89q.craftbook.mechanics.ic.gates.logic.*;
import com.sk89q.craftbook.mechanics.ic.gates.variables.IsAtLeast;
import com.sk89q.craftbook.mechanics.ic.gates.variables.ItemCounter;
import com.sk89q.craftbook.mechanics.ic.gates.variables.NumericModifier;
import com.sk89q.craftbook.mechanics.ic.gates.world.blocks.*;
import com.sk89q.craftbook.mechanics.ic.gates.world.entity.*;
import com.sk89q.craftbook.mechanics.ic.gates.world.items.*;
import com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous.*;
import com.sk89q.craftbook.mechanics.ic.gates.world.sensors.*;
import com.sk89q.craftbook.mechanics.ic.gates.world.weather.*;
import com.sk89q.craftbook.mechanics.ic.plc.PlcFactory;
import com.sk89q.craftbook.mechanics.ic.plc.lang.Perlstone;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Manages known registered ICs. For an IC to be detected in-world through CraftBook,
 * the IC's factory has to be registered with this manager.
 *
 * @author sk89q
 */
public class ICManager {

    public static final ICFamily familySISO = new FamilySISO();
    public static final ICFamily family3ISO = new Family3ISO();
    public static final ICFamily familySI3O = new FamilySI3O();
    public static final ICFamily familyAISO = new FamilyAISO();
    public static final ICFamily family3I3O = new Family3I3O();
    public static final ICFamily familyVIVO = new FamilyVIVO();
    public static final ICFamily familySI5O = new FamilySI5O();

    private ICConfiguration icConfiguration;

    private File romFolder;
    private File midiFolder;
    private File fireworkFolder;

    private static ICManager INSTANCE;

    public ICManager() {
        INSTANCE = this;
    }

    public static ICManager inst() {
        return INSTANCE;
    }

    public void enable() {
        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "ic-config.yml"), "ic-config.yml");
        icConfiguration = new ICConfiguration(new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "ic-config.yml"), true, YAMLFormat.EXTENDED), CraftBookPlugin.logger());

        midiFolder = new File(CraftBookPlugin.inst().getDataFolder(), "midi/");
        new File(midiFolder, "playlists").mkdirs();

        romFolder = new File(CraftBookPlugin.inst().getDataFolder(), "rom/");

        fireworkFolder = new File(CraftBookPlugin.inst().getDataFolder(), "fireworks/");

        registerICs(CraftBookPlugin.inst().getServer());

        try {
            icConfiguration.load();
        } catch (Throwable e) {
            CraftBookBukkitUtil.printStacktrace(e);
        }
    }

    public void disable() {

        for(RegisteredICFactory factory : registered.values()) {
            factory.getFactory().unload();
        }
        icConfiguration = null;
        emptyCache();
        INSTANCE = null;
    }

    public File getFireworkFolder() {

        return fireworkFolder;
    }

    public File getMidiFolder() {

        return midiFolder;
    }

    public File getRomFolder() {

        return romFolder;
    }

    /**
     * Holds a map of registered IC factories with their ID.
     *
     * @see RegisteredICFactory
     */
    public final Map<String, RegisteredICFactory> registered = new LinkedHashMap<>();

    /**
     * Holds a map of long IDs to short IDs
     *
     * @see RegisteredICFactory
     */
    public final Map<String, String> longRegistered = new HashMap<>();

    private static final Map<Location, IC> cachedICs = new HashMap<>();

    private static final Set<String> customPrefix = new HashSet<>();

    /**
     * Register an ic if possible
     *
     * @param name
     * @param factory
     * @param families
     */
    public boolean registerIC(String name, String longName, ICFactory factory, ICFamily... families) {

        for(String ic : ICMechanic.instance.disabledICs)
            if(ic.equalsIgnoreCase(name))
                return false;
        return register(name, longName, factory, families);
    }

    /**
     * Register an IC with the manager. The casing of the ID can be of any case because IC IDs are case-insensitive.
     * Re-using an already registered
     * name will override the previous registration.
     *
     * @param id       case-insensitive ID (such as MC1001)
     * @param factory  factory to create ICs
     * @param families families for the ic
     */
    public void register(String id, ICFactory factory, ICFamily... families) {

        register(id, null, factory, families);
    }

    /**
     * Register an IC with the manager. The casing of the ID can be of any case because IC IDs are case-insensitive.
     * Re-using an already registered
     * name will override the previous registration.
     *
     * @param id       case-insensitive ID (such as MC1001)
     * @param longId   case-insensitive long name (such as inverter)
     * @param factory  factory to create ICs
     * @param families families for the ic
     *
     * @return true if IC registration was a success
     */
    public boolean register(String id, String longId, ICFactory factory, ICFamily... families) {

        // check if at least one family is given
        if (families.length < 1) return false;
        // this is needed so we dont have two patterns
        String id2 = "[" + id + "]";
        // lets check if the IC ID has already been registered
        if (registered.containsKey(id.toLowerCase(Locale.ENGLISH))) return false;
        // check if the ic matches the requirements
        Matcher matcher = RegexUtil.IC_PATTERN.matcher(id2);
        if (!matcher.matches()) return false;
        String prefix = matcher.group(2).toLowerCase(Locale.ENGLISH);
        // lets get the custom prefix
        customPrefix.add(prefix);

        RegisteredICFactory registration = new RegisteredICFactory(id, longId, factory, families);
        // Lowercase the ID so that we can do case in-sensitive lookups
        registered.put(id.toLowerCase(Locale.ENGLISH), registration);

        if (longId != null) {
            String toRegister = longId.toLowerCase(Locale.ENGLISH);
            if (toRegister.length() > 15) {
                toRegister = toRegister.substring(0, 15);
            }
            longRegistered.put(toRegister, id);
        }

        factory.load();

        return true;
    }

    /**
     * Get an IC registration by a provided ID.
     *
     * @param id case insensitive ID
     *
     * @return registration
     *
     * @see RegisteredICFactory
     */
    public RegisteredICFactory get(String id) {

        return registered.get(id.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Checks if the IC Mechanic at the given point is cached. If not it will return false.
     *
     * @param pt of the ic
     *
     * @return true if ic is cached
     */
    public static boolean isCachedIC(Location pt) {

        return cachedICs.containsKey(pt);
    }

    /**
     * Gets the cached IC based on its location in the world. isCached should be checked before calling this method.
     *
     * @param pt of the ic
     *
     * @return cached ic.
     */
    public static IC getCachedIC(Location pt) {

        return cachedICs.get(pt);
    }

    /**
     * Adds the given IC to the cached IC list.
     *
     * @param pt of the ic
     * @param ic to add
     */
    public static void addCachedIC(Location pt, IC ic) {

        if (!ICMechanic.instance.cache) return;
        if(cachedICs.containsKey(pt)) return;
        CraftBookPlugin.logDebugMessage("Caching IC at: " + pt.toString(), "ic-cache");
        cachedICs.put(pt, ic);
    }

    /**
     * Removes the given IC from the cache list based on its location.
     *
     * @param pt of the ic
     *
     * @return the removed ic
     */
    public static IC removeCachedIC(Location pt) {

        if (cachedICs.containsKey(pt)) {
            CraftBookPlugin.logDebugMessage("Removing cached IC at: " + pt.toString(), "ic-cache");
            return cachedICs.remove(pt);
        }
        return null;
    }

    /**
     * Gets called when the IC gets unloaded. This method then takes care of clearing the IC from the cache.
     *
     * @param pt of the block break
     */
    public static void unloadIC(Location pt) {

        removeCachedIC(pt);
    }

    /**
     * Clears the IC cache.
     *
     */
    public static void emptyCache() {

        CraftBookPlugin.logDebugMessage("Culling cached IC list.", "ic-cache");
        cachedICs.clear();
    }

    /**
     * Gets the IC Cache map.
     */
    public static Map<Location, IC> getCachedICs() {

        return cachedICs;
    }

    public static boolean hasCustomPrefix(String prefix) {

        return customPrefix.contains(prefix.toLowerCase(Locale.ENGLISH));
    }

    public List<RegisteredICFactory> getICList() {

        return new LinkedList<>(registered.values());
    }

    public void registerICs(Server server) {

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
        registerIC("MC1205", "set above", new SetBlockAdmin.Factory(server, true), familySISO, familyAISO); // Restricted
        registerIC("MC1206", "set below", new SetBlockAdmin.Factory(server, false), familySISO, familyAISO); // Restricted
        registerIC("MC1207", "flex set", new FlexibleSetBlock.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1208", "mult set", new MultipleSetBlock.Factory(server), familySISO, familyAISO);
        registerIC("MC1209", "collector", new ContainerCollector.Factory(server), familySISO, familyAISO);
        registerIC("MC1210", "emitter", new ParticleEffect.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1211", "set bridge", new SetBridge.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1212", "set door", new SetDoor.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1213", "sound", new SoundEffect.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1214", "range coll", new RangedCollector.Factory(server), familySISO, familyAISO);
        registerIC("MC1215", "set a chest", new SetBlockChest.Factory(server, true), familySISO, familyAISO);
        registerIC("MC1216", "set b chest", new SetBlockChest.Factory(server, false), familySISO, familyAISO);
        registerIC("MC1217", "pot induce", new PotionInducer.Factory(server), familySISO, familyAISO);
        registerIC("MC1218", "block launch", new BlockLauncher.Factory(server), familySISO, familyAISO);
        registerIC("MC1219", "auto craft", new AutomaticCrafter.Factory(server), familySISO, familyAISO);
        registerIC("MC1220", "a b break", new BlockBreaker.Factory(server, false), familySISO, familyAISO);
        registerIC("MC1221", "b b break", new BlockBreaker.Factory(server, true), familySISO, familyAISO);
        registerIC("MC1222", "liq flood", new LiquidFlood.Factory(server), familySISO, familyAISO); // Restricted
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
        registerIC("MC1242", "stocker", new ContainerStocker.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1243", "distributer", new Distributer.Factory(server), familySISO, familyAISO);
        registerIC("MC1244", "animal harv", new AnimalHarvester.Factory(server), familySISO, familyAISO);
        registerIC("MC1245", "cont stkr", new ContainerStacker.Factory(server), familySISO, familyAISO);
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
        registerIC("MC1265", "inv sns itm", new ItemNotSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1266", "sense power", new PowerSensor.Factory(server), familySISO, familyAISO);
        //FIXME registerIC("MC1267", "sense move", new MovementSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1268", "sns cntns", new ContentsSensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1269", "sns p cntns", new PlayerInventorySensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1270", "melody", new Melody.Factory(server), familySISO, familyAISO);
        registerIC("MC1271", "sns entity", new EntitySensor.Factory(server), familySISO, familyAISO);
        registerIC("MC1272", "sns player", new PlayerSensor.Factory(server), familySISO, familyAISO); // Restricted
        registerIC("MC1273", "jukebox", new Jukebox.Factory(server), familySISO, familyAISO);
        registerIC("MC1275", "tune", new Tune.Factory(server), familySISO, familyAISO);
        registerIC("MC1276", "radio station", new RadioStation.Factory(server), familySISO, familyAISO);
        registerIC("MC1277", "radio player", new RadioPlayer.Factory(server), familySISO, familyAISO);
        registerIC("MC1278", "sentry gun", new SentryGun.Factory(server), familySISO, familyAISO); //Restricted
        registerIC("MC1279", "player trap",new PlayerTrap.Factory(server), familySISO, familyAISO);
        registerIC("MC1280", "animal brd", new AnimalBreeder.Factory(server), familySISO, familyAISO);
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

        // SI5Os
        registerIC("MC6020", "random 5", new Random5Bit.Factory(server), familySI5O);

        // PLCs
        registerIC("MC5000", "perlstone", PlcFactory.fromLang(server, new Perlstone(), false, "MC5000"), familyVIVO);
        registerIC("MC5001", "perlstone 3i3o", PlcFactory.fromLang(server, new Perlstone(), false, "MC5001"), family3I3O);

        // Xtra ICs
        // SISOs
        registerIC("MCX230", "rain sense", new RainSensor.Factory(server), familySISO, familyAISO);
        registerIC("MCX231", "storm sense", new TStormSensor.Factory(server), familySISO, familyAISO);
        registerIC("MCX233", "weather set", new WeatherControl.Factory(server), familySISO, familyAISO);
        // 3ISOs
        registerIC("MCT233", "weather set ad", new WeatherControlAdvanced.Factory(server), family3ISO);

        //Variable ICs
        if(VariableManager.instance != null) {
            registerIC("VAR100", "num mod", new NumericModifier.Factory(server), familySISO, familyAISO);
            registerIC("VAR170", "at least", new IsAtLeast.Factory(server), familySISO, familyAISO);
            registerIC("VAR200", "item count", new ItemCounter.Factory(server), familySISO, familyAISO);
        }
    }

    public String getSearchID(Player p, String search) {

        ArrayList<String> icNameList = new ArrayList<>(registered.keySet());

        Collections.sort(icNameList);

        for (String ic : icNameList) {
            try {
                RegisteredICFactory ric = registered.get(ic);
                IC tic = ric.getFactory().create(null);
                if (search != null && !tic.getTitle().toLowerCase(Locale.ENGLISH).contains(search.toLowerCase(Locale.ENGLISH))
                        && !ric.getId().toLowerCase(Locale.ENGLISH).contains(search.toLowerCase(Locale.ENGLISH))) continue;

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

        ArrayList<String> icNameList = new ArrayList<>(registered.keySet());

        Collections.sort(icNameList);

        ArrayList<String> strings = new ArrayList<>();
        boolean col = true;
        for (String ic : icNameList) {
            try {
                thisIC:
                {
                RegisteredICFactory ric = registered.get(ic);
                IC tic = ric.getFactory().create(null);
                if (search != null && !tic.getTitle().toLowerCase(Locale.ENGLISH).contains(search.toLowerCase(Locale.ENGLISH))
                        && !ric.getId().toLowerCase(Locale.ENGLISH).contains(search.toLowerCase(Locale.ENGLISH))) continue;
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
                        else if (c == 'w' && !ric.getFactory().getClass().getPackage().getName().endsWith("weather"))
                            break thisIC;
                        else if (c == 'l' && !ric.getFactory().getClass().getPackage().getName().endsWith("logic"))
                            break thisIC;
                        else if (c == 'm' && !ric.getFactory().getClass().getPackage().getName().endsWith("miscellaneous"))
                            break thisIC;
                        else if (c == 'c' && !ric.getFactory().getClass().getPackage().getName().endsWith("sensors"))
                            break thisIC;
                        else if (c == 'v' && !ric.getFactory().getClass().getPackage().getName().endsWith("variables"))
                            break thisIC;

                    }
                }
                col = !col;
                ChatColor colour = col ? ChatColor.YELLOW : ChatColor.GOLD;

                if (!ICMechanic.checkPermissionsBoolean(CraftBookPlugin.inst().wrapPlayer(p), ric.getFactory(), ic.toLowerCase(Locale.ENGLISH))) {
                    colour = col ? ChatColor.RED : ChatColor.DARK_RED;
                }
                strings.add(colour + tic.getTitle() + " (" + ric.getId() + ")"
                        + ": " + (tic instanceof SelfTriggeredIC ? "ST " : "T ")
                        + (ric.getFactory() instanceof RestrictedIC ? ChatColor.DARK_RED + "R " : ""));
                }
            } catch (Throwable e) {
                CraftBookPlugin.logger().warning("An error occurred generating the docs for IC: " + ic + ".");
                CraftBookPlugin.logger().warning("Please report this error on: http://youtrack.sk89q.com/.");
            }
        }

        return strings.toArray(new String[strings.size()]);
    }
}
