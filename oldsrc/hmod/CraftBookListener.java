// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.CraftBookDelegateListener;
import com.sk89q.craftbook.InsufficientArgumentsException;
import com.sk89q.craftbook.access.BaseEntityInterface;
import com.sk89q.craftbook.access.MinecartInterface;
import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.SignInterface;
import com.sk89q.craftbook.util.BlockVector;
import com.sk89q.craftbook.util.Vector;

/**
 * This is CraftBook's main event listener for Hey0's server mod. "Delegate"
 * listeners are also used for different features and this listener acts
 * as a proxy for some custom hooks and events.
 *
 * @author sk89q
 */
public class CraftBookListener extends PluginListener
                            implements Runnable,
                                       SignPatch.ExtensionListener {
    /**
     * Logger instance.
     */
    static final Logger logger = Logger.getLogger("Minecraft.CraftBook");

    /**
     * Stores an instance of the plugin. This is used to register delegate
     * listeners and access some global features.
     */
    private CraftBook main; 
   
    private HmodWorldImpl w;

    /**
     * Construct the object.
     * 
     * @param craftBook
     */
    public CraftBookListener(CraftBook craftBook) {
        main = craftBook;
        w = main.getWorld();
    }

    public void run() {
        for(CraftBookDelegateListener l:main.tickListeners) l.onTick(w);
    }
    
    public void onSignAdded(int x, int y, int z) {
        for(CraftBookDelegateListener l:main.signCreateListeners) l.onSignCreate(w,x,y,z);
    }
    
    public boolean onSignChange(Player p, Sign sp) {
        PlayerInterface player = new HmodPlayerImpl(p, w);
        BlockVector signPosition = new BlockVector(sp.getX(),sp.getY(),sp.getZ());
        SignInterface s = new HmodSignImpl(w, signPosition, sp);
        for(CraftBookDelegateListener l:main.signChangeListeners) 
            if(l.onSignChange(player,w,signPosition,s)) return true;
        return false;
    }
    
    /**
     * Called on command.
     * 
     * @param player
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCommand(Player player, String[] split) {
        try {
            if (split[0].equalsIgnoreCase("/reload")
                    && player.canUseCommand("/reload")
                    && split.length > 1
                    && (split[1].equalsIgnoreCase("CraftBook")
                        || split[1].equals("*"))) {
            
                // Redirect log messages to the player's chat.
                LoggerToChatHandler handler = new LoggerToChatHandler(player);
                handler.setLevel(Level.ALL);
                Logger minecraftLogger = Logger.getLogger("Minecraft");
                minecraftLogger.addHandler(handler);

                try {
                    main.loadConfiguration();
                    player.sendMessage("CraftBook configuration reloaded.");
                } catch (Throwable t) {
                    player.sendMessage("Error while reloading: "
                            + t.getMessage());
                } finally {
                    minecraftLogger.removeHandler(handler);
                }

                return !split[1].equals("*");
            }

            PlayerInterface myPlayer = new HmodPlayerImpl(player, w);
            
            for (CraftBookDelegateListener listener : main.commandListeners)
                if (listener.onCommand(myPlayer, split)) return true;

            return false;
        } catch (InsufficientArgumentsException e) {
            player.sendMessage(Colors.Rose + e.getMessage());
            return true;
        }
    }
        
    public boolean onConsoleCommand(String[] split) {
        for (CraftBookDelegateListener listener : main.consoleCommandListeners)
            if (listener.onConsoleCommand(split)) return true;
        
        return false;
    }

    /**
     * Called on redstone change.
     *
     * @param block
     * @param oldLevel
     * @param newLevel
     */
    public int onRedstoneChange(Block block, int oldLevel, int newLevel) {
        BlockVector v = new BlockVector(block.getX(), block.getY(), block.getZ());
        
        // Give the method a BlockVector instead of a Block
        boolean wasOn = oldLevel >= 1;
        boolean isOn = newLevel >= 1;
        boolean wasChange = wasOn != isOn;

        // For efficiency reasons, we're only going to consider changes between
        // off and on state, and ignore simple current changes (i.e. 15->13)
        if (!wasChange) {
            return newLevel;
        }

        int x = v.getBlockX();
        int y = v.getBlockY();
        int z = v.getBlockZ();
        
        int type = w.getId(x, y, z);

        // When this hook has been called, the level in the world has not
        // yet been updated, so we're going to do this very ugly thing of
        // faking the value with the new one whenever the data value of this
        // block is requested -- it is quite ugly
        try {
            if (type == BlockType.LEVER) {
                // Fake data
                w.fakeData(x, y, z,
                    newLevel > 0
                        ? w.getData(x, y, z) | 0x8
                        : w.getData(x, y, z) & 0x7);
            } else if (type == BlockType.STONE_PRESSURE_PLATE) {
                // Fake data
                w.fakeData(x, y, z,
                    newLevel > 0
                        ? w.getData(x, y, z) | 0x1
                        : w.getData(x, y, z) & 0x14);
            } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
                // Fake data
                w.fakeData(x, y, z,
                    newLevel > 0
                        ? w.getData(x, y, z) | 0x1
                        : w.getData(x, y, z) & 0x14);
            } else if (type == BlockType.STONE_BUTTON) {
                // Fake data
                w.fakeData(x, y, z,
                    newLevel > 0
                        ? w.getData(x, y, z) | 0x8
                        : w.getData(x, y, z) & 0x7);
            } else if (type == BlockType.REDSTONE_WIRE) {
                // Fake data
                w.fakeData(x, y, z, newLevel);

                int westSide = w.getId(x, y, z + 1);
                int westSideAbove = w.getId(x, y + 1, z + 1);
                int westSideBelow = w.getId(x, y - 1, z + 1);
                int eastSide = w.getId(x, y, z - 1);
                int eastSideAbove = w.getId(x, y + 1, z - 1);
                int eastSideBelow = w.getId(x, y - 1, z - 1);

                int northSide = w.getId(x - 1, y, z);
                int northSideAbove = w.getId(x - 1, y + 1, z);
                int northSideBelow = w.getId(x - 1, y - 1, z);
                int southSide = w.getId(x + 1, y, z);
                int southSideAbove = w.getId(x + 1, y + 1, z);
                int southSideBelow = w.getId(x + 1, y - 1, z);

                // Make sure that the wire points to only this block
                if (!BlockType.isRedstoneBlock(westSide)
                        && !BlockType.isRedstoneBlock(eastSide)
                        && (!BlockType.isRedstoneBlock(westSideAbove) || westSide == 0)
                        && (!BlockType.isRedstoneBlock(eastSideAbove) || eastSide == 0)
                        && (!BlockType.isRedstoneBlock(westSideBelow) || westSide != 0)
                        && (!BlockType.isRedstoneBlock(eastSideBelow) || eastSide != 0)) {
                    // Possible blocks north / south
                    handleDirectWireInput(new Vector(x - 1, y, z), isOn, v);
                    handleDirectWireInput(new Vector(x + 1, y, z), isOn, v);
                    handleDirectWireInput(new Vector(x - 1, y - 1, z), isOn, v);
                    handleDirectWireInput(new Vector(x + 1, y - 1, z), isOn, v);
                }

                if (!BlockType.isRedstoneBlock(northSide)
                        && !BlockType.isRedstoneBlock(southSide)
                        && (!BlockType.isRedstoneBlock(northSideAbove) || northSide == 0)
                        && (!BlockType.isRedstoneBlock(southSideAbove) || southSide == 0)
                        && (!BlockType.isRedstoneBlock(northSideBelow) || northSide != 0)
                        && (!BlockType.isRedstoneBlock(southSideBelow) || southSide != 0)) {
                    // Possible blocks west / east
                    handleDirectWireInput(new Vector(x, y, z - 1), isOn, v);
                    handleDirectWireInput(new Vector(x, y, z + 1), isOn, v);
                    handleDirectWireInput(new Vector(x, y - 1, z - 1), isOn, v);
                    handleDirectWireInput(new Vector(x, y - 1, z + 1), isOn, v);
                }

                // Can be triggered from below
                handleDirectWireInput(new Vector(x, y + 1, z), isOn, v);

                return newLevel;
            }

            // For redstone wires, the code already exited this method
            // Non-wire blocks proceed

            handleDirectWireInput(new Vector(x - 1, y, z), isOn, v);
            handleDirectWireInput(new Vector(x + 1, y, z), isOn, v);
            handleDirectWireInput(new Vector(x - 1, y - 1, z), isOn, v);
            handleDirectWireInput(new Vector(x + 1, y - 1, z), isOn, v);
            handleDirectWireInput(new Vector(x, y, z - 1), isOn, v);
            handleDirectWireInput(new Vector(x, y, z + 1), isOn, v);
            handleDirectWireInput(new Vector(x, y - 1, z - 1), isOn, v);
            handleDirectWireInput(new Vector(x, y - 1, z + 1), isOn, v);

            // Can be triggered from below
            handleDirectWireInput(new Vector(x, y + 1, z), isOn, v);

            return newLevel;
        } finally {
            w.destroyFake();
        }
    }
    
    /**
     * Handles direct redstone input. This method merely passes the call
     * onto the delegates for further processing. If a delegate throws an
     * exception, it will not be caught here.
     * 
     * @param inputVec
     * @param isOn
     * @param changed
     * @see CraftBookDelegateListener#onDirectWireInput(Vector, boolean, Vector)
     */
    public void handleDirectWireInput(Vector pt, boolean isOn, Vector changed) {
        // Call the direct wire input hook of delegates
        for (CraftBookDelegateListener listener : main.wireInputListeners) 
            listener.onWireInput(w, pt, isOn, changed);
    }
    
    public void onDisconnect(Player p) {
        PlayerInterface player = new HmodPlayerImpl(p, w);
        for (CraftBookDelegateListener listener : main.disconnectListeners) 
            listener.onDisconnect(player);
    }
    
    public boolean onBlockPlace(Player p, Block pp, Block cp, Item itemInHand) {
        PlayerInterface player = new HmodPlayerImpl(p, w);
        BlockVector pv = new BlockVector(pp.getX(),pp.getY(),pp.getZ());
        BlockVector cv = new BlockVector(cp.getX(),cp.getY(),cp.getZ());
        for (CraftBookDelegateListener listener : main.blockPlaceListeners) 
            if(listener.onBlockPlace(w,player,pv,cv,itemInHand.getItemId()))
                return true;
        return false;
    }
    
    public void onBlockRightClicked(Player p, Block cp, Item itemInHand) {
        PlayerInterface player = new HmodPlayerImpl(p, w);
        BlockVector cv = new BlockVector(cp.getX(),cp.getY(),cp.getZ());
        for (CraftBookDelegateListener listener : main.blockRightClickListeners) 
            listener.onBlockRightClicked(w,player,cv,itemInHand.getItemId());
    }
    
    public boolean onBlockDestroy(Player p, Block dp) {
        PlayerInterface player = new HmodPlayerImpl(p, w);
        BlockVector dv = new BlockVector(dp.getX(),dp.getY(),dp.getZ());
        for (CraftBookDelegateListener listener : main.blockDestroyedListeners) 
            if(listener.onBlockDestroy(w, player, dv, dp.getStatus()))
                return true;
        return false;
    }
    
    public boolean onVehicleDamage(BaseVehicle vehicle, BaseEntity entity, int damage) {
        if(!(vehicle instanceof Minecart)) return false;
        MinecartInterface m = getMinecart((Minecart)vehicle);
        BaseEntityInterface e = new HmodBaseEntityImpl(entity, w);
        for (CraftBookDelegateListener listener : main.minecartDamageListeners) 
            if(listener.onMinecartDamage(w, m, e, damage))
                return true;
        return false;
    }
    
    public void onVehicleUpdate(BaseVehicle vehicle) {
        if(!(vehicle instanceof Minecart)) return;
        MinecartInterface m = getMinecart((Minecart)vehicle);
        for (CraftBookDelegateListener listener : main.minecartVelocityChangeListeners) 
            listener.onMinecartVelocityChange(w, m);
    }
    
    public void onVehiclePositionChange(BaseVehicle vehicle, int x, int y, int z) {
        if(!(vehicle instanceof Minecart)) return;
        MinecartInterface m = getMinecart((Minecart)vehicle);
        for (CraftBookDelegateListener listener : main.minecartPositionChangeListeners) 
            listener.onMinecartPositionChange(w, m, x, y, z);
    }
    
    public void onVehicleEnter(BaseVehicle vehicle, HumanEntity entity) {
        if(!(vehicle instanceof Minecart)) return;
        MinecartInterface m = getMinecart((Minecart)vehicle);
        BaseEntityInterface e = new HmodBaseEntityImpl(entity, w);
        boolean entering = vehicle.getPassenger()==null;
        for (CraftBookDelegateListener listener : main.minecartEnterListeners) 
            listener.onMinecartEnter(w, m, e, entering);
    }
    
    public void onVehicleDestroyed(BaseVehicle vehicle) {
        if(!(vehicle instanceof Minecart)) return;
        MinecartInterface m = getMinecart((Minecart)vehicle);
        for (CraftBookDelegateListener listener : main.minecartDestroyListeners) 
            listener.onMinecartDestroyed(w, m);
    }
    
    private MinecartInterface getMinecart(Minecart m) {
        if(m.getStorage()==null)
            return new HmodMinecartImpl(m,w);
        else
            return new HmodStorageMinecartImpl(m,w);
    }
}