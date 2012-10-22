package com.sk89q.craftbook.ic;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.Diode;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * @author Silthus
 */
public abstract class AbstractChipState implements ChipState {

    protected final Sign sign;
    protected final BlockWorldVector source;
	protected final boolean selfTriggered;

	protected AbstractChipState(BlockWorldVector source, Sign sign, boolean selfTriggered) {
		this.sign = sign;
		this.source = source;
		this.selfTriggered = selfTriggered;
	}

	protected abstract Block getBlock(int pin);

	@Override
	public boolean get(int pin) {

		Block block = getBlock(pin);
		return block != null && (selfTriggered || isTriggered(pin)) && block.isBlockIndirectlyPowered();
	}

	@Override
	public void set(int pin, boolean value) {

		Block block = getBlock(pin);
		if (block != null) {
			ICUtil.setState(block, value);
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
        if (block != null) if (block.getType() == Material.REDSTONE_WIRE)
            return true;
        else if (block.getType() == Material.DIODE_BLOCK_OFF
                || block.getType() == Material.DIODE_BLOCK_ON)
            if (block.getRelative(((Diode) block.getState().getData()).getFacing()).equals(sign.getBlock()))
                return true;
        return false;
    }
}
