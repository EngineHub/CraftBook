// Id
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook;

import static com.sk89q.worldedit.bukkit.BukkitUtil.toWorldVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.ICMechanic;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.BlockWorldVector2D;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * A MechanicManager tracks the Vector where loaded Mechanic instances have registered triggerability,
 * and dispatches incoming events by checking
 * for Mechanic instance that might be triggered by the event and by considering instantiation of a new Mechanic
 * instance for unregistered Vector.
 *
 * @author sk89q
 * @author hash
 */
public class MechanicManager {

    /**
     * List of factories that will be used to detect mechanisms at a location.
     */
    public final LinkedList<MechanicFactory<? extends Mechanic>> factories;

    /**
     * Keeps track of trigger blocks. Trigger blocks are the blocks that will activate mechanics. No block can be a
     * trigger block for two mechanics at
     * once. An example of a trigger block is a sign block with [Gate] as the second line, triggering the gate mechanic.
     */
    private final TriggerBlockManager triggersManager;

    /**
     * Keeps track of watch blocks. A persistent mechanic have several watch blocks that entail the blocks that the
     * mechanic may use. These blocks may
     * not be trigger blocks and are probably not. Watch blocks aren't utilized yet.
     */
    private final WatchBlockManager watchBlockManager;

    /**
     * List of mechanics that think on a routine basis.
     */
    public final Set<SelfTriggeringMechanic> thinkingMechanics = new LinkedHashSet<SelfTriggeringMechanic>();

    /**
     * Construct the manager.
     */
    public MechanicManager() {

        factories = new LinkedList<MechanicFactory<? extends Mechanic>>();
        triggersManager = new TriggerBlockManager();
        watchBlockManager = new WatchBlockManager();
    }

    /**
     * Register a mechanic factory.
     *
     * @param factory
     */
    public void register(MechanicFactory<? extends Mechanic> factory) {

        if (!factories.contains(factory)) {
            factories.add(factory);
        }
    }

    /**
     * Unregister a mechanic factory.
     *
     * @param factory
     */
    public boolean unregister(MechanicFactory<? extends Mechanic> factory) {

        if (factories.contains(factory)) {
            factories.remove(factory);
            return true;
        }
        return false;
    }

    /**
     * Unregister a mechanic factory, using a iterator object.
     *
     * @param factory
     */
    public boolean unregister(Iterator<MechanicFactory<? extends Mechanic>> factory) {

        factory.remove();
        return true;
    }

    /**
     * Handle a block right click event.
     *
     * @param event
     *
     * @return true if there was a mechanic to process the event
     */
    public boolean dispatchSignChange(SignChangeEvent event) {
        // We don't need to handle events that no mechanic we use makes use of
        if (!passesFilter(event)) return false;

        // See if this event could be occurring on any mechanism's triggering blocks
        Block block = event.getBlock();
        BlockWorldVector pos = toWorldVector(block);
        LocalPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        BlockState state = event.getBlock().getState();

        if (!(state instanceof Sign)) return false;

        Sign sign = (Sign) state;

        try {
            load(pos, localPlayer, BukkitUtil.toChangedSign(sign, event.getLines(), localPlayer));
        } catch (InvalidMechanismException e) {
            if (e.getMessage() != null)
                localPlayer.printError(e.getMessage());

            event.setCancelled(true);
            block.getWorld().dropItem(block.getLocation(), new ItemStack(ItemID.SIGN, 1));
            block.setTypeId(0);
        }

        return false;
    }

    /**
     * Handle a block break event.
     *
     * @param event
     *
     * @return the number of mechanics to processed
     */
    public short dispatchBlockBreak(BlockBreakEvent event) {

        CraftBookPlugin plugin = CraftBookPlugin.inst();

        // We don't need to handle events that no mechanic we use makes use of
        if (!passesFilter(event)) return 0;

        short returnValue = 0;
        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());

        // Announce the event to anyone who considers it to be on one of their defining blocks
        watchBlockManager.notify(event);

        // See if this event could be occurring on any mechanism's triggering blocks
        BlockWorldVector pos = toWorldVector(event.getBlock());

        try {
            HashSet<Mechanic> mechanics = load(pos, player);
            if(mechanics.size() > 0) {
                // A mechanic has been found, check if we can actually build here.
                if (!plugin.canBuild(event.getPlayer(), event.getBlock().getLocation(), false)) {
                    if(plugin.getConfiguration().showPermissionMessages)
                        player.printError("area.break-permissions");
                    return 0;
                }
            }
            for (Mechanic aMechanic : mechanics) {
                if (aMechanic != null) {

                    if(plugin.getConfiguration().advancedBlockChecks && event.isCancelled())
                        return returnValue;

                    aMechanic.onBlockBreak(event);
                    returnValue++;
                }
            }
        } catch (InvalidMechanismException e) {
            if (e.getMessage() != null) {
                player.printError(e.getMessage());
            }
        }
        return returnValue;
    }

    /**
     * Handle a block right click event.
     *
     * @param event
     *
     * @return the number of mechanics to processed
     */
    public short dispatchBlockRightClick(PlayerInteractEvent event) {

        CraftBookPlugin plugin = CraftBookPlugin.inst();

        // We don't need to handle events that no mechanic we use makes use of
        if (!passesFilter(event)) return 0;

        short returnValue = 0;
        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());

        // See if this event could be occurring on any mechanism's triggering blocks
        BlockWorldVector pos = toWorldVector(event.getClickedBlock());

        try {
            HashSet<Mechanic> mechanics = load(pos, player);
            if(mechanics.size() > 0) {
                if (!plugin.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
                    if(plugin.getConfiguration().showPermissionMessages)
                        player.printError("area.use-permissions");
                    return 0;
                }
            }
            for (Mechanic aMechanic : mechanics) {
                if (aMechanic != null) {

                    if(plugin.getConfiguration().advancedBlockChecks && event.isCancelled())
                        return returnValue;

                    aMechanic.onRightClick(event);
                    returnValue++;
                }
            }
        } catch (InvalidMechanismException e) {
            if (e.getMessage() != null) {
                player.printError(e.getMessage());
            }
        }
        return returnValue;
    }

    /**
     * Handle a block left click event.
     *
     * @param event
     *
     * @return the number of mechanics to processed
     */
    public short dispatchBlockLeftClick(PlayerInteractEvent event) {

        CraftBookPlugin plugin = CraftBookPlugin.inst();

        // We don't need to handle events that no mechanic we use makes use of
        if (!passesFilter(event)) return 0;

        short returnValue = 0;
        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());

        // See if this event could be occurring on any mechanism's triggering blocks
        BlockWorldVector pos = toWorldVector(event.getClickedBlock());
        try {
            HashSet<Mechanic> mechanics = load(pos, player);
            if(mechanics.size() > 0) {
                if (!plugin.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
                    //player.printError("area.permissions"); Don't show a message for this.
                    return 0;
                }
            }
            for (Mechanic aMechanic : mechanics) {
                if (aMechanic != null) {

                    if(plugin.getConfiguration().advancedBlockChecks && event.isCancelled())
                        return returnValue;

                    aMechanic.onLeftClick(event);
                    returnValue++;
                }
            }
        } catch (InvalidMechanismException e) {
            if (e.getMessage() != null) {
                player.printError(e.getMessage());
            }
        }

        return returnValue;
    }

    /**
     * Handle the redstone block change event.
     *
     * @param event
     *
     * @return the number of mechanics to processed
     */
    public short dispatchBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        // We don't need to handle events that no mechanic we use makes use of
        if (!passesFilter(event)) return 0;

        short returnValue = 0;
        // See if this event could be occurring on any mechanism's triggering blocks
        BlockWorldVector pos = toWorldVector(event.getBlock());
        try {
            HashSet<Mechanic> mechanics = load(pos, null);
            for (Mechanic aMechanic : mechanics) {
                if (aMechanic != null) {
                    aMechanic.onBlockRedstoneChange(event);
                    returnValue++;
                }
            }
        } catch (InvalidMechanismException ignored) {
        }

        return returnValue;
    }

    /**
     * Load a Mechanic at a position. May return an already existing PersistentMechanic if one is triggered at that
     * position, or return a new Mechanic
     * (persistent or otherwise; if the new mechanic is persistent, it will have already been registered with this
     * manager).
     *
     * @param pos
     * @param player If a player is available, the player who is interacting.
     *
     * @return a list of all {@link Mechanic} at the location;
     *
     * @throws InvalidMechanismException if it appears that the position is intended to me a mechanism,
     *                                   but the mechanism is misconfigured and inoperable.
     */
    public HashSet<Mechanic> load(BlockWorldVector pos, LocalPlayer player) throws InvalidMechanismException {

        HashSet<Mechanic> detectedMechanics = detect(pos);
        if(player != null)
            detectedMechanics.addAll(detect(pos,player));

        return loadDetectedMechanics(pos, detectedMechanics);
    }

    /**
     * Load a Mechanic at a position.
     *
     * @param pos
     * @param player
     * @param sign
     *
     * @return a list of all {@link Mechanic} at the location;
     *
     * @throws InvalidMechanismException if it appears that the position is intended to me a mechanism,
     *                                   but the mechanism is misconfigured and inoperable.
     */
    protected HashSet<Mechanic> load(BlockWorldVector pos, LocalPlayer player,
            ChangedSign sign) throws InvalidMechanismException {

        return loadDetectedMechanics(pos, detect(pos, player, sign));
    }

    private HashSet<Mechanic> loadDetectedMechanics(BlockWorldVector pos, HashSet<Mechanic> detectedMechanics) {
        PersistentMechanic ptMechanic = triggersManager.get(pos);

        if (ptMechanic != null && !ptMechanic.isActive()) {
            unload(ptMechanic, null);
            ptMechanic = null;
        }

        // No mechanic detected!
        if (ptMechanic == null) {
            for (Mechanic aMechanic : detectedMechanics) {
                if (aMechanic == null) {
                    continue;
                }

                // Register mechanic if it's a persistent type
                if (aMechanic instanceof PersistentMechanic) {
                    PersistentMechanic pm = (PersistentMechanic) aMechanic;
                    triggersManager.register(pm);
                    watchBlockManager.register(pm);

                    if (aMechanic instanceof SelfTriggeringMechanic) {
                        synchronized (this) {
                            thinkingMechanics.add((SelfTriggeringMechanic) aMechanic);
                        }
                    }
                    break;
                }
            }
        }

        // Lets handle what happens when ptMechanic is here
        if (ptMechanic != null) {

            List<Mechanic> removedMechanics = new ArrayList<Mechanic>();
            for (Mechanic aMechanic : detectedMechanics) {
                if (ptMechanic.getClass().equals(aMechanic.getClass())) {
                    removedMechanics.add(aMechanic);
                }
            }

            detectedMechanics.removeAll(removedMechanics);
            detectedMechanics.add(ptMechanic);
        }

        return detectedMechanics;
    }

    /**
     * Attempt to detect a mechanic at a location. This is only called in response to events for which a trigger
     * block for an existing
     * PersistentMechanic cannot be found.
     *
     * @param pos
     *
     * @return a {@link Mechanic} if a mechanism could be found at the location; null otherwise
     *
     * @throws InvalidMechanismException if it appears that the position is intended to me a mechanism,
     *                                   but the mechanism is misconfigured and inoperable.
     */
    protected HashSet<Mechanic> detect(BlockWorldVector pos) throws InvalidMechanismException {

        HashSet<Mechanic> mechanics = new HashSet<Mechanic>();

        for (MechanicFactory<? extends Mechanic> factory : factories) {
            Mechanic mechanic;
            if ((mechanic = factory.detect(pos)) != null) {
                mechanics.add(mechanic);
            }
        }
        return mechanics;
    }

    /**
     * Attempt to detect a mechanic at a location. This is only called in response to events for which a trigger
     * block for an existing
     * PersistentMechanic cannot be found.
     *
     * @param pos
     * @param player
     *
     * @return a {@link Mechanic} if a mechanism could be found at the location; null otherwise
     *
     * @throws InvalidMechanismException if it appears that the position is intended to me a mechanism,
     *                                   but the mechanism is misconfigured and inoperable.
     */
    protected HashSet<Mechanic> detect(BlockWorldVector pos, LocalPlayer player) throws InvalidMechanismException {

        HashSet<Mechanic> mechanics = new HashSet<Mechanic>();

        for (MechanicFactory<? extends Mechanic> factory : factories) {
            Mechanic mechanic;
            if ((mechanic = factory.detect(pos, player)) != null) {
                mechanics.add(mechanic);
            }
        }
        return mechanics;
    }

    /**
     * Attempt to detect a mechanic at a location, with player information available.
     *
     * @param pos
     * @param player
     *
     * @return a {@link Mechanic} if a mechanism could be found at the location; null otherwise
     *
     * @throws InvalidMechanismException if it appears that the position is intended to me a mechanism,
     *                                   but the mechanism is misconfigured and inoperable.
     */
    protected HashSet<Mechanic> detect(BlockWorldVector pos, LocalPlayer player,
            ChangedSign sign) throws InvalidMechanismException {

        HashSet<Mechanic> mechanics = new HashSet<Mechanic>();

        for (MechanicFactory<? extends Mechanic> factory : factories) {
            try {
                Mechanic mechanic;
                if ((mechanic = factory.detect(pos, player, sign)) != null) {
                    mechanics.add(mechanic);
                }
            } catch (ProcessedMechanismException ignored) {
                // Do nothing here one screwed up mech doesn't mean all them are wrong
            }
        }
        return mechanics;
    }

    /**
     * Used to filter events for processing. This allows for short circuiting code so that code isn't checked
     * unnecessarily.
     *
     * @param event
     *
     * @return true if the event should be processed by this manager; false otherwise.
     */
    protected boolean passesFilter(Event event) {

        if(event instanceof Cancellable && ((Cancellable) event).isCancelled() && CraftBookPlugin.inst().getConfiguration().advancedBlockChecks)
            return false;

        return true;
    }

    /**
     * Handles chunk load.
     *
     * @param chunk
     */
    public void enumerate(Chunk chunk) {

        try {
            for (BlockState state : chunk.getTileEntities()) {
                if (state == null) continue;
                if (state instanceof Sign) {
                    try {
                        load(BukkitUtil.toWorldVector(state.getBlock()), null);
                    } catch (InvalidMechanismException ignored) {
                    } catch (Throwable t) {
                        BukkitUtil.printStacktrace(t);
                    }
                }
            }
        } catch (NullPointerException e) {
            // Ignore: Generally thrown by Bukkit even for valid chunks
        } catch (Throwable error) {

            BukkitUtil.printStacktrace(error);
            CraftBookPlugin.logger().severe("A corruption issue has been found at chunk ("
                    + chunk.getX() + ", " + chunk.getZ() + ") Self-Triggering mechanics " +
                    "may not work as expected until this is resolved!");
            // TODO This probably needs formatted better
            CraftBookPlugin.logger().severe("Chunk (" + chunk.getX() + ", " + chunk.getZ() + ") starts at " +
                    chunk.getBlock(0, 0, 0).getLocation().toString() + " and ends at " +
                    chunk.getBlock(15, 255, 15).getLocation().toString() + '.');
        }
    }

    /**
     * Unload all mechanics inside the given chunk.
     *
     * @param chunk
     */
    public void unload(BlockWorldVector2D chunk, ChunkUnloadEvent event) {
        // Find mechanics that we need to unload
        Set<PersistentMechanic> applicable = triggersManager.getByChunk(chunk);
        applicable.addAll(watchBlockManager.getByChunk(chunk));

        for (Mechanic m : applicable) {
            if (event != null && event.isCancelled())
                return;
            unload(m, event);
        }
    }

    /**
     * Unloads the mechanics at the given position.
     * 
     * @param position
     * @param event
     */
    public void unload(BlockWorldVector position, ChunkUnloadEvent event) {

        Set<PersistentMechanic> applicable = new HashSet<PersistentMechanic>();
        applicable.add(triggersManager.get(position));
        applicable.addAll(watchBlockManager.get(position));

        for (Mechanic m : applicable) {
            if (event != null && event.isCancelled())
                return;
            unload(m, event);
        }
    }

    /**
     * Unload a mechanic. This will also remove the trigger points from this mechanic manager.
     *
     * @param mechanic
     */
    protected void unload(Mechanic mechanic, ChunkUnloadEvent event) {

        if (mechanic == null) {
            CraftBookPlugin.logger().log(Level.WARNING, "CraftBook mechanic: Failed to unload(Mechanic) - null.");
            return;
        }


        if(CraftBookPlugin.inst().getConfiguration().ICKeepLoaded && mechanic instanceof ICMechanic && event != null && ((PersistentMechanic) mechanic).isActive()) {
            event.setCancelled(true);
            return;
        }

        try {
            if (event != null) {
                mechanic.unloadWithEvent(event);
                if(event.isCancelled())
                    return;
            }
            mechanic.unload();
        } catch (Throwable t) { // Mechanic failed to unload for some reason
            CraftBookPlugin.logger().log(Level.WARNING, "CraftBook mechanic: Failed to unload " + mechanic.getClass().getSimpleName());
            BukkitUtil.printStacktrace(t);
        }

        if (event != null && event.isCancelled())
            return;

        synchronized (this) {
            thinkingMechanics.remove(mechanic);
        }

        if (mechanic instanceof PersistentMechanic) {
            PersistentMechanic pm = (PersistentMechanic) mechanic;
            triggersManager.deregister(pm);
            watchBlockManager.deregister(pm);
        }
    }

    /**
     * Causes all thinking mechanics to think.
     */
    public void think() {

        SelfTriggeringMechanic[] mechs;

        synchronized (this) {
            // Copy to array to get rid of concurrency snafus
            mechs = thinkingMechanics.toArray(new SelfTriggeringMechanic[thinkingMechanics.size()]);
        }

        for (SelfTriggeringMechanic mechanic : mechs) {
            if (mechanic instanceof PersistentMechanic && ((PersistentMechanic) mechanic).isActive()) {
                try {
                    mechanic.think();
                } catch (Throwable t) { // Mechanic failed to think for some reason
                    CraftBookPlugin.logger().log(Level.WARNING, "CraftBook mechanic: Failed to think for " + mechanic.getClass().getSimpleName());
                    BukkitUtil.printStacktrace(t);
                    if(mechanic instanceof ICMechanic && CraftBookPlugin.inst().getConfiguration().ICBreakOnError)
                        BukkitUtil.toSign(((ICMechanic)mechanic).getIC().getSign()).getBlock().breakNaturally();
                }
            } else {
                unload(mechanic, null);
                if(mechanic instanceof ICMechanic) {
                    try {
                        load(((ICMechanic) mechanic).getIC().getSign().getBlockVector(), null);
                    } catch (InvalidMechanismException ignored) {
                    }
                }
            }
        }
    }
}