package com.sk89q.craftbook.ic;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.Diode;

import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * @author Silthus
 */
public abstract class AbstractChipState implements ChipState {

    protected final Sign sign;
    protected final BlockWorldVector source;
    protected final boolean selfTriggered;
    protected final Block icBlock;

    protected AbstractChipState(BlockWorldVector source, Sign sign, boolean selfTriggered) {

        this.sign = sign;
        this.source = source;
        this.selfTriggered = selfTriggered;
        icBlock = SignUtil.getBackBlock(sign.getBlock());
    }

    protected abstract Block getBlock(int pin);

    @Override
    public boolean get(int pin) {

        Block block = getBlock(pin);
        return block != null && block.isBlockIndirectlyPowered();
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
        if (block != null) if (block.getTypeId() == BlockID.REDSTONE_WIRE)
            return true;
        else if (block.getTypeId() == BlockID.REDSTONE_REPEATER_OFF
                || block.getTypeId() == BlockID.REDSTONE_REPEATER_ON)
            if (block.getRelative(((Diode) block.getState().getData()).getFacing()).equals(sign.getBlock()))
                return true;
        return false;
    }
}
