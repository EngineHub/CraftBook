package com.sk89q.craftbook.mechanics.ic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;

/**
 * @author Silthus
 */
public abstract class AbstractChipState implements ChipState {

    protected final ChangedSign sign;
    protected final Location source;
    protected final boolean selfTriggered;
    protected final Block icBlock;

    protected AbstractChipState(Location source, ChangedSign sign, boolean selfTriggered) {

        // Check this here to prevent and handle future NPEs
        Validate.notNull(sign, "Null ChangedSign found: " + source.toString());
        this.sign = sign;
        this.source = source;
        this.selfTriggered = selfTriggered;
        icBlock = SignUtil.getBackBlock(CraftBookBukkitUtil.toSign(sign).getBlock());
    }

    protected abstract Block getBlock(int pin);

    @Override
    public boolean get(int pin) {
        Block block = getBlock(pin);
        if(block == null) return false;
        BlockData data = block.getBlockData();
        if (data instanceof AnaloguePowerable) {
            return ((AnaloguePowerable) data).getPower() > 0;
        }
        if (data instanceof Powerable) {
            return ((Powerable) data).isPowered();
        }
        return block.isBlockIndirectlyPowered();
    }

    @Override
    public void set(int pin, boolean value) {

        Block block = getBlock(pin);
        if (block != null) {
            ICUtil.setState(block, value, icBlock);
        }
    }

    @Override
    public boolean isTriggered(int pin) {
        Block block = getBlock(pin);
        return block != null && BukkitAdapter.adapt(block.getLocation()).equals(source);
    }

    @Override
    public boolean isValid(int pin) {
        Block block = getBlock(pin);
        if (block != null) {
            return block.getType() == Material.REDSTONE_WIRE
                    || block.getType() == Material.REPEATER
                    || block.getType() == Material.COMPARATOR
                    || block.getType() == Material.LEVER;
        }
        return false;
    }
}
