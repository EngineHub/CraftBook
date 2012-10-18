package com.sk89q.craftbook.ic;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.Diode;

/**
 * @author Silthus
 */
public abstract class AbstractChipState implements ChipState {

    protected final Sign sign;
    protected final BlockWorldVector source;

    public AbstractChipState(BlockWorldVector source, Sign sign) {

        this.sign = sign;
        this.source = source;
    }

    protected abstract Block getBlock(int pin);

    @Override
    public boolean isTriggered(int pin) {

        Block block = getBlock(pin);
        return block != null && BukkitUtil.toWorldVector(block).equals(source);
    }

    @Override
    public boolean isValid(int pin) {

        Block block = getBlock(pin);
        if (block != null) if (block.getType() == Material.REDSTONE_WIRE)
            return true;
        else if (block.getType() == Material.DIODE_BLOCK_OFF
                || block.getType() == Material.DIODE_BLOCK_ON)
            if (block.getRelative(((Diode) block.getState().getData()).getFacing()).equals(sign.getBlock()))
                return true;
        return false;
    }
}
