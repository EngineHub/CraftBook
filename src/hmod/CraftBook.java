/*    
Craftbook 
Copyright (C) 2010 sk89q <http://www.sk89q.com>
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.craftbook.CraftBookCore;
import com.sk89q.craftbook.CraftBookDelegateListener;
import com.sk89q.craftbook.access.Configuration;
import com.sk89q.craftbook.access.Event;
import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.WorldEditInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.state.StateManager;

/**
 * hMod interface for CraftBook
 */
public class CraftBook extends Plugin implements ServerInterface {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    
    private CraftBookCore core = new CraftBookCore(this);
    private PluginLoader loader = etc.getLoader();
    private Server server = etc.getServer();
    
    private HmodWorldImpl world = new HmodWorldImpl(this);
    private List<WorldInterface> worlds;
    
    private CraftBookListener listener = new CraftBookListener(this);

    /**
     * CraftBook version, fetched from the .jar's manifest. Used to print the
     * CraftBook version in various places.
     */
    private String version;
    
    private IdentityHashMap<CraftBookDelegateListener,Object> listenerList =
        new IdentityHashMap<CraftBookDelegateListener,Object>();
    protected HashMap<Event,List<CraftBookDelegateListener>> events = 
        new HashMap<Event,List<CraftBookDelegateListener>>();
    
    protected List<CraftBookDelegateListener> tickListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> signCreateListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> signChangeListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> commandListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> consoleCommandListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> wireInputListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> disconnectListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> blockPlaceListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> blockRightClickListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> blockDestroyedListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> vehiclePositionChangeListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> vehicleUpdateListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> vehicleDamageListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> vehicleEnterListeners = 
        new ArrayList<CraftBookDelegateListener>();
    protected List<CraftBookDelegateListener> vehicleDestroyListeners = 
        new ArrayList<CraftBookDelegateListener>();
    
    {
        events.put(Event.TICK, tickListeners);
        events.put(Event.SIGN_CREATE, signCreateListeners);
        events.put(Event.SIGN_CHANGE, signChangeListeners);
        events.put(Event.COMMAND, commandListeners);
        events.put(Event.CONSOLE_COMMAND, consoleCommandListeners);
        events.put(Event.WIRE_INPUT, wireInputListeners);
        events.put(Event.DISCONNECT, disconnectListeners);
        events.put(Event.BLOCK_PLACE, blockPlaceListeners);
        events.put(Event.BLOCK_DESTROYED, blockDestroyedListeners);
        events.put(Event.VEHICLE_POSITIONCHANGE, vehiclePositionChangeListeners);
        events.put(Event.VEHICLE_UPDATE, vehicleUpdateListeners);
        events.put(Event.VEHICLE_DAMAGE, vehicleDamageListeners);
        events.put(Event.VEHICLE_ENTERED, vehicleEnterListeners);
        events.put(Event.VEHICLE_DESTROYED, vehicleDestroyListeners);
    }

    /**
     * State manager object.
     */
    private HmodStateManager stateManager = new HmodStateManager(this);
        
    /**
     * State manager thread. 
     */
    private Thread stateThread = new Thread() {
        public void run() {
            PluginLoader pl = etc.getLoader();
            while(pl.getPlugin("CraftBook")==CraftBook.this) {
                try {Thread.sleep(10*60*1000);} catch (InterruptedException e) {}
                if(isEnabled()&&pl.getPlugin("CraftBook")==CraftBook.this) try {
                    stateManager.saveAll();
                } catch (IOException e) {
                    logger.log(Level.WARNING,"failed to save state", e);
                }
            }
        }
    };
    
    private Configuration config;
    
    protected String name;
    protected File path;
    protected File pathToWorldState;
    protected File pathToGlobalState;
    protected File pathToToggleAreas;
    
    private HmodWorldEditBridge worldEdit = new HmodWorldEditBridge();
    
    public CraftBook() {
        List<WorldInterface> worlds = new ArrayList<WorldInterface>();
        worlds.add(world);
        this.worlds = Collections.unmodifiableList(worlds);
        
        try {
            config = new HmodConfigurationImpl(new PropertiesFile("craftbook.properties"));
        } catch (IOException e) {
            logger.warning("Failed to load craftbook.properties: " + e.getMessage());
        }
    }
    
    public void initialize() {
        PluginLoader l = etc.getLoader();
        
        TickPatch.applyPatch();
        TickPatch.addTask(TickPatch.wrapRunnable(this,listener));
        SignPatch.addListener(SignPatch.wrapListener(this, listener));
        l.addListener(PluginLoader.Hook.SIGN_CHANGE, listener, this, PluginListener.Priority.MEDIUM);
        l.addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
        l.addListener(PluginLoader.Hook.SERVERCOMMAND, listener, this, PluginListener.Priority.MEDIUM);
        l.addListener(PluginLoader.Hook.REDSTONE_CHANGE, listener, this, PluginListener.Priority.MEDIUM);
        
        core.initialize();
        
        logger.log(Level.INFO, "CraftBook version " + getCraftBookVersion() + " initialized");
        
        PropertiesFile prop = new PropertiesFile("server.properties");
        String tName = "world"; 
        try {
            prop.load();
            if (prop.containsKey("level-name"))
                tName = prop.getString("level-name");
        } catch (IOException e) {
            logger.log(Level.WARNING,"server.properties missing",e);
        } finally {
            name = tName;
            path = new File(tName);
            File pathToState = new File(path,"craftbook");
            pathToWorldState = new File(pathToState,"world");
            pathToGlobalState = new File(pathToState,"global");
            pathToToggleAreas = new File(pathToState,"areas");
        }
        
        stateThread.setName("StateManager-"+name);
        stateThread.start();
    }

    public void loadConfiguration() {
        for(CraftBookDelegateListener l:listenerList.keySet()) l.loadConfiguration();
        core.loadConfiguration();
    }
    
    @Override
    public void enable() {
        SignPatch.applyPatch();
        loadConfiguration();
        core.enable();

        try {
            stateManager.loadAll();
        } catch (IOException e) {
            logger.log(Level.WARNING,"failed to load state", e);
        }
        
        logger.log(Level.INFO, "CraftBook version " + getCraftBookVersion() + " enabled");
    }
    
    @Override
    public void disable() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().contains("MinecartMania")) {
                etc.getServer().addToServerQueue(new Runnable() {
                    public void run() {
                        try {
                            etc.getLoader().disablePlugin("MinecartMania");
                            logger.warning("Minecart Mania has been disabled.");
                        } finally {
                            etc.getLoader().enablePlugin("CraftBook");
                        }
                    }
                });
                
                return;
            }
        }

        SignPatch.removePatch();
        core.disable();

        try {
            stateManager.saveAll();
        } catch (IOException e) {
            logger.log(Level.WARNING,"failed to save state", e);
        }

        logger.log(Level.INFO, "CraftBook version " + getCraftBookVersion() + " disabled");
    }

    /**
     * Get the CraftBook version.
     *
     * @return
     */
    public String getCraftBookVersion() {
        if (version != null) {
            return version;
        }
        
        Package p = CraftBookCore.class.getPackage();
        
        if (p == null) {
            p = Package.getPackage("com.sk89q.craftbook");
        }
        
        if (p == null) {
            version = "(unknown)";
        } else {
            version = p.getImplementationVersion();
            
            if (version == null) {
                version = "(unknown)";
            }
        }

        return version;
    }
    
    /**
     * Conditionally registers a hook for a listener.
     * 
     * @param name
     * @param priority
     * @return whether the hook was registered correctly
     */
    public boolean registerHook(PluginListener listener,
            String name, PluginListener.Priority priority) {
        try {
            PluginLoader.Hook hook = PluginLoader.Hook.valueOf(name);
            etc.getLoader().addListener(hook, listener, this, priority);
            return true;
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "CraftBook: Missing hook " + name + "!");
            return false;
        }
    }

    public boolean isCraftBookLoaded() {
        return loader.getPlugin(getClass().getName()) == this;
    }
    public boolean isCraftBookEnabled() {
        return isEnabled();
    }

    public boolean isPlayerOnline(String player) {
        return server.getPlayer(player) != null;
    }

    // TODO: Optimize
    public PlayerInterface getPlayer(String player) {
        return new HmodPlayerImpl(server.getPlayer(player),this);
    }

    // TODO: Optimize
    public PlayerInterface matchPlayer(String player) {
        return new HmodPlayerImpl(server.matchPlayer(player),this);
    }

    // TODO: Optimize
    public List<PlayerInterface> getPlayerList() {
        List<Player> list = server.getPlayerList();
        List<PlayerInterface> list2 = new ArrayList<PlayerInterface>();
        
        for(Player p:list) list2.add(new HmodPlayerImpl(p,this));
        
        return list2;
    }
 
    public void registerListener(Event e, CraftBookDelegateListener l) {
        listenerList.put(l, null);
        events.get(e).add(l);
    }
    
    public WorldInterface matchWorldName(String world) {
        return world.equals(this.world.getName())?this.world:null;
    }
    public WorldInterface getWorld(String id) {
        return world.equals(this.world.getUniqueIdString())?this.world:null;
    }
    public List<WorldInterface> getWorlds() {
        return worlds;
    }
    
    public HmodWorldImpl getWorld() {
        return world;
    }

    public Configuration getConfiguration() {
        return config;
    }

    public void sendMessage(String message) {
        world.sendMessage(message);
    }

    public StateManager getStateManager() {
        return stateManager;
    }

    public WorldEditInterface getWorldEditBridge() {
        return worldEdit;
    }    
}
