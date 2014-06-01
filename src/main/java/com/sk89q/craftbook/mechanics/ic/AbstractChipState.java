package com.sk89q.craftbook.mechanics.ic;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.Lever;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;

/**
 * @author Silthus
 */
public abstract class AbstractChipState implements ChipState {

    protected final ChangedSign sign;
    protected final BlockWorldVector source;
    protected final boolean selfTriggered;
    protected final Block icBlock;

    protected AbstractChipState(BlockWorldVector source, ChangedSign sign, boolean selfTriggered) {

        // Check this here to prevent and handle future NPEs
        Validate.notNull(sign, "Null ChangedSign found: " + source.toString());
        this.sign = sign;
        this.source = source;
        this.selfTriggered = selfTriggered;
        icBlock = SignUtil.getBackBlock(BukkitUtil.toSign(sign).getBlock());
    }

    protected abstract Block getBlock(int pin);

    @Override
    public boolean get(int pin) {

        Block block = getBlock(pin);
        if(block == null) return false;
        if(block.getType() == Material.LEVER)
            return ((Lever) block.getState().getData()).isPowered();
        return block.isBlockIndirectlyPowered() || block.getType() == Material.DIODE_BLOCK_ON;
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
        return block != null && BukkitUtil.toWorldVector(block).equals(source);
    }

    @Override
    public boolean isValid(int pin) {

        Block block = getBlock(pin);
        if (block != null)
            if (block.getType() == Material.REDSTONE_WIRE || block.getType() == Material.DIODE_BLOCK_OFF || block.getType() == Material.DIODE_BLOCK_ON || block.getType() == Material.LEVER)
                return true;
        return false;
    }
}
