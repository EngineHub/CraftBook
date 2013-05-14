// $Id$
/*
 * CraftBook Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

import java.util.ArrayList;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.Directional;
import org.bukkit.material.PressureSensor;

import com.sk89q.craftbook.RightClickBlockEvent;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.BlockWorldVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;

/**
 * This adapter hooks a mechanic manager up to Bukkit.
 *
 * @author sk89q
 */
public class MechanicListenerAdapter implements Listener {

    public static ArrayList<Event> ignoredEvents = new ArrayList<Event>();

    /**
     * Constructs the adapter.
     */
    public MechanicListenerAdapter() {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (ignoredEvents.contains(event)) {
            ignoredEvents.remove(event);
            return;
        }

        boolean isRightClick = false;

        if(CraftBookPlugin.inst().getConfiguration().experimentalClicks && event.getAction() == Action.RIGHT_CLICK_AIR) {
            isRightClick = event.getPlayer().getTargetBlock(null, 5).getTypeId() != 0;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || isRightClick)
            CraftBookPlugin.inst().getManager().dispatchBlockRightClick(isRightClick ? new RightClickBlockEvent(event, event.getPlayer().getTargetBlock(null, 5)) : event);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK)
            CraftBookPlugin.inst().getManager().dispatchBlockLeftClick(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {

        if (ignoredEvents.contains(event)) {
            ignoredEvents.remove(event);
            return;
        }
        CraftBookPlugin.inst().getManager().dispatchSignChange(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {

        if (ignoredEvents.contains(event)) {
            ignoredEvents.remove(event);
            return;
        }

        if(CraftBookPlugin.inst().canBuild(event.getPlayer(), event.getBlock().getLocation()) && !(CraftBookPlugin.inst().getConfiguration().advancedBlockChecks && event.isCancelled())) {
            switch(event.getBlock().getTypeId()) {

                case BlockID.REDSTONE_TORCH_ON:
                case BlockID.REDSTONE_REPEATER_ON:
                case BlockID.REDSTONE_BLOCK:
                case BlockID.COMPARATOR_ON:
                    handleRedstoneForBlock(event.getBlock(), 15, 0);
                    break;
                case BlockID.REDSTONE_WIRE:
                    if(event.getBlock().getData() > 0)
                        handleRedstoneForBlock(event.getBlock(), event.getBlock().getData(), 0);
                    break;
                case BlockID.LEVER:
                    if(((org.bukkit.material.Lever) event.getBlock().getState().getData()).isPowered())
                        handleRedstoneForBlock(event.getBlock(), 15, 0);
                    break;
                case BlockID.WOODEN_BUTTON:
                case BlockID.STONE_BUTTON:
                    if(((org.bukkit.material.Button) event.getBlock().getState().getData()).isPowered())
                        handleRedstoneForBlock(event.getBlock(), 15, 0);
                    break;
                case BlockID.STONE_PRESSURE_PLATE:
                case BlockID.WOODEN_PRESSURE_PLATE:
                case BlockID.PRESSURE_PLATE_HEAVY:
                case BlockID.PRESSURE_PLATE_LIGHT:
                case BlockID.DETECTOR_RAIL:
                    if(event.getBlock().getState().getData() instanceof PressureSensor && ((PressureSensor) event.getBlock().getState().getData()).isPressed())
                        handleRedstoneForBlock(event.getBlock(), 15, 0);
                    break;
            }
        }

        CraftBookPlugin.inst().getManager().dispatchBlockBreak(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {

        if (ignoredEvents.contains(event)) {
            ignoredEvents.remove(event);
            return;
        }
        int oldLevel = event.getOldCurrent();
        int newLevel = event.getNewCurrent();
        Block block = event.getBlock();

        handleRedstoneForBlock(block, oldLevel, newLevel);
    }

    public void handleRedstoneForBlock(Block block, int oldLevel, int newLevel) {

        World world = block.getWorld();
        BlockWorldVector v = BukkitUtil.toWorldVector(block);

        // Give the method a BlockVector instead of a Block
        boolean wasOn = oldLevel >= 1;
        boolean isOn = newLevel >= 1;
        boolean wasChange = wasOn != isOn;

        // For efficiency reasons, we're only going to consider changes between
        // off and on state, and ignore simple current changes (i.e. 15->13)
        if (!wasChange) return;

        LocalWorld w = BukkitUtil.getLocalWorld(world);
        int x = v.getBlockX();
        int y = v.getBlockY();
        int z = v.getBlockZ();

        int type = block.getTypeId();

        // When this hook has been called, the level in the world has not
        // yet been updated, so we're going to do this very ugly thing of
        // faking the value with the new one whenever the data value of this
        // block is requested -- it is quite ugly

        if (type == BlockID.REDSTONE_WIRE) {

            if (CraftBookPlugin.inst().getConfiguration().indirectRedstone) {

                // power all blocks around the redstone wire on the same y level
                // north/south
                handleDirectWireInput(new WorldVector(w, x - 1, y, z), block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x + 1, y, z), block, oldLevel, newLevel);
                // east/west
                handleDirectWireInput(new WorldVector(w, x, y, z - 1), block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x, y, z + 1), block, oldLevel, newLevel);

                // Can be triggered from below
                handleDirectWireInput(new WorldVector(w, x, y + 1, z), block, oldLevel, newLevel);

                // Can be triggered from above (Eg, glass->glowstone like redstone lamps)
                handleDirectWireInput(new WorldVector(w, x, y - 1, z), block, oldLevel, newLevel);
            } else {

                int above = world.getBlockTypeIdAt(x, y + 1, z);

                int westSide = world.getBlockTypeIdAt(x, y, z + 1);
                int westSideAbove = world.getBlockTypeIdAt(x, y + 1, z + 1);
                int westSideBelow = world.getBlockTypeIdAt(x, y - 1, z + 1);
                int eastSide = world.getBlockTypeIdAt(x, y, z - 1);
                int eastSideAbove = world.getBlockTypeIdAt(x, y + 1, z - 1);
                int eastSideBelow = world.getBlockTypeIdAt(x, y - 1, z - 1);

                int northSide = world.getBlockTypeIdAt(x - 1, y, z);
                int northSideAbove = world.getBlockTypeIdAt(x - 1, y + 1, z);
                int northSideBelow = world.getBlockTypeIdAt(x - 1, y - 1, z);
                int southSide = world.getBlockTypeIdAt(x + 1, y, z);
                int southSideAbove = world.getBlockTypeIdAt(x + 1, y + 1, z);
                int southSideBelow = world.getBlockTypeIdAt(x + 1, y - 1, z);

                // Make sure that the wire points to only this block
                if (!BlockType.isRedstoneBlock(westSide) && !BlockType.isRedstoneBlock(eastSide)
                        && (!BlockType.isRedstoneBlock(westSideAbove) || westSide == 0 || above != 0)
                        && (!BlockType.isRedstoneBlock(eastSideAbove) || eastSide == 0 || above != 0)
                        && (!BlockType.isRedstoneBlock(westSideBelow) || westSide != 0)
                        && (!BlockType.isRedstoneBlock(eastSideBelow) || eastSide != 0)) {
                    // Possible blocks north / south
                    handleDirectWireInput(new WorldVector(w, x - 1, y, z), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x + 1, y, z), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x - 1, y - 1, z), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x + 1, y - 1, z), block, oldLevel, newLevel);
                }

                if (!BlockType.isRedstoneBlock(northSide) && !BlockType.isRedstoneBlock(southSide)
                        && (!BlockType.isRedstoneBlock(northSideAbove) || northSide == 0 || above != 0)
                        && (!BlockType.isRedstoneBlock(southSideAbove) || southSide == 0 || above != 0)
                        && (!BlockType.isRedstoneBlock(northSideBelow) || northSide != 0)
                        && (!BlockType.isRedstoneBlock(southSideBelow) || southSide != 0)) {
                    // Possible blocks west / east
                    handleDirectWireInput(new WorldVector(w, x, y, z - 1), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x, y, z + 1), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x, y - 1, z - 1), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x, y - 1, z + 1), block, oldLevel, newLevel);
                }

                // Can be triggered from below
                handleDirectWireInput(new WorldVector(w, x, y + 1, z), block, oldLevel, newLevel);
            }
            return;
        } else if (type == BlockID.REDSTONE_REPEATER_OFF || type == BlockID.REDSTONE_REPEATER_ON || type == BlockID.COMPARATOR_OFF || type == BlockID.COMPARATOR_ON) {

            Directional diode = (Directional) block.getState().getData();
            BlockFace f = diode.getFacing();
            handleDirectWireInput(new WorldVector(w, x + f.getModX(), y, z + f.getModZ()), block, oldLevel, newLevel);
            if(block.getRelative(f).getTypeId() != 0) {
                handleDirectWireInput(new WorldVector(w, x + f.getModX(), y - 1, z + f.getModZ()), block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x + f.getModX(), y + 1, z + f.getModZ()), block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x + f.getModX() + 1, y - 1, z + f.getModZ()), block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x + f.getModX() - 1, y - 1, z + f.getModZ()), block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x + f.getModX() + 1, y - 1, z + f.getModZ() + 1), block, oldLevel, newLevel);
                handleDirectWireInput(new WorldVector(w, x + f.getModX() - 1, y - 1, z + f.getModZ() - 1), block, oldLevel, newLevel);
            }
            return;
        } else if (type == BlockID.STONE_BUTTON || type == BlockID.WOODEN_BUTTON || type == BlockID.LEVER) {

            Attachable button = (Attachable) block.getState().getData();
            BlockFace f = button.getAttachedFace();
            handleDirectWireInput(new WorldVector(w, x + f.getModX()*2, y, z + f.getModZ()*2), block, oldLevel, newLevel);
        }
        // For redstone wires and repeaters, the code already exited this method
        // Non-wire blocks proceed

        handleDirectWireInput(new WorldVector(w, x - 1, y, z), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x + 1, y, z), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x - 1, y - 1, z), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x + 1, y - 1, z), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x, y, z - 1), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x, y, z + 1), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x, y - 1, z - 1), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x, y - 1, z + 1), block, oldLevel, newLevel);

        // Can be triggered from below
        handleDirectWireInput(new WorldVector(w, x, y + 1, z), block, oldLevel, newLevel);
    }

    /**
     * Handle the direct wire input.
     *
     * @param pt
     * @param sourceBlock
     * @param oldLevel
     * @param newLevel
     */
    protected void handleDirectWireInput(WorldVector pt, Block sourceBlock, int oldLevel, int newLevel) {

        Block block = ((BukkitWorld) pt.getWorld()).getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if(block.getLocation().equals(sourceBlock.getLocation())) //The same block, don't run.
            return;
        CraftBookPlugin.inst().getManager().dispatchBlockRedstoneChange(new SourcedBlockRedstoneEvent(sourceBlock, block, oldLevel, newLevel));
    }

    /**
     * Called when a chunk is loaded.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoad(final ChunkLoadEvent event) {

        if (ignoredEvents.contains(event)) {
            ignoredEvents.remove(event);
            return;
        }
        CraftBookPlugin.server().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

            @Override
            public void run() {

                CraftBookPlugin.inst().getManager().enumerate(event.getChunk());
            }
        }, 2);
    }

    /**
     * Called when a chunk is unloaded.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent event) {

        if (ignoredEvents.contains(event)) {
            ignoredEvents.remove(event);
            return;
        }
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();

        CraftBookPlugin.inst().getManager().unload(new BlockWorldVector2D(BukkitUtil.getLocalWorld(event.getWorld()), chunkX, chunkZ), event);
    }
}