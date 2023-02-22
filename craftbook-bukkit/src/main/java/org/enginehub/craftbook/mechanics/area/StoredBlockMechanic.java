/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.mechanics.area;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanic.exception.InvalidMechanismException;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.mechanics.pipe.PipeFinishEvent;
import org.enginehub.craftbook.mechanics.pipe.PipePutEvent;
import org.enginehub.craftbook.mechanics.pipe.PipeSuckEvent;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public abstract class StoredBlockMechanic extends AbstractCraftBookMechanic {

    private NamespacedKey storedBlockTypeKey;
    private NamespacedKey storedBlockQuantityKey;

    @Override
    public void enable() throws MechanicInitializationException {
        super.enable();

        this.storedBlockTypeKey = new NamespacedKey("craftbook", "toggle_block_type");
        this.storedBlockQuantityKey = new NamespacedKey("craftbook", "toggle_block_quantity");
    }


    /**
     * Find the base block of this mechanic attached to the given sign.
     *
     * @param sign The sign
     * @return The base block
     * @throws InvalidMechanismException if this is not a valid construct
     */
    public abstract Block getBlockBase(Block sign) throws InvalidMechanismException;

    public abstract boolean isApplicableSign(String line);

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeFinish(PipeFinishEvent event) {
        if (!EventUtil.passesFilter(event) || !SignUtil.isSign(event.getOrigin())) {
            return;
        }

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getOrigin());

        if (!isApplicableSign(sign.getLine(1))) {
            return;
        }

        List<ItemStack> leftovers = new ArrayList<>();
        try {
            Material type = getOrSetStoredType(event.getOrigin());
            for (ItemStack stack : event.getItems()) {
                if (stack.getType() != type) {
                    leftovers.add(stack);
                    continue;
                }

                addToStoredBlockCount(sign.getSign(), stack.getAmount());
            }

            event.setItems(leftovers);
        } catch (InvalidMechanismException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipePut(PipePutEvent event) {
        if (!EventUtil.passesFilter(event) || !SignUtil.isSign(event.getPuttingBlock())) {
            return;
        }

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getPuttingBlock());

        if (!isApplicableSign(sign.getLine(1))) {
            return;
        }

        List<ItemStack> leftovers = new ArrayList<>();
        try {
            Material type = getOrSetStoredType(event.getPuttingBlock());
            for (ItemStack stack : event.getItems()) {
                if (stack.getType() != type) {
                    leftovers.add(stack);
                    continue;
                }

                addToStoredBlockCount(sign.getSign(), stack.getAmount());
            }

            event.setItems(leftovers);
        } catch (InvalidMechanismException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeSuck(PipeSuckEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!SignUtil.isSign(event.getSuckedBlock())) {
            return;
        }

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getSuckedBlock());

        if (!isApplicableSign(sign.getLine(1))) {
            return;
        }

        List<ItemStack> items = event.getItems();
        try {
            Material base = getOrSetStoredType(event.getSuckedBlock());
            int blocks = getStoredBlockCount(sign.getSign());
            if (blocks > 0) {
                items.add(new ItemStack(base, blocks));
                setStoredBlockCount(sign.getSign(), 0);
            }
            event.setItems(items);
        } catch (InvalidMechanismException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!SignUtil.isSign(event.getBlock())) {
            return;
        }

        Sign sign = (Sign) event.getBlock().getState(false);

        if (!isApplicableSign(sign.getLine(1))) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        int amount = getStoredBlockCounts(sign);

        if (amount > 0) {
            try {
                Material base = getOrSetStoredType(event.getBlock());
                while (amount > 0) {
                    ItemStack toDrop = new ItemStack(base, Math.min(amount, 64));
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), toDrop);
                    amount -= 64;
                }
            } catch (InvalidMechanismException e) {
                if (e.getMessage() != null)
                    player.printError(e.getMessage());
            }
        }
    }

    /**
     * Removes the given amount from the given signs stored block count.
     *
     * <p>
     * If `enforce-type` is set, it will only remove the amount from blocks of the type stored in the first sign.
     * </p>
     *
     * @param amount The amount
     * @param signs The signs
     * @return If the removal could be successfully completed
     */
    public boolean takeFromStoredBlockCounts(int amount, Sign ... signs) {
        if (signs.length == 0 || amount < 0) {
            // Sanity check for an empty list, given we're making assumptions about at least one sign below.
            return false;
        }
        if (amount == 0) {
            // Declare removals of 0 as successful always.
            return true;
        }

        int current = getStoredBlockCounts(signs);
        Material type = getStoredType(signs[0]);

        if (current < amount) {
            // We can't do this, there are less than the required blocks available.
            return false;
        }

        for (Sign sign : signs) {
            if (sign == null || getStoredType(sign) != type) {
                continue;
            }
            int stored = getStoredBlockCount(sign);
            if (stored >= amount) {
                return setStoredBlockCount(sign, stored - amount);
            } else {
                setStoredBlockCount(sign, 0);
                amount -= stored;
            }
        }

        return amount == 0;
    }

    /**
     * Adds the given amount to the stored block count.
     *
     * @param sign The sign
     * @param amount The amount
     * @return If the value was set
     */
    public boolean addToStoredBlockCount(Sign sign, int amount) {
        var currentBlocks = getStoredBlockCount(sign);
        return setStoredBlockCount(sign, currentBlocks + amount);
    }

    /**
     * Gets the amount of blocks stored across an array of signs.
     *
     * <p>
     * If `enforce-type` is set, it will only return the amount of blocks of the type stored in the first sign.
     * </p>
     *
     * @param signs The list of signs
     * @return The stored block count
     */
    public int getStoredBlockCounts(Sign ... signs) {
        if (signs.length == 0) {
            // Sanity check for an empty list, given we're making assumptions about at least one sign below.
            return 0;
        }

        Material type = getStoredType(signs[0]);

        int sum = 0;
        for (Sign sign : signs) {
            if (sign != null && getStoredType(sign) == type) {
                sum += getStoredBlockCount(sign);
            }
        }

        return sum;
    }

    /**
     * Gets the number of stored blocks within this sign.
     *
     * @param sign The sign
     * @return The stored block count
     */
    public int getStoredBlockCount(Sign sign) {
        if (sign.getPersistentDataContainer().has(storedBlockQuantityKey, PersistentDataType.INTEGER)) {
            //noinspection DataFlowIssue
            return sign.getPersistentDataContainer().get(storedBlockQuantityKey, PersistentDataType.INTEGER);
        }
        return 0;
    }

    /**
     * Sets the stored block count within this sign.
     *
     * @param sign The sign
     * @param count The count
     * @return If the count was set
     */
    public boolean setStoredBlockCount(Sign sign, int count) {
        if (count < 0) {
            return false;
        }
        sign.getPersistentDataContainer().set(storedBlockQuantityKey, PersistentDataType.INTEGER, count);
        return true;
    }

    /**
     * Gets the currently set stored block type in this sign. If the sign has no stored block type it will return null.
     * Most likely you want to use {@link #getOrSetStoredType(Block)} instead, as this will ensure the data exists.
     *
     * @param sign The sign
     * @return The stored material, if applicable
     */
    @Nullable
    public Material getStoredType(Sign sign) {
        if (sign.getPersistentDataContainer().has(storedBlockTypeKey, PersistentDataType.STRING)) {
            String signBlockData = sign.getPersistentDataContainer().get(storedBlockTypeKey, PersistentDataType.STRING);
            if (signBlockData == null) {
                throw new IllegalStateException(getClass().getSimpleName() + " sign has corrupt stored block type at " + sign.getLocation());
            }
            BlockType parsedBlock = BlockType.REGISTRY.get(signBlockData);
            if (parsedBlock == null) {
                throw new IllegalStateException(getClass().getSimpleName() + " sign has corrupt stored block type at " + sign.getLocation());
            }
            return BukkitAdapter.adapt(parsedBlock);
        }
        return null;
    }

    /**
     * Sets the stored block type in this sign.
     *
     * @param sign The sign
     * @param material The material to set
     */
    public void setStoredType(Sign sign, Material material) {
        sign.getPersistentDataContainer().set(storedBlockTypeKey, PersistentDataType.STRING, BukkitAdapter.asBlockType(material).getId());
    }

    /**
     * Gets the block type of this mechanic. Usually passes through, but can be used by enforce
     * type.
     *
     * @param block The block location
     * @return The type
     */
    public Material getOrSetStoredType(Block block) throws InvalidMechanismException {
        Sign sign = (Sign) block.getState(false);
        Material storedType = getStoredType(sign);

        if (storedType == null) {
            Material realType = getBlockBase(block).getType();
            setStoredType(sign, realType);
            return realType;
        }

        return storedType;
    }

    public int getCostOfBlock(BlockData block) {
        if (Tag.SLABS.isTagged(block.getMaterial()) && block instanceof Slab slab && slab.getType() == Slab.Type.DOUBLE) {
            return 2;
        }
        return 1;
    }
}
