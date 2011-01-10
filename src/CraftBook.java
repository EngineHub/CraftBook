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
import com.sk89q.craftbook.access.Action;
import com.sk89q.craftbook.access.Configuration;
import com.sk89q.craftbook.access.Event;
import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.WorldInterface;

/**
 * hMod interface for CraftBook
 */
public class CraftBook extends Plugin implements ServerInterface {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    public static final String DEFAULT_WORLD_NAME = "world";
    
    private CraftBookCore core = new CraftBookCore(this);
    private PluginLoader loader = etc.getLoader();
    private Server server = etc.getServer();
    
    private HmodWorldImpl world = new HmodWorldImpl(this);
    private List<WorldInterface> worlds;
    
    private IdentityHashMap<CraftBookDelegateListener,Object> listenerList =
        new IdentityHashMap<CraftBookDelegateListener,Object>();
    protected HashMap<Event,List<CraftBookDelegateListener>> events = 
        new HashMap<Event,List<CraftBookDelegateListener>>();
    {
        for(Event e:Event.values()) events.put(e, new ArrayList<CraftBookDelegateListener>());
    }
    
    private Configuration config;
    
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
        core.initialize();
    }

    @Override
    public void enable() {
        SignPatch.applyPatch();
        for(CraftBookDelegateListener l:listenerList.keySet()) l.loadConfiguration();
        core.enable();
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
    
    public boolean hasWorld(String world) {
        return world.equals(DEFAULT_WORLD_NAME);
    }
    public WorldInterface getWorld(String world) {
        return world.equals(DEFAULT_WORLD_NAME)?this.world:null;
    }
    public List<WorldInterface> getWorlds() {
        return worlds;
    }
    
    public WorldInterface getWorld() {
        return world;
    }

    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public void delayAction(Action a) {
        // TODO Auto-generated method stub
        
    }
}
